/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.instance.model.api.IAnyResource;

public interface FhirService<T extends IAnyResource> {
	
	T get(@Nonnull String uuid);
	
	List<T> get(@Nonnull Collection<String> uuids);
	
	T create(@Nonnull T newResource);
	
	T update(@Nonnull String uuid, @Nonnull T updatedResource);
	
	/**
	 * Updates the specified resource if it exists otherwise creates a new one of the resource provider
	 * supports the operation and createIfNotExists is set to true.
	 * 
	 * @param uuid the unique identifier of the resource
	 * @param updatedResource the resource to update
	 * @param requestDetails {@link RequestDetails} object
	 * @param createIfNotExists specifies whether to create the resource if it does not exist.
	 * @return the updated or created resource
	 */
	T update(@Nonnull String uuid, @Nonnull T updatedResource, RequestDetails requestDetails, boolean createIfNotExists);
	
	T patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body, RequestDetails requestDetails);
	
	void delete(@Nonnull String uuid);
}
