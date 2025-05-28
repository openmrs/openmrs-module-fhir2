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
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.FhirConstants.UCUM_SYSTEM_URI;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservationValueTranslatorImpl implements ObservationValueTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Override
	public Type toFhirResource(@Nonnull Obs obs) {
		if (obs == null) {
			return null;
		}
		
		// IMPORTANT boolean values are stored as a coded value, so for this to
		// work, we must check for a boolean value before a general coded value
		Boolean valueBoolean = getValueBoolean(obs);
		if (valueBoolean != null) {
			return new BooleanType(valueBoolean);
		} else if (obs.getValueCoded() != null) {
			return conceptTranslator.toFhirResource(obs.getValueCoded());
		} else if (obs.getValueDrug() != null) {
			// TODO implement this
		} else if (obs.getValueGroupId() != null) {
			// TODO implement this
		} else if (obs.getValueDatetime() != null) {
			return new DateTimeType(obs.getValueDatetime());
		} else if (obs.getValueNumeric() != null) {
			Double value = obs.getValueNumeric();
			
			Quantity result = new Quantity();
			if (obs.getConcept() instanceof ConceptNumeric) {
				ConceptNumeric cn = (ConceptNumeric) obs.getConcept();
				if (cn.getAllowDecimal()) {
					result.setValue(value);
				} else {
					result.setValue(value.longValue());
				}
				
				result.setUnit(cn.getUnits());
				
				// only set the coding system if unit conforms to UCUM standard
				Coding coding = quantityCodingTranslator.toFhirResource(cn);
				if (coding != null && coding.hasSystem() && coding.getSystem().equals(UCUM_SYSTEM_URI)) {
					result.setCode(coding.getCode());
					result.setSystem(coding.getSystem());
				}
			} else {
				result.setValue(value);
			}
			
			return result;
		} else if (obs.getValueText() != null) {
			return new StringType(obs.getValueText());
		} else if (obs.getValueComplex() != null) {
			// TODO implement this
		}
		
		// TODO How can we implement this without relying on Context
		return new StringType(obs.getValueAsString(Context.getLocale()));
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs obs, @Nonnull Type resource) {
		notNull(obs, "The existing Obs object should not be null");
		if (resource == null) {
			return null;
		}
		
		if (resource instanceof CodeableConcept) {
			obs.setValueCoded(conceptTranslator.toOpenmrsType((CodeableConcept) resource));
		} else if (resource instanceof DateTimeType) {
			obs.setValueDate(((DateTimeType) resource).getValue());
		} else if (resource instanceof IntegerType) {
			obs.setValueNumeric(Double.valueOf(((IntegerType) resource).getValue()));
		} else if (resource instanceof Quantity) {
			obs.setValueNumeric(((Quantity) resource).getValue().doubleValue());
		} else if (resource instanceof BooleanType) {
			setValueBoolean(obs, ((BooleanType) resource).getValue());
		} else if (resource instanceof StringType) {
			obs.setValueText(((StringType) resource).getValue());
		}
		
		return obs;
	}
	
	/**
	 * @return the valueBoolean of the given obs
	 */
	protected Boolean getValueBoolean(Obs obs) {
		return obs.getValueBoolean();
	}
	
	/**
	 * sets the valueBoolean property of the given obs to the given value
	 */
	protected void setValueBoolean(Obs obs, Boolean valueBoolean) {
		obs.setValueBoolean(valueBoolean);
	}
}
