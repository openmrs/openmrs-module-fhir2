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

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.MedicationRequest30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("medicationRequestFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirMedicationRequestService medicationRequestService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationRequest.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public MedicationRequest getMedicationRequestById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = medicationRequestService.get(id.getIdPart());
		if (medicationRequest == null) {
			throw new ResourceNotFoundException("Could not find medicationRequest with Id " + id.getIdPart());
		}
		
		return MedicationRequest30_40.convertMedicationRequest(medicationRequest);
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForMedicationRequests(
	        @OptionalParam(name = MedicationRequest.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = MedicationRequest.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = MedicationRequest.SP_CONTEXT, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = MedicationRequest.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = MedicationRequest.SP_REQUESTER, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
	                Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	                Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
	        @OptionalParam(name = MedicationRequest.SP_MEDICATION, chainWhitelist = {
	                "" }, targetTypes = Medication.class) ReferenceAndListParam medicationReference,
	        @OptionalParam(name = MedicationRequest.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		return medicationRequestService.searchForMedicationRequests(patientReference, encounterReference, code,
		    participantReference, medicationReference, id, lastUpdated);
	}
	
	@Create
	public MethodOutcome createMedicationRequest(@ResourceParam MedicationRequest mRequest) {
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = medicationRequestService
		        .create(MedicationRequest30_40.convertMedicationRequest(mRequest));
		
		return FhirProviderUtils.buildCreate(MedicationRequest30_40.convertMedicationRequest(medicationRequest));
	}
	
	@Update
	public MethodOutcome updateMedicationRequest(@IdParam IdType id, @ResourceParam MedicationRequest mRequest) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update resource");
		}
		
		mRequest.setId(id.getIdPart());
		
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = medicationRequestService.update(id.getIdPart(),
		    MedicationRequest30_40.convertMedicationRequest(mRequest));
		
		return FhirProviderUtils.buildUpdate(MedicationRequest30_40.convertMedicationRequest(medicationRequest));
	}
	
	@Delete
	public OperationOutcome deleteMedicationRequest(@IdParam IdType id) {
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = medicationRequestService.delete(id.getIdPart());
		if (medicationRequest == null) {
			throw new ResourceNotFoundException(
			        "Could not find medication request resource with id " + id.getIdPart() + " to delete");
		}
		
		return FhirProviderUtils.buildDelete(MedicationRequest30_40.convertMedicationRequest(medicationRequest));
	}
}
