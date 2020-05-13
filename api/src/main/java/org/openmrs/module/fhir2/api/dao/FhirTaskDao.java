/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import javax.validation.constraints.NotNull;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.DomainResource;
import org.openmrs.module.fhir2.FhirTask;

public interface FhirTaskDao {
	
	FhirTask saveTask(FhirTask task);
	
	FhirTask getTaskByUuid(@NotNull String taskUUID);
	
	Collection<FhirTask> getTasksByBasedOnUuid(Class<? extends DomainResource> resourceType, String uuid);
	
	Collection<FhirTask> searchForTasks(ReferenceParam basedOnReference, ReferenceParam ownerReference,
	        TokenAndListParam status, SortSpec sort);
}
