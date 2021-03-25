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
import org.hl7.fhir.convertors.conv30_40.Flag30_40;
import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirFlagService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("FlagFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class FlagFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirFlagService flagService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Flag.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Flag getFlagByUuid(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Flag flag = flagService.get(id.getIdPart());
		if (flag == null) {
			throw new ResourceNotFoundException("Could not find Flag with Id " + id.getIdPart());
		}
		return Flag30_40.convertFlag(flag);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createFlag(@ResourceParam Flag flag) {
		return FhirProviderUtils.buildCreate(flagService.create(Flag30_40.convertFlag(flag)));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateFlag(@IdParam IdType id, @ResourceParam Flag flag) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		flag.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(flagService.update(id.getIdPart(), Flag30_40.convertFlag(flag)));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteFlag(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Flag flag = flagService.delete(id.getIdPart());
		if (flag == null) {
			throw new ResourceNotFoundException("Could not find flag to update with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(Flag30_40.convertFlag(flag));
	}
}
