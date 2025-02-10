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

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.openmrs.ConceptNumeric;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceRangeTranslator;
import org.springframework.stereotype.Component;

@Component
public class ObservationReferenceRangeTranslatorImpl implements ObservationReferenceRangeTranslator {
	
	@Override
	public List<Observation.ObservationReferenceRangeComponent> toFhirResource(@Nonnull ConceptNumeric conceptNumeric) {
		if (conceptNumeric != null) {
			boolean allowDecimal = conceptNumeric.getAllowDecimal() != null ? conceptNumeric.getAllowDecimal() : true;
			
			List<Observation.ObservationReferenceRangeComponent> observationReferenceRangeComponentList = new ArrayList<>();
			if (conceptNumeric.getHiNormal() != null || conceptNumeric.getLowNormal() != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(conceptNumeric.getHiNormal(),
				    conceptNumeric.getLowNormal(), FhirConstants.OBSERVATION_REFERENCE_NORMAL, allowDecimal));
			}
			
			if (conceptNumeric.getHiCritical() != null || conceptNumeric.getLowCritical() != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(conceptNumeric.getHiCritical(),
				    conceptNumeric.getLowCritical(), FhirConstants.OBSERVATION_REFERENCE_TREATMENT, allowDecimal));
			}
			
			if (conceptNumeric.getHiAbsolute() != null || conceptNumeric.getLowAbsolute() != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(conceptNumeric.getHiAbsolute(),
				    conceptNumeric.getLowAbsolute(), FhirConstants.OBSERVATION_REFERENCE_ABSOLUTE, allowDecimal));
			}
			
			return observationReferenceRangeComponentList;
		} else {
			return null;
		}
	}
	
	private Observation.ObservationReferenceRangeComponent createObservationReferenceRange(Double hiValue, Double lowValue,
	        String code, boolean allowDecimal) {
		Observation.ObservationReferenceRangeComponent component = new Observation.ObservationReferenceRangeComponent();
		
		if (hiValue != null) {
			if (allowDecimal) {
				component.setHigh(new Quantity().setValue(hiValue));
			} else {
				component.setHigh(new Quantity().setValue(hiValue.longValue()));
			}
		}
		
		if (lowValue != null) {
			if (allowDecimal) {
				component.setLow(new Quantity().setValue(lowValue));
			} else {
				component.setLow(new Quantity().setValue(lowValue.longValue()));
			}
		}
		
		CodeableConcept referenceRangeType = new CodeableConcept();
		Coding coding = referenceRangeType.addCoding().setCode(code);
		
		if (FhirConstants.OBSERVATION_REFERENCE_ABSOLUTE.equals(code)) {
			coding.setSystem(FhirConstants.OPENMRS_FHIR_EXT_OBSERVATION_REFERENCE_RANGE);
		} else {
			coding.setSystem(FhirConstants.OBSERVATION_REFERENCE_RANGE_SYSTEM_URI);
		}
		
		component.setType(referenceRangeType);
		
		return component;
	}
}
