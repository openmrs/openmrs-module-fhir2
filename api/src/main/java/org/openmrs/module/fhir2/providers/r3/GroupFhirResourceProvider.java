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
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Group30_40;
import org.hl7.fhir.dstu3.model.Group;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirGroupService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("GroupFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class GroupFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirGroupService groupService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Group.class;
	}
	
	@Read
	public Group getGroupByUuid(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Group group = groupService.get(id.getIdPart());
		if (group == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		return Group30_40.convertGroup(group);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createGroup(@ResourceParam Group group) {
		return FhirProviderUtils.buildCreate(Group30_40.convertGroup(groupService.create(Group30_40.convertGroup(group))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateGroup(@IdParam IdType id, @ResourceParam Group group) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		group.setId(id.getIdPart());
		
		return FhirProviderUtils
		        .buildUpdate(Group30_40.convertGroup(groupService.update(id.getIdPart(), Group30_40.convertGroup(group))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteGroup(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Group group = (groupService.delete(id.getIdPart()));
		if (group == null) {
			throw new ResourceNotFoundException("Could not find group to update with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(Group30_40.convertGroup(group));
	}
}
