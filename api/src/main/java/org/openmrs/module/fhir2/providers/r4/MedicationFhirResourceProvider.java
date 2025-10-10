/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.MedicationSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("medicationFhirR4ResourceProvider")
@R4Provider
public class MedicationFhirResourceProvider extends BaseUpsertFhirResourceProvider<Medication> {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirMedicationService fhirMedicationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Medication.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Medication getMedicationByUuid(@IdParam @Nonnull IdType id) {
		Medication medication = fhirMedicationService.get(id.getIdPart());
		if (medication == null) {
			throw new ResourceNotFoundException("Could not find medication with Id " + id.getIdPart());
		}
		return medication;
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createMedication(@ResourceParam Medication medication) {
		return FhirProviderUtils.buildCreate(fhirMedicationService.create(medication));
	}
	
	@Override
	protected MethodOutcome doUpsert(IdType id, Medication medication, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		medication.setId(id.getIdPart());
		
		return FhirProviderUtils
		        .buildUpdate(fhirMedicationService.update(id.getIdPart(), medication, requestDetails, createIfNotExists));
	}
	
	@Patch
	public MethodOutcome patchMedication(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update Medication resource");
		}
		
		Medication medication = fhirMedicationService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(medication);
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteMedication(@IdParam @Nonnull IdType id) {
		fhirMedicationService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForMedication(@OptionalParam(name = Medication.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = Medication.SP_FORM) TokenAndListParam dosageForm,
	        @OptionalParam(name = Medication.SP_INGREDIENT_CODE) TokenAndListParam ingredientCode,
	        @OptionalParam(name = Medication.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(reverse = true, allow = { "MedicationRequest:" + MedicationRequest.SP_MEDICATION,
	                "MedicationDispense:" + MedicationDispense.SP_PRESCRIPTION }) HashSet<Include> revIncludes) {
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return fhirMedicationService.searchForMedications(
		    new MedicationSearchParams(code, dosageForm, ingredientCode, id, lastUpdated, revIncludes));
	}
}
