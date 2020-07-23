/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.conv30_40.RelatedPerson30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("relatedPersonFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class RelatedPersonFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirRelatedPersonService relatedPersonService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return RelatedPerson.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public RelatedPerson getRelatedPersonById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.RelatedPerson relatedPerson = relatedPersonService.get(id.getIdPart());
		if (relatedPerson == null) {
			throw new ResourceNotFoundException("Could not find relatedPerson with Id " + id.getIdPart());
		}
		return RelatedPerson30_40.convertRelatedPerson(relatedPerson);
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchRelatedPerson(@OptionalParam(name = RelatedPerson.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = RelatedPerson.SP_GENDER) TokenAndListParam gender,
	        @OptionalParam(name = RelatedPerson.SP_BIRTHDATE) DateRangeParam birthDate,
	        @OptionalParam(name = RelatedPerson.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = RelatedPerson.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = RelatedPerson.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = RelatedPerson.SP_ADDRESS_COUNTRY) StringAndListParam country,
	        @OptionalParam(name = RelatedPerson.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @IncludeParam(allow = { "RelatedPerson:" + RelatedPerson.SP_PATIENT }) HashSet<Include> includes) {
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return new SearchQueryBundleProviderR3Wrapper(relatedPersonService.searchForRelatedPeople(name, gender, birthDate,
		    city, state, postalCode, country, id, lastUpdated, sort, includes));
	}
}
