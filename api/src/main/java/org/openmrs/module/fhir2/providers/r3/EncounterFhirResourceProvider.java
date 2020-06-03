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

import javax.validation.constraints.NotNull;

import java.util.List;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Encounter30_40;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("encounterFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class EncounterFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirEncounterService encounterService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Encounter getEncounterById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.get(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter with Id " + id.getIdPart());
		}
		
		return Encounter30_40.convertEncounter(encounter);
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getEncounterHistoryById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.get(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter with Id " + id.getIdPart());
		}
		return Encounter30_40.convertEncounter(encounter).getContained();
	}
	
	@Search
	public IBundleProvider searchEncounter(@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date,
	        @OptionalParam(name = Encounter.SP_LOCATION, chainWhitelist = { "", Location.SP_ADDRESS_CITY,
	                Location.SP_ADDRESS_STATE, Location.SP_ADDRESS_COUNTRY,
	                Location.SP_ADDRESS_POSTALCODE }, targetTypes = Location.class) ReferenceAndListParam location,
	        @OptionalParam(name = Encounter.SP_PARTICIPANT, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
	                Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	                Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
	        @OptionalParam(name = Encounter.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = Encounter.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientParam) {
		if (patientParam != null) {
			subjectReference = patientParam;
		}
		
		return encounterService.searchForEncounters(date, location, participantReference, subjectReference);
	}
	
}
