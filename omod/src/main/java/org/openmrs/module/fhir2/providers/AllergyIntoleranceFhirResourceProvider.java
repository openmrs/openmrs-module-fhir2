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
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.util.FhirServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class AllergyIntoleranceFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirAllergyIntoleranceService fhirAllergyIntoleranceService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AllergyIntolerance.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public AllergyIntolerance getAllergyIntoleranceByUuid(@IdParam @NotNull IdType id) {
		AllergyIntolerance allergy = fhirAllergyIntoleranceService.getAllergyIntoleranceByUuid(id.getIdPart());
		if (allergy == null) {
			throw new ResourceNotFoundException("Could not find allergy with Id " + id.getIdPart());
		}
		return allergy;
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getAllergyIntoleranceHistoryById(@IdParam @NotNull IdType id) {
		AllergyIntolerance allergy = fhirAllergyIntoleranceService.getAllergyIntoleranceByUuid(id.getIdPart());
		if (allergy == null) {
			throw new ResourceNotFoundException("Could not find allergy with Id " + id.getIdPart());
		}
		return allergy.getContained();
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle searchForAllergies(
	        @OptionalParam(name = AllergyIntolerance.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = AllergyIntolerance.SP_CATEGORY) TokenOrListParam category,
	        @OptionalParam(name = AllergyIntolerance.SP_CODE) TokenAndListParam allergen,
	        @OptionalParam(name = AllergyIntolerance.SP_SEVERITY) TokenOrListParam severity,
	        @OptionalParam(name = AllergyIntolerance.SP_MANIFESTATION) TokenAndListParam manifestationCode,
	        @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenOrListParam clinicalStatus) {
		return FhirServerUtils.convertSearchResultsToBundle(fhirAllergyIntoleranceService
		        .searchForAllergies(patientReference, category, allergen, severity, manifestationCode, clinicalStatus));
	}
}
