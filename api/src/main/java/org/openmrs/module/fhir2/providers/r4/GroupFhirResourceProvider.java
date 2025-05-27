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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirGroupMemberService;
import org.openmrs.module.fhir2.api.FhirGroupService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("GroupFhirR4ResourceProvider")
@R4Provider
public class GroupFhirResourceProvider implements IResourceProvider {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirGroupService groupService;
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirGroupMemberService groupMemberService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Group.class;
	}
	
	@Read
	public Group getGroupByUuid(@IdParam @Nonnull IdType id) {
		Group group = groupService.get(id.getIdPart());
		if (group == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		return group;
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createGroup(@ResourceParam Group group) {
		return FhirProviderUtils.buildCreate(groupService.create(group));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateGroup(@IdParam IdType id, @ResourceParam Group group) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		group.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(groupService.update(id.getIdPart(), group));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteGroup(@IdParam @Nonnull IdType id) {
		groupService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Operation(name = "$members", idempotent = true)
	public IBundleProvider getGroupMembers(@IdParam @Nonnull IdType groupId) {
		Group group = groupService.get(groupId.getIdPart());
		if (group == null) {
			throw new ResourceNotFoundException("Could not find group with the id " + groupId.getIdPart());
		}
		return groupMemberService.getGroupMembers(groupId.getIdPart());
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForGroups(@OptionalParam(name = Group.SP_MANAGING_ENTITY, chainWhitelist = { "",
	        Practitioner.SP_RES_ID, Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	        Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference) {
		
		return groupService.searchForGroups(participantReference);
	}
}
