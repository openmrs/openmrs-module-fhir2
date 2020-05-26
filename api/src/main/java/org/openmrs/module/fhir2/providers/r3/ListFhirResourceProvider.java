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
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.List30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("listFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class ListFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirListService<Cohort, org.hl7.fhir.r4.model.ListResource> listService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ListResource.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public ListResource getListById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.ListResource listResource = listService.get(id.getIdPart());
		if (listResource == null) {
			throw new ResourceNotFoundException("Could not find list with Id " + id.getIdPart());
		}
		
		return List30_40.convertList(listResource);
	}
}
