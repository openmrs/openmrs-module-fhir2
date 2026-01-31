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

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.IdType;
import org.openmrs.module.fhir2.api.FhirFlagService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("FlagFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class FlagFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirFlagService flagService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Flag.class;
	}
	
	@Read
	public Flag getPatientById(@IdParam @Nonnull IdType id) {
		Flag flag = flagService.get(id.getIdPart());
		if (flag == null) {
			throw new ResourceNotFoundException("Could not find Flag with Id " + id.getIdPart());
		}
		return flag;
	}
	
}
