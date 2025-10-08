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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.IdType;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

/**
 * An instance of this base resource provider class permits upsert operations if enabled.
 */
public abstract class BaseUpsertFhirResourceProvider<T extends IAnyResource> implements IResourceProvider {
	
	protected static final String GP_NAME_SUPPORTED_RESOURCES = "fhir2.upsert.supported.resources";
	
	private static FhirGlobalPropertyService globalPropsService;
	
	@Update
	public MethodOutcome upsert(@IdParam IdType id, @ResourceParam T resource, RequestDetails requestDetails) {
		boolean createIfNotExists = getSupportedResources().contains(resource.fhirType());
		MethodOutcome outcome = doUpsert(id, resource, requestDetails, createIfNotExists);
		if (createIfNotExists) {
			Object created = requestDetails.getUserData().get(FhirConstants.USER_DATA_KEY_OUTCOME_CREATED);
			if (created != null) {
				outcome.setCreated((boolean) created);
			}
		}
		
		return outcome;
	}
	
	/**
	 * Updates the specified resource if it exists otherwise creates a new one of the resource provider
	 * supports the operation and createIfNotExists is set to true.
	 * 
	 * @param id the {@link IdType} object
	 * @param resource the resource to update
	 * @param requestDetails the RequestDetails object
	 * @param createIfNotExists specifies whether to create the resource if it does not exist.
	 * @return MethodOutcome object
	 */
	protected abstract MethodOutcome doUpsert(IdType id, T resource, RequestDetails requestDetails,
	        boolean createIfNotExists);
	
	private List<String> getSupportedResources() {
		if (globalPropsService == null) {
			globalPropsService = Context.getRegisteredComponents(FhirGlobalPropertyService.class).get(0);
		}
		
		String value = globalPropsService.getGlobalProperty(GP_NAME_SUPPORTED_RESOURCES);
		if (value != null) {
			return Arrays.stream(StringUtils.split(value, ',')).filter(v -> v.trim().length() > 0)
			        .collect(Collectors.toList());
		}
		
		return Collections.EMPTY_LIST;
	}
	
}
