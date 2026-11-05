/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class BaseProfileRoutingR4IntegrationTest<T extends IResourceProvider, U extends DomainResource> extends BaseFhirR4IntegrationTest<T, U> {
	
	protected static final String UNKNOWN_PROFILE = "http://example.com/StructureDefinition/not-a-handler-profile";
	
	protected static String openmrsProfile(String name) {
		return OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/" + name;
	}
	
	protected static List<String> profilesOf(IBaseResource resource) {
		return resource.getMeta().getProfile().stream().map(IPrimitiveType::getValue).collect(Collectors.toList());
	}
	
	/**
	 * Runs a JSON search and follows {@code next} links until the result set is exhausted, returning
	 * the matched resources from every page in order.
	 */
	protected List<Resource> searchAllPages(String uri) throws Exception {
		List<Resource> resources = new ArrayList<>();
		Bundle page = searchPage(uri);
		resources.addAll(matchedResources(page));
		
		Bundle.BundleLinkComponent next = page.getLink(Bundle.LINK_NEXT);
		while (next != null) {
			// only the query string of the next link is meaningful to the mock servlet
			page = searchPage("?" + new URL(next.getUrl()).getQuery());
			resources.addAll(matchedResources(page));
			next = page.getLink(Bundle.LINK_NEXT);
		}
		
		return resources;
	}
	
	protected Bundle searchPage(String uri) throws Exception {
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		Bundle bundle = readBundleResponse(response);
		assertThat(bundle.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		return bundle;
	}
	
	protected static List<Resource> matchedResources(Bundle bundle) {
		return bundle.getEntry().stream()
		        .filter(e -> !e.hasSearch() || e.getSearch().getMode() != Bundle.SearchEntryMode.INCLUDE)
		        .map(Bundle.BundleEntryComponent::getResource).collect(Collectors.toList());
	}
	
	protected static Set<String> idsOf(List<? extends IBaseResource> resources) {
		return resources.stream().map(r -> r.getIdElement().getIdPart())
		        .collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	/** Ids of the given resources that carry {@code profile} in {@code meta.profile}. */
	protected static Set<String> idsWithProfile(List<? extends IBaseResource> resources, String profile) {
		return idsOf(resources.stream().filter(r -> profilesOf(r).contains(profile)).collect(Collectors.toList()));
	}
	
	protected static Matcher<IBaseResource> hasProfiles(Matcher<? super List<String>> matcher) {
		return new FeatureMatcher<IBaseResource, List<String>>(matcher, "resource with meta.profile", "meta.profile") {
			
			@Override
			protected List<String> featureValueOf(IBaseResource actual) {
				return profilesOf(actual);
			}
		};
	}
}
