/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ListResource;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class ListFhirResourceProvider implements IResourceProvider {
	
	@Inject
	private FhirListService<Cohort> cohortFhirListService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ListResource.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public ListResource getListById(@IdParam @NotNull IdType id) {
		ListResource listResource = cohortFhirListService.getListByUuid(id.getIdPart());
		if (listResource == null) {
			throw new ResourceNotFoundException("Could not find listResource with Id " + id.getIdPart());
		}
		return listResource;
	}
}
