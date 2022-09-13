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

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("medicationDispenseFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class MedicationDispenseFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirMedicationDispenseService fhirMedicationDispenseService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationDispense.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public MedicationDispense getMedicationDispenseByUuid(@IdParam @Nonnull IdType id) {
		MedicationDispense medicationDispense = fhirMedicationDispenseService.get(id.getIdPart());
		if (medicationDispense == null) {
			throw new ResourceNotFoundException("Could not find medicationDispense with Id " + id.getIdPart());
		}
		return medicationDispense;
	}
	
	@Create
	public MethodOutcome createMedicationDispense(@ResourceParam MedicationDispense mDispense) {
		MedicationDispense medicationDispense = fhirMedicationDispenseService.create(mDispense);
		return FhirProviderUtils.buildCreate(medicationDispense);
	}
	
	public MethodOutcome updateMedicationDispense(@IdParam IdType id, @ResourceParam MedicationDispense mDispense) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update resource");
		}
		mDispense.setId(id.getIdPart());
		MedicationDispense medicationDispense = fhirMedicationDispenseService.update(id.getIdPart(), mDispense);
		return FhirProviderUtils.buildUpdate(medicationDispense);
	}

	public OperationOutcome deleteMedicationDispense(@IdParam IdType id) {
		fhirMedicationDispenseService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForMedicationDispenses(
	        @OptionalParam(name = MedicationDispense.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = MedicationDispense.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = MedicationDispense.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = MedicationDispense.SP_CONTEXT, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = MedicationDispense.SP_PRESCRIPTION, chainWhitelist = {
	                "" }, targetTypes = MedicationRequest.class) ReferenceAndListParam medicationRequestReference,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(allow = { "MedicationDispense:" + MedicationDispense.SP_PATIENT,
	                "MedicationDispense:" + MedicationDispense.SP_CONTEXT,
	                "MedicationDispense:" + MedicationDispense.SP_PRESCRIPTION,
	                "MedicationDispense:" + MedicationDispense.SP_MEDICATION,
	                "MedicationDispense:" + MedicationDispense.SP_PERFORMER }) HashSet<Include> includes,
	        @Sort SortSpec sort) {
		
		MedicationDispenseSearchParams params = new MedicationDispenseSearchParams();
		params.setId(id);
		params.setPatient(patientReference == null ? subjectReference : patientReference);
		params.setEncounter(encounterReference);
		params.setMedicationRequest(medicationRequestReference);
		params.setLastUpdated(lastUpdated);
		params.setIncludes(CollectionUtils.isEmpty(includes) ? null : includes);
		params.setSort(sort);
		
		return fhirMedicationDispenseService.searchMedicationDispenses(params);
	}
}
