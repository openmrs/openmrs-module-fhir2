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
import org.openmrs.BaseOpenmrsData;
import org.openmrs.LocationAttribute;
import org.openmrs.PersonAttribute;
import org.openmrs.ProviderAttribute;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TelecomTranslatorImpl implements TelecomTranslator<BaseOpenmrsData> {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private ProviderService providerService;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public BaseOpenmrsData toOpenmrsType(BaseOpenmrsData attribute, ContactPoint contactPoint) {
		if (attribute == null || contactPoint == null) {
			return attribute;
		}
		
		if (attribute instanceof PersonAttribute) {
			PersonAttribute personAttribute = (PersonAttribute) attribute;
			personAttribute.setUuid(contactPoint.getId());
			personAttribute.setValue(contactPoint.getValue());
			personAttribute.setAttributeType(personService.getPersonAttributeTypeByUuid(
			    globalPropertyService.getGlobalProperty(FhirConstants.PERSON_CONTACT_ATTRIBUTE_TYPE)));
		} else if (attribute instanceof LocationAttribute) {
			LocationAttribute locationAttribute = (LocationAttribute) attribute;
			locationAttribute.setUuid(contactPoint.getId());
			locationAttribute.setValue(contactPoint.getValue());
			locationAttribute.setAttributeType(locationService.getLocationAttributeTypeByUuid(
			    globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_CONTACT_ATTRIBUTE_TYPE)));
		} else if (attribute instanceof ProviderAttribute) {
			ProviderAttribute providerAttribute = (ProviderAttribute) attribute;
			providerAttribute.setUuid(contactPoint.getId());
			providerAttribute.setValue(contactPoint.getValue());
			providerAttribute.setAttributeType(providerService.getProviderAttributeTypeByUuid(
			    globalPropertyService.getGlobalProperty(FhirConstants.PROVIDER_CONTACT_ATTRIBUTE_TYPE)));
		}
		
		return attribute;
	}
	
	@Override
	public ContactPoint toFhirResource(BaseOpenmrsData attribute) {
		if (attribute == null) {
			return null;
		}
		
		ContactPoint contactPoint = new ContactPoint();
		
		if (attribute instanceof PersonAttribute) {
			PersonAttribute personAttribute = (PersonAttribute) attribute;
			contactPoint.setId(personAttribute.getUuid());
			contactPoint.setValue(personAttribute.getValue());
		} else if (attribute instanceof LocationAttribute) {
			LocationAttribute locationAttribute = (LocationAttribute) attribute;
			contactPoint.setId(locationAttribute.getUuid());
			contactPoint.setValue(locationAttribute.getValue().toString());
		} else if (attribute instanceof ProviderAttribute) {
			ProviderAttribute providerAttribute = (ProviderAttribute) attribute;
			contactPoint.setId(providerAttribute.getUuid());
			contactPoint.setValue(providerAttribute.getValue().toString());
		}
		
		return contactPoint;
	}
}
