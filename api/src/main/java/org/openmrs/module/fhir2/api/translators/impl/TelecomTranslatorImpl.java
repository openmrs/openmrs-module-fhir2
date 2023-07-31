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

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.openmrs.module.fhir2.model.FhirContactPoint;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TelecomTranslatorImpl implements TelecomTranslator<FhirContactPoint> {
	
	@Override
	public ContactPoint toFhirResource(@Nonnull FhirContactPoint openmrsContactPoint) {
		notNull(openmrsContactPoint, "The Openmrs FhirContactPoint object should not be null");
		
		ContactPoint contactPoint = new ContactPoint();
		
		contactPoint.setId(openmrsContactPoint.getUuid());
		contactPoint.setSystem(openmrsContactPoint.getSystem());
		contactPoint.setValue(openmrsContactPoint.getValue());
		contactPoint.setUse(openmrsContactPoint.getUse());
		
		return contactPoint;
	}
	
	@Override
	public FhirContactPoint toOpenmrsType(@Nonnull FhirContactPoint existingOpenmrsContactPoint,
	        @Nonnull ContactPoint contactPoint) {
		notNull(existingOpenmrsContactPoint, "The existing Openmrs FhirContactPoint object should not be null");
		notNull(contactPoint, "The ContactPoint object should not be null");
		
		if (contactPoint.hasId()) {
			existingOpenmrsContactPoint.setUuid(contactPoint.getId());
		}
		
		if (contactPoint.hasSystem()) {
			existingOpenmrsContactPoint.setSystem(contactPoint.getSystem());
		}
		
		if (contactPoint.hasValue()) {
			existingOpenmrsContactPoint.setValue(contactPoint.getValue());
		}
		
		if (contactPoint.hasUse()) {
			existingOpenmrsContactPoint.setUse(contactPoint.getUse());
		}
		
		return existingOpenmrsContactPoint;
	}
}
