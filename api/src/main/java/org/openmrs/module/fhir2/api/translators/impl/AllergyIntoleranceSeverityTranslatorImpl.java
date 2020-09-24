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

import java.util.Map;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceSeverityTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
public class AllergyIntoleranceSeverityTranslatorImpl implements AllergyIntoleranceSeverityTranslator {
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private FhirConceptService conceptService;
	
	@Override
	public AllergyIntolerance.AllergyIntoleranceSeverity toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		Map<String, String> conceptUUIDs = getSeverityConceptUUIDs();
		if (conceptUUIDs.isEmpty()) {
			return null;
		}
		
		if (concept.getUuid().equals(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_MILD))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MILD;
		} else if (concept.getUuid().equals(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_MODERATE))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE;
		} else if (concept.getUuid().equals(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_SEVERE))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE;
		} else {
			return null;
		}
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull AllergyIntolerance.AllergyIntoleranceSeverity allergyIntoleranceSeverity) {
		if (allergyIntoleranceSeverity == null) {
			return null;
		}
		Map<String, String> conceptUUIDs = getSeverityConceptUUIDs();
		if (conceptUUIDs.isEmpty()) {
			return null;
		}
		
		switch (allergyIntoleranceSeverity) {
			case MILD:
				return conceptService.get(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_MILD));
			case MODERATE:
				return conceptService.get(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_MODERATE));
			case SEVERE:
				return conceptService.get(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_SEVERE));
			default:
				return conceptService.get(conceptUUIDs.get(FhirConstants.GLOBAL_PROPERTY_OTHER));
		}
	}
	
	private Map<String, String> getSeverityConceptUUIDs() {
		return globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
	}
}
