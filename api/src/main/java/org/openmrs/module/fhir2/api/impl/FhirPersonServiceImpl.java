/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Person;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPersonServiceImpl implements FhirPersonService {
	
	@Inject
	private FhirPersonDao fhirPersonDao;
	
	@Inject
	private PersonTranslator personTranslator;
	
	@Override
	public Person getPersonByUuid(String uuid) {
		return personTranslator.toFhirResource(fhirPersonDao.getPersonByUuid(uuid));
	}
	
	@Override
	public Collection<Person> findPersonsByName(String name) {
		return fhirPersonDao.findPersonsByName(name)
				.stream()
				.map(personTranslator::toFhirResource)
				.collect(Collectors.toList());
	}
	
	@Override
	public Collection<Person> findPersonsByBirthDate(Date birthDate) {
		return fhirPersonDao.findPersonsByBirthDate(birthDate)
				.stream()
				.map(personTranslator::toFhirResource)
				.collect(Collectors.toList());
	}
	
	@Override
	public Collection<Person> findSimilarPeople(String name, Integer birthYear, String gender) {
		return fhirPersonDao.findSimilarPeople(name, birthYear, gender)
				.stream()
				.map(personTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Collection<Person> findPersonsByGender(String gender) {
		return fhirPersonDao.findPersonsByGender(gender)
				.stream()
				.map(personTranslator::toFhirResource)
				.collect(Collectors.toList());
	}
}
