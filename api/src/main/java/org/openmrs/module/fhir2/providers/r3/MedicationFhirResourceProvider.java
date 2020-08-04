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
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.conv30_40.Medication30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("medicationFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class MedicationFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirMedicationService medicationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Medication.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Medication getMedicationById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Medication medication = medicationService.get(id.getIdPart());
		if (medication == null) {
			throw new ResourceNotFoundException("Could not find medication with Id " + id.getIdPart());
		}
		
		return Medication30_40.convertMedication(medication);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createMedication(@ResourceParam Medication medication) {
		return FhirProviderUtils.buildCreate(
		    Medication30_40.convertMedication(medicationService.create(Medication30_40.convertMedication(medication))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateMedication(@IdParam IdType id, @ResourceParam Medication medication) {
		if (id != null) {
			medication.setId(id.getIdPart());
		}
		
		return FhirProviderUtils.buildUpdate(Medication30_40.convertMedication(
		    medicationService.update(id == null ? null : id.getIdPart(), Medication30_40.convertMedication(medication))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteMedication(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Medication medication = medicationService.delete(id.getIdPart());
		if (medication == null) {
			throw new ResourceNotFoundException("Could not find medication to update with id " + id.getIdPart());
		}
		
		return FhirProviderUtils.buildDelete(Medication30_40.convertMedication(medication));
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForMedication(@OptionalParam(name = Medication.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = Medication.SP_FORM) TokenAndListParam dosageForm,
	        @OptionalParam(name = Medication.SP_INGREDIENT_CODE) TokenAndListParam ingredientCode,
	        @OptionalParam(name = Medication.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @IncludeParam(reverse = true, allow = {
	                "MedicationRequest:" + MedicationRequest.SP_MEDICATION }) HashSet<Include> revIncludes) {
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return new SearchQueryBundleProviderR3Wrapper(
		        medicationService.searchForMedications(code, dosageForm, ingredientCode, id, lastUpdated, revIncludes));
	}
}
