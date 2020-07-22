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
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("serviceRequestFhirR4ResourceProvider")
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class ServiceRequestFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirServiceRequestService serviceRequestService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ServiceRequest.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public ServiceRequest getServiceRequestById(@IdParam @NotNull IdType id) {
		ServiceRequest serviceRequest = serviceRequestService.get(id.getIdPart());
		
		if (serviceRequest == null) {
			throw new ResourceNotFoundException("Could not find Service Request with Id " + id.getIdPart());
		}
		
		return serviceRequest;
	}
	
	@Create
	public MethodOutcome createServiceRequest(@ResourceParam ServiceRequest serviceRequest) {
		return FhirProviderUtils.buildCreate(serviceRequestService.create(serviceRequest));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateServiceRequest(@IdParam IdType id, @ResourceParam ServiceRequest serviceRequest) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		serviceRequest.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(serviceRequestService.update(id.getIdPart(), serviceRequest));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteServiceRequest(@IdParam @NotNull IdType id) {
		ServiceRequest serviceRequest = serviceRequestService.delete(id.getIdPart());
		if (serviceRequest == null) {
			throw new ResourceNotFoundException("Could not find serviceRequest to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(serviceRequest);
	}
	
	@Search
	public IBundleProvider searchForServiceRequests(@OptionalParam(name = ServiceRequest.SP_RES_ID) TokenAndListParam uuid,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated) {
		return serviceRequestService.searchForServiceRequests(uuid, lastUpdated);
	}
}
