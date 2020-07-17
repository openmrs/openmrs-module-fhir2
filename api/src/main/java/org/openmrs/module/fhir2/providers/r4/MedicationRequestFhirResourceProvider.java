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

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
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
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("medicationRequestFhirR4ResourceProvider")
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationRequest.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public MedicationRequest getMedicationRequestByUuid(@IdParam @Nonnull IdType id) {
		MedicationRequest medicationRequest = fhirMedicationRequestService.get(id.getIdPart());
		if (medicationRequest == null) {
			throw new ResourceNotFoundException("Could not find medicationRequest with Id " + id.getIdPart());
		}
		return medicationRequest;
	}
	
	public MethodOutcome createMedicationRequest(@ResourceParam MedicationRequest mRequest) {
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = fhirMedicationRequestService.create(mRequest);
		
		return FhirProviderUtils.buildCreate(medicationRequest);
	}
	
	public MethodOutcome updateMedicationRequest(@IdParam IdType id, @ResourceParam MedicationRequest mRequest) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update resource");
		}
		
		mRequest.setId(id.getIdPart());
		
		org.hl7.fhir.r4.model.MedicationRequest medicationRequest = fhirMedicationRequestService.update(id.getIdPart(),
		    mRequest);
		
		return FhirProviderUtils.buildUpdate(medicationRequest);
	}
	
	public OperationOutcome deleteMedicationRequest(@IdParam IdType id) {
		MedicationRequest medicationRequest = fhirMedicationRequestService.delete(id.getIdPart());
		if (medicationRequest == null) {
			throw new ResourceNotFoundException(
			        "Could not find medication request resource with id " + id.getIdPart() + " to delete");
		}
		
		return FhirProviderUtils.buildDelete(medicationRequest);
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
	        @OptionalParam(name = MedicationRequest.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = MedicationRequest.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = MedicationRequest.SP_REQUESTER, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
	                Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	                Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
	        @OptionalParam(name = MedicationRequest.SP_MEDICATION, chainWhitelist = {
	                "" }, targetTypes = Medication.class) ReferenceAndListParam medicationReference,
	        @OptionalParam(name = MedicationRequest.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(allow = { "MedicationRequest:" + MedicationRequest.SP_MEDICATION,
	                "MedicationRequest:" + MedicationRequest.SP_REQUESTER,
	                "MedicationRequest:" + MedicationRequest.SP_PATIENT,
	                "MedicationRequest:" + MedicationRequest.SP_ENCOUNTER }) HashSet<Include> includes) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return fhirMedicationRequestService.searchForMedicationRequests(patientReference, encounterReference, code,
		    participantReference, medicationReference, id, lastUpdated, includes);
	}
}
