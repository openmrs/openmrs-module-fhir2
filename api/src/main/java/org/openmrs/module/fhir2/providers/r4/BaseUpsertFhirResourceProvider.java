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

import java.util.Map;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.IdType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;

/**
 * An instance of this resource provider base class allows upsert operations if enabled.
 */
public abstract class BaseUpsertFhirResourceProvider<T extends IAnyResource> implements IResourceProvider {
	
	@Update
	public MethodOutcome upsert(@IdParam IdType id, @ResourceParam T resource, RequestDetails requestDetails) {
		Map<Object, Object> userData = FhirProviderUtils.getUserData(requestDetails);
		userData.put(FhirConstants.USER_DATA_KEY_CREATE_IF_NOT_EXISTS, true);
		MethodOutcome outcome = doUpsert(id, resource, requestDetails);
		Object created = userData.get(FhirConstants.USER_DATA_KEY_OUTCOME_CREATED);
		if (created != null) {
			outcome.setCreated((boolean) created);
		}
		
		return outcome;
	}
	
	/**
	 * Updates the specified resource if it exists otherwise creates a new one of the resource provider
	 * supports the operation.
	 * 
	 * @param id the {@link IdType} object
	 * @param resource the resource to update
	 * @param requestDetails the RequestDetails instance
	 * @return MethodOutcome instance
	 */
	protected abstract MethodOutcome doUpsert(IdType id, T resource, RequestDetails requestDetails);
	
}
