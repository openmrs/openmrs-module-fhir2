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

import static lombok.AccessLevel.PACKAGE;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Person;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.PersonSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("personFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
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
	
	@Create
	public MethodOutcome createPerson(@ResourceParam Person person) {
		return FhirProviderUtils.buildCreate(fhirPersonService.create(person));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updatePerson(@IdParam IdType id, @ResourceParam Person person) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		person.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(fhirPersonService.update(id.getIdPart(), person));
	}
	
	@Patch
	public MethodOutcome patchPerson(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body, RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch person");
		}
		
		Person person = fhirPersonService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(person);
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deletePerson(@IdParam @Nonnull IdType id) {
		fhirPersonService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchPeople(@OptionalParam(name = Person.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Person.SP_GENDER) TokenAndListParam gender,
	        @OptionalParam(name = Person.SP_BIRTHDATE) DateRangeParam birthDate,
	        @OptionalParam(name = Person.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Person.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = Person.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Person.SP_ADDRESS_COUNTRY) StringAndListParam country,
	        @OptionalParam(name = Person.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort, @IncludeParam(allow = {
	                "Person:" + Person.SP_LINK + ":Patient", "Person:" + Person.SP_PATIENT }) HashSet<Include> includes) {
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return fhirPersonService.searchForPeople(new PersonSearchParams(name, gender, birthDate, city, state, postalCode,
		        country, id, lastUpdated, sort, includes));
	}
	
}
