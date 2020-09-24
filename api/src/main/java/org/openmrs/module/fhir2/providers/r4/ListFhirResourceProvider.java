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

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("listFhirR4ResourceProvider")
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class ListFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirListService<Cohort, ListResource> cohortFhirListService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ListResource.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public ListResource getListById(@IdParam @Nonnull IdType id) {
		ListResource listResource = cohortFhirListService.get(id.getIdPart());
		if (listResource == null) {
			throw new ResourceNotFoundException("Could not find listResource with Id " + id.getIdPart());
		}
		return listResource;
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome creatListResource(@ResourceParam ListResource listResource) {
		return FhirProviderUtils.buildCreate(cohortFhirListService.create(listResource));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateListResource(@IdParam IdType id, @ResourceParam ListResource listResource) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		listResource.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(cohortFhirListService.update(id.getIdPart(), listResource));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteListResource(@IdParam @Nonnull IdType id) {
		ListResource listResource = cohortFhirListService.delete(id.getIdPart());
		if (listResource == null) {
			throw new ResourceNotFoundException("Could not find listJson to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(listResource);
	}
}
