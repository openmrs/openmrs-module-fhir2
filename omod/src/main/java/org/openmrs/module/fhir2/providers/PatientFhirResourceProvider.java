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

import javax.inject.Inject;
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
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.util.FhirUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class PatientFhirResourceProvider implements IResourceProvider {
	
	@Inject
	private FhirPatientService patientService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Patient getPatientById(@IdParam @NotNull IdType id) {
		Patient patient = patientService.getPatientByUuid(id.getIdPart());
		if (patient == null) {
			throw new ResourceNotFoundException("Could not find patient with Id " + id.getIdPart());
		}
		return patient;
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle searchPatients(@OptionalParam(name = Patient.SP_NAME) StringOrListParam name,
	        @OptionalParam(name = Patient.SP_GIVEN) StringOrListParam given,
	        @OptionalParam(name = Patient.SP_FAMILY) StringOrListParam family,
	        @OptionalParam(name = Patient.SP_IDENTIFIER) TokenOrListParam identifier,
	        @OptionalParam(name = Patient.SP_GENDER) TokenOrListParam gender,
	        @OptionalParam(name = Patient.SP_BIRTHDATE) DateRangeParam birthDate,
	        @OptionalParam(name = Patient.SP_DEATH_DATE) DateRangeParam deathDate,
	        @OptionalParam(name = Patient.SP_DECEASED) TokenOrListParam deceased,
	        @OptionalParam(name = Patient.SP_ADDRESS_CITY) StringOrListParam city,
	        @OptionalParam(name = Patient.SP_ADDRESS_STATE) StringOrListParam state,
	        @OptionalParam(name = Patient.SP_ADDRESS_POSTALCODE) StringOrListParam postalCode, @Sort SortSpec sort) {
		return FhirUtils.convertSearchResultsToBundle(patientService.searchForPatients(name, given, family, identifier,
		    gender, birthDate, deathDate, deceased, city, state, postalCode, sort));
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPatientResourceHistory(@IdParam @NotNull IdType id) {
		Patient patient = patientService.getPatientByUuid(id.getIdPart());
		if (patient == null) {
			throw new ResourceNotFoundException("Could not find patient with Id " + id.getIdPart());
		}
		return patient.getContained();
	}
}
