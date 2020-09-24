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

import java.util.List;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Person30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("personFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class PersonFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPersonService personService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Person.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Person getPersonById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Person person = personService.get(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find person with Id " + id.getIdPart());
		}
		
		return Person30_40.convertPerson(person);
	}
	
	@Create
	public MethodOutcome createPerson(@ResourceParam Person person) {
		return FhirProviderUtils
		        .buildCreate(Person30_40.convertPerson(personService.create(Person30_40.convertPerson(person))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updatePerson(@IdParam IdType id, @ResourceParam Person person) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		person.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(
		    Person30_40.convertPerson(personService.update(id.getIdPart(), Person30_40.convertPerson(person))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deletePerson(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Person person = personService.delete(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find person to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(Person30_40.convertPerson(person));
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPersonHistoryById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Person person = personService.get(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find person with Id " + id.getIdPart());
		}
		return Person30_40.convertPerson(person).getContained();
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
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort) {
		return new SearchQueryBundleProviderR3Wrapper(personService.searchForPeople(name, gender, birthDate, city, state,
		    postalCode, country, id, lastUpdated, sort));
	}
	
}
