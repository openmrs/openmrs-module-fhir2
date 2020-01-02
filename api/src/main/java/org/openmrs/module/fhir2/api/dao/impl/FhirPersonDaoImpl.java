/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPersonDaoImpl implements FhirPersonDao {
	
	@Inject
	PersonService personService;
	
	@Override
	public Person getPersonByUuid(String uuid) {
		return personService.getPersonByUuid(uuid);
	}
	
	@Override
	public Collection<Person> findPersonsByName(String name) {
		return personService.getPeople(name, false);
	}
}
