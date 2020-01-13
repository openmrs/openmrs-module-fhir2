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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class TelecomTranslatorImpl implements TelecomTranslator {
	
	@Inject
	private PersonService personService;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public PersonAttribute toOpenmrsType(PersonAttribute currentPersonAttribute, ContactPoint contactPoint) {
		notNull(currentPersonAttribute, "currentPersonAttribute cannot be null");
		if (contactPoint == null) {
			return currentPersonAttribute;
		}
		currentPersonAttribute.setUuid(contactPoint.getId());
		currentPersonAttribute.setValue(contactPoint.getValue());
		return currentPersonAttribute;
	}
	
	@Override
	public ContactPoint toFhirResource(PersonAttribute personAttribute) {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(personAttribute.getUuid());
		contactPoint.setValue(personAttribute.getValue());
		return contactPoint;
	}
	
	@Override
	public PersonAttribute toOpenmrsType(ContactPoint contactPoint) {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(contactPoint.getId());
		personAttribute.setValue(contactPoint.getValue());
		personAttribute.setAttributeType(personService.getPersonAttributeTypeByUuid(globalPropertyService
		        .getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY)));
		return personAttribute;
	}
}
