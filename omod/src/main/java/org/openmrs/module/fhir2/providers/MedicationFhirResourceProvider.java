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

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.util.FhirServerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class MedicationFhirResourceProvider implements IResourceProvider {
	
	@Inject
	private FhirMedicationService fhirMedicationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Medication.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Medication getMedicationByUuid(@IdParam @NotNull IdType id) {
		Medication medication = fhirMedicationService.getMedicationByUuid(id.getIdPart());
		if (medication == null) {
			throw new ResourceNotFoundException("Could not find medication with Id " + id.getIdPart());
		}
		return medication;
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle searchForMedication(@OptionalParam(name = Medication.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = Medication.SP_FORM) TokenAndListParam dosageForm,
	        @OptionalParam(name = Medication.SP_STATUS) TokenOrListParam status) {
		return FhirServerUtils
		        .convertSearchResultsToBundle(fhirMedicationService.searchForMedications(code, dosageForm, null, status));
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createMedication(@ResourceParam Medication medication) {
		return FhirServerUtils.buildCreate(fhirMedicationService.saveMedication(medication));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateMedication(@IdParam IdType id, @ResourceParam Medication medication) {
		if (id != null) {
			medication.setId(id.getIdPart());
		}
		
		return FhirServerUtils.buildUpdate(fhirMedicationService.updateMedication(medication, id.getIdPart()));
	}
	
}
