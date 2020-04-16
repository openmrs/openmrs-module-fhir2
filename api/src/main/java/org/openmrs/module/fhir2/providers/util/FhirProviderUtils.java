/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.util;

import java.util.Collection;

import ca.uhn.fhir.rest.api.MethodOutcome;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FhirProviderUtils {
	
	public static MethodOutcome buildUpdate(DomainResource resource) {
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(false);
		return buildWithResource(methodOutcome, resource);
	}
	
	public static MethodOutcome buildCreate(DomainResource resource) {
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(true);
		return buildWithResource(methodOutcome, resource);
	}
	
	private static MethodOutcome buildWithResource(MethodOutcome methodOutcome, DomainResource resource) {
		if (resource != null) {
			if (resource.getId() != null) {
				methodOutcome.setId(resource.getIdElement());
			}
			
			methodOutcome.setResource(resource);
		}
		
		return methodOutcome;
	}
	
	public static <T extends Resource> Bundle convertSearchResultsToBundle(Collection<T> resources) {
		Bundle bundle = FhirProviderUtils.convertIterableToBundle(resources);
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setTotal(resources.size());
		return bundle;
	}
	
	public static <T extends Resource> Bundle convertIterableToBundle(Iterable<T> resources) {
		Bundle bundle = new Bundle();
		for (T resource : resources) {
			Bundle.BundleEntryComponent entry = bundle.addEntry();
			entry.setResource(resource);
		}
		
		return bundle;
	}
	
}
