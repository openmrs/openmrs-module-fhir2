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
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("patientFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class PatientFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPatientService patientService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Patient getPatientById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Patient patient = patientService.get(id.getIdPart());
		if (patient == null) {
			throw new ResourceNotFoundException("Could not find patient with Id " + id.getIdPart());
		}
		
		return Patient30_40.convertPatient(patient);
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPatientHistoryById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Patient patient = patientService.get(id.getIdPart());
		if (patient == null) {
			throw new ResourceNotFoundException("Could not find patient with Id " + id.getIdPart());
		}
		
		return Patient30_40.convertPatient(patient).getContained();
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchPatients(@OptionalParam(name = Patient.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Patient.SP_GIVEN) StringAndListParam given,
	        @OptionalParam(name = Patient.SP_FAMILY) StringAndListParam family,
	        @OptionalParam(name = Patient.SP_IDENTIFIER) TokenAndListParam identifier,
	        @OptionalParam(name = Patient.SP_GENDER) TokenAndListParam gender,
	        @OptionalParam(name = Patient.SP_BIRTHDATE) DateRangeParam birthDate,
	        @OptionalParam(name = Patient.SP_DEATH_DATE) DateRangeParam deathDate,
	        @OptionalParam(name = Patient.SP_DECEASED) TokenAndListParam deceased,
	        @OptionalParam(name = Patient.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Patient.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = Patient.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Patient.SP_ADDRESS_COUNTRY) StringAndListParam country, @Sort SortSpec sort) {
		return patientService.searchForPatients(name, given, family, identifier, gender, birthDate, deathDate, deceased,
		    city, state, postalCode, country, sort);
	}
	
}
