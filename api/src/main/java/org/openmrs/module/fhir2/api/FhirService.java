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

import javax.validation.constraints.NotNull;

import org.hl7.fhir.instance.model.api.IAnyResource;

public interface FhirService<T extends IAnyResource> {
	
	T get(@NotNull String uuid);
	
	T create(T newResource);
	
	T update(String uuid, T updatedResource);
	
	T delete(@NotNull String uuid);
}
