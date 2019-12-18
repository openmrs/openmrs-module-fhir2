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

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
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
	public Bundle findPatientsByName(@RequiredParam(name = Patient.SP_NAME) @NotNull String name) {
		return FhirUtils.convertSearchResultsToBundle(patientService.findPatientsByName(name));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findPatientsByGivenName(@RequiredParam(name = Patient.SP_GIVEN) @NotNull String given) {
		return FhirUtils.convertSearchResultsToBundle(patientService.findPatientsByGivenName(given));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findPatientsByFamilyName(@RequiredParam(name = Patient.SP_FAMILY) @NotNull String family) {
		return FhirUtils.convertSearchResultsToBundle(patientService.findPatientsByFamilyName(family));
	}
}
