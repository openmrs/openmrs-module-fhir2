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

import static lombok.AccessLevel.PACKAGE;
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
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("procedureRequestFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class ProcedureRequestFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirServiceRequestService serviceRequestService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ProcedureRequest.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public ProcedureRequest getProcedureRequestById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.ServiceRequest serviceRequest = serviceRequestService.get(id.getIdPart());
		if (serviceRequest == null) {
			throw new ResourceNotFoundException("Could not find serviceRequest with Id " + id.getIdPart());
		}
		return (ProcedureRequest) VersionConvertorFactory_30_40.convertResource(serviceRequest);
	}
	
	public MethodOutcome createProcedureRequest(@ResourceParam ProcedureRequest procedureRequest) {
		return FhirProviderUtils.buildCreate(
				VersionConvertorFactory_30_40.convertResource(serviceRequestService.create((ServiceRequest) VersionConvertorFactory_30_40.convertResource(procedureRequest))));
	}
	
	public MethodOutcome updateProcedureRequest(@IdParam IdType id, @ResourceParam ProcedureRequest procedureRequest) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		procedureRequest.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(VersionConvertorFactory_30_40.convertResource(
		    serviceRequestService.update(id.getIdPart(), (ServiceRequest) VersionConvertorFactory_30_40.convertResource(procedureRequest))));
	}
	
	public OperationOutcome deleteProcedureRequest(@IdParam @Nonnull IdType id) {
		serviceRequestService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR3();
	}
	
	@Search
	public IBundleProvider searchForProcedureRequests(
	        @OptionalParam(name = ProcedureRequest.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = ProcedureRequest.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = ProcedureRequest.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = ProcedureRequest.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = ProcedureRequest.SP_REQUESTER, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
	                Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	                Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
	        @OptionalParam(name = ProcedureRequest.SP_OCCURRENCE) DateRangeParam occurrence,
	        @OptionalParam(name = ProcedureRequest.SP_RES_ID) TokenAndListParam uuid,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(allow = { "ProcedureRequest:" + ProcedureRequest.SP_PATIENT,
	                "ProcedureRequest:" + ProcedureRequest.SP_REQUESTER,
	                "ProcedureRequest:" + ProcedureRequest.SP_ENCOUNTER }) HashSet<Include> includes) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return new SearchQueryBundleProviderR3Wrapper(serviceRequestService.searchForServiceRequests(patientReference, code,
		    encounterReference, participantReference, occurrence, uuid, lastUpdated, includes));
	}
}
