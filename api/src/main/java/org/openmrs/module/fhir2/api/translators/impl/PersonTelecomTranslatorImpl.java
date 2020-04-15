/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.PersonTelecomTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PersonTelecomTranslatorImpl implements PersonTelecomTranslator {
	
	@Autowired
	private TelecomTranslator<Object> telecomTranslator;
	
	@Autowired
	private FhirPersonDao fhirPersonDao;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.PersonTelecomTranslator#toFhirResource(org.openmrs.Person)
	 */
	@Override
	public List<ContactPoint> toFhirResource(Person person) {
		return fhirPersonDao
		        .getActiveAttributesByPersonAndAttributeTypeUuid(person,
		            globalPropertyService.getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY))
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.PersonTelecomTranslator#toOpenmrsType(java.util.List)
	 */
	@Override
	public Set<PersonAttribute> toOpenmrsType(List<ContactPoint> contactPoints) {
		return contactPoints.stream()
		        .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
		        .collect(Collectors.toSet());
	}
}
