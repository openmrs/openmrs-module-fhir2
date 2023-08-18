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

import static lombok.AccessLevel.PACKAGE;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
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
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.conv30_40.RelatedPerson30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("relatedPersonFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
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
	
	@Create
	public MethodOutcome createRelatedPerson(@ResourceParam RelatedPerson relatedPerson) {
		return FhirProviderUtils.buildCreate(RelatedPerson30_40
		        .convertRelatedPerson(relatedPersonService.create(RelatedPerson30_40.convertRelatedPerson(relatedPerson))));
	}
	
	@Update
	public MethodOutcome updateRelatedPerson(@IdParam IdType id, @ResourceParam RelatedPerson relatedPerson) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		relatedPerson.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(RelatedPerson30_40.convertRelatedPerson(
		    relatedPersonService.update(id.getIdPart(), RelatedPerson30_40.convertRelatedPerson(relatedPerson))));
	}
	
	@Delete
	public OperationOutcome deleteRelatedPerson(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.RelatedPerson relatedPerson = relatedPersonService.delete(id.getIdPart());
		if (relatedPerson == null) {
			throw new ResourceNotFoundException("Could not find person to delete with id " + id.getIdPart());
		}
		
		return FhirProviderUtils.buildDelete(RelatedPerson30_40.convertRelatedPerson(relatedPerson));
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
