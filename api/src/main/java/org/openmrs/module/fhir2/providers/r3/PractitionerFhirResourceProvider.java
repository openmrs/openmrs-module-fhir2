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

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.conv30_40.Practitioner30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashSet;

import static lombok.AccessLevel.PACKAGE;

@Component("practitionerFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class PractitionerFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPractitionerService practitionerService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Practitioner getPractitionerById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Practitioner practitioner = practitionerService.get(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner with Id " + id.getIdPart());
		}
		
		return Practitioner30_40.convertPractitioner(practitioner);
	}
	
	@Create
	public MethodOutcome createPractitioner(@ResourceParam Practitioner practitioner) {
		return FhirProviderUtils.buildCreate(Practitioner30_40
		        .convertPractitioner(practitionerService.create(Practitioner30_40.convertPractitioner(practitioner))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updatePractitioner(@IdParam IdType id, @ResourceParam Practitioner practitioner) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		practitioner.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(Practitioner30_40.convertPractitioner(
		    practitionerService.update(id.getIdPart(), Practitioner30_40.convertPractitioner(practitioner))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deletePractitioner(@IdParam @Nonnull IdType id) {
		practitionerService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR3();
	}
	
	@Search
	public IBundleProvider searchForPractitioners(@OptionalParam(name = Practitioner.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenAndListParam identifier,
	        @OptionalParam(name = Practitioner.SP_GIVEN) StringAndListParam given,
	        @OptionalParam(name = Practitioner.SP_FAMILY) StringAndListParam family,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_COUNTRY) StringAndListParam country,
	        @OptionalParam(name = Practitioner.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(reverse = true, allow = { "Encounter:" + Encounter.SP_PARTICIPANT,
	                "MedicationRequest:" + MedicationRequest.SP_REQUESTER,
	                "ProcedureRequest:" + ProcedureRequest.SP_REQUESTER,
	                "MedicationDispense:" + MedicationDispense.SP_PRESCRIPTION }) HashSet<Include> revIncludes) {
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return new SearchQueryBundleProviderR3Wrapper(practitionerService.searchForPractitioners(identifier, name, given,
		    family, city, state, postalCode, country, id, lastUpdated, revIncludes));
	}
	
}
