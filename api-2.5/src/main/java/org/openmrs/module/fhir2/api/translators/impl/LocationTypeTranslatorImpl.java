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

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTypeTranslatorImpl implements LocationTypeTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull Location location) {
		CodeableConcept type = null;
		
		if (location.getType() != null) {
			type = conceptTranslator.toFhirResource(location.getType());
		}
		
		return type;
	}
	
	@Override
	public Location toOpenmrsType(@Nonnull Location location, @Nonnull CodeableConcept type) {
		location.setType(conceptTranslator.toOpenmrsType(type));
		return location;
	}
}
