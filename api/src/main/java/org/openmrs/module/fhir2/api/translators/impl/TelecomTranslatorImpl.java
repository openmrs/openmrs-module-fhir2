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
import org.openmrs.LocationAttribute;
import org.openmrs.PersonAttribute;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class TelecomTranslatorImpl implements TelecomTranslator<Object> {
	
	@Inject
	private PersonService personService;
	
	@Inject
	private LocationService locationService;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public Object toOpenmrsType(Object whichAttributeOrExistingAttribute, ContactPoint contactPoint) {
		notNull(whichAttributeOrExistingAttribute, "currentAttribute cannot be null");
		if (contactPoint == null) {
			return whichAttributeOrExistingAttribute;
		}
		if (whichAttributeOrExistingAttribute instanceof PersonAttribute) {
			PersonAttribute personAttribute = (PersonAttribute) whichAttributeOrExistingAttribute;
			personAttribute.setUuid(contactPoint.getId());
			personAttribute.setValue(contactPoint.getValue());
			personAttribute.setAttributeType(personService.getPersonAttributeTypeByUuid(globalPropertyService
			        .getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY)));
			return personAttribute;
		}
		if (whichAttributeOrExistingAttribute instanceof LocationAttribute) {
			LocationAttribute locationAttribute = (LocationAttribute) whichAttributeOrExistingAttribute;
			locationAttribute.setUuid(contactPoint.getId());
			locationAttribute.setValue(contactPoint.getValue());
			locationAttribute.setAttributeType(locationService.getLocationAttributeTypeByUuid(globalPropertyService
			        .getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY)));
			return locationAttribute;
		}
		return whichAttributeOrExistingAttribute;
	}
	
	@Override
	public ContactPoint toFhirResource(Object attribute) {
		ContactPoint contactPoint = new ContactPoint();
		if (attribute instanceof PersonAttribute) {
			PersonAttribute personAttribute = (PersonAttribute) attribute;
			contactPoint.setId(personAttribute.getUuid());
			contactPoint.setValue(personAttribute.getValue());
		}
		if (attribute instanceof LocationAttribute) {
			LocationAttribute locationAttribute = (LocationAttribute) attribute;
			contactPoint.setId(locationAttribute.getUuid());
			contactPoint.setValue(locationAttribute.getValue().toString());
		}
		
		return contactPoint;
	}
	
	/**
	 * Maps a FHIR contactPoint to an OpenMRS data element
	 * 
	 * @param contactPoint the FHIR contactPoint to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	public Object toOpenmrsType(ContactPoint contactPoint) {
		return this.toOpenmrsType(new Object(), contactPoint);
	}
}
