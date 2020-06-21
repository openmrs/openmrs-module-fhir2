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

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("procedureRequestFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class ProcedureRequestFhirResourceProvider implements IResourceProvider {

	@Autowired
	private FhirServiceRequestService serviceRequestService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ProcedureRequest.class;
	}

	@Read
	@SuppressWarnings("unused")
	public ProcedureRequest getProcedureRequestById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.ServiceRequest serviceRequest = serviceRequestService.get(id.getIdPart());
		if (serviceRequest == null) {
			throw new ResourceNotFoundException("Could not find serviceRequest with Id " + id.getIdPart());
		}
		return (ProcedureRequest) VersionConvertor_30_40.convertResource(serviceRequest, false);
	}

	@Search
	public IBundleProvider searchForProcedureRequests(
			@OptionalParam(name = ServiceRequest.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
					Patient.SP_GIVEN, Patient.SP_FAMILY,
					Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
			@OptionalParam(name = ServiceRequest.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
					Patient.SP_GIVEN, Patient.SP_FAMILY,
					Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
			@OptionalParam(name = ServiceRequest.SP_CODE) TokenAndListParam code,
			@OptionalParam(name = ServiceRequest.SP_ENCOUNTER, chainWhitelist = {
					"" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
			@OptionalParam(name = ServiceRequest.SP_REQUESTER, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
					Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
					Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
			@OptionalParam(name = ServiceRequest.SP_OCCURRENCE) DateRangeParam occurence) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		return serviceRequestService.searchForServiceRequests(patientReference, code, encounterReference,
				participantReference, occurence);
	}
}
