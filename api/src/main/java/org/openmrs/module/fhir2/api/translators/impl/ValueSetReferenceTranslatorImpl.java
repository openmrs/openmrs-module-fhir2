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

import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createValueSetReference;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.ValueSetReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ValueSetReferenceTranslatorImpl implements ValueSetReferenceTranslator {
	
	@Override
	public Reference toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		return createValueSetReference(concept);
	}
}
