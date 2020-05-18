/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Person;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirPersonServiceImpl extends BaseFhirService<Person, org.openmrs.Person> implements FhirPersonService {
	
	@Autowired
	private FhirPersonDao dao;
	
	@Autowired
	private PersonTranslator translator;
	
	@Override
	@Transactional(readOnly = true)
	public Person get(String uuid) {
		return translator.toFhirResource(dao.get(uuid));
	}
	
	@Override
	public Collection<Person> searchForPeople(StringAndListParam name, TokenAndListParam gender, DateRangeParam birthDate,
	        StringAndListParam city, StringAndListParam state, StringAndListParam postalCode, StringAndListParam country,
	        SortSpec sort) {
		return dao.searchForPeople(name, gender, birthDate, city, state, postalCode, country, sort).stream()
		        .map(translator::toFhirResource).collect(Collectors.toList());
	}
	
}
