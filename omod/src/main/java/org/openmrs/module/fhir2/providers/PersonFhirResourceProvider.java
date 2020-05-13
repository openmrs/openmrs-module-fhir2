/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import javax.validation.constraints.NotNull;

import java.util.List;

import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.util.FhirServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class PersonFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPersonService fhirPersonService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Person.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Person getPersonById(@IdParam IdType id) {
		Person person = fhirPersonService.get(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find Person with Id " + id.getIdPart());
		}
		return person;
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPersonHistoryById(@IdParam @NotNull IdType id) {
		Person person = fhirPersonService.get(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find person with Id " + id.getIdPart());
		}
		return person.getContained();
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle searchPeople(@OptionalParam(name = Person.SP_NAME) StringOrListParam name,
	        @OptionalParam(name = Person.SP_GENDER) TokenOrListParam gender,
	        @OptionalParam(name = Person.SP_BIRTHDATE) DateRangeParam birthDate,
	        @OptionalParam(name = Person.SP_ADDRESS_CITY) StringOrListParam city,
	        @OptionalParam(name = Person.SP_ADDRESS_STATE) StringOrListParam state,
	        @OptionalParam(name = Person.SP_ADDRESS_POSTALCODE) StringOrListParam postalCode,
	        @OptionalParam(name = Person.SP_ADDRESS_COUNTRY) StringOrListParam country, @Sort SortSpec sort) {
		return FhirServerUtils.convertSearchResultsToBundle(
		    fhirPersonService.searchForPeople(name, gender, birthDate, city, state, postalCode, country, sort));
	}
	
}
