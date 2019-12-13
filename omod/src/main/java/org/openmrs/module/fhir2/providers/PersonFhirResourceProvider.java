/*
  This Source Code Form is subject to the terms of the Mozilla Public License,
  v. 2.0. If a copy of the MPL was not distributed with this file, You can
  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class PersonFhirResourceProvider implements IResourceProvider {
	
	@Inject
	private FhirPersonService fhirPersonService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Person.class;
	}
	
	@Read
	public Person getPersonById(@IdParam IdType id) {
		Person person = fhirPersonService.getPersonByUuid(id.getIdPart());
		if (person == null) {
			throw new ResourceNotFoundException("Could not find Person with Id " + id.getIdPart());
		}
		return person;
	}
}
