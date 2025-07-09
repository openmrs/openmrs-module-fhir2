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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.ObservationCategoryMap;
import org.openmrs.module.fhir2.api.translators.ObservationCategoryTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ObservationCategoryTranslatorImpl implements ObservationCategoryTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationCategoryMap categoryMap;
	
	@Override
	@Cacheable(value = "fhir2ObservationCategoryToCodeableConcept")
	public CodeableConcept toFhirResource(@Nonnull Concept concept) {
		if (concept == null || concept.getConceptClass() == null) {
			return null;
		}
		
		String category = categoryMap.getCategory(concept.getConceptClass().getUuid());
		
		if (category == null) {
			return null;
		}
		
		CodeableConcept result = new CodeableConcept();
		result.addCoding().setSystem(FhirConstants.OBSERVATION_CATEGORY_VALUE_SET_URI).setCode(category)
		        .setDisplay(StringUtils.capitalize(category.replace('-', ' ')));
		return result;
	}
}
