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

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import org.hl7.fhir.r4.model.DateType;
import org.openmrs.Person;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.springframework.stereotype.Component;

@Component
public class BirthDateTranslatorImpl implements BirthDateTranslator {
	
	@Override
	public DateType toFhirResource(@Nonnull Person person) {
		if (person.getBirthdate() == null) {
			return null;
		}
		
		if (person.getBirthdateEstimated() != null && person.getBirthdateEstimated()) {
			DateType dateType = new DateType();
			LocalDate now = LocalDate.now();
			
			LocalDate birthDate = Instant.ofEpochMilli(person.getBirthdate().getTime()).atZone(ZoneId.systemDefault())
			        .toLocalDate();
			
			// 5 years is the cut-off for WHO and CDC infant growth charts, so it seems like a convenient break between
			// "infant" and "child"
			if (Period.between(birthDate, now).getYears() > 5) {
				dateType.setValue(person.getBirthdate(), TemporalPrecisionEnum.YEAR);
			} else {
				dateType.setValue(person.getBirthdate(), TemporalPrecisionEnum.MONTH);
			}
			
			return dateType;
		}
		
		return new DateType(person.getBirthdate());
	}
	
	@Override
	public Person toOpenmrsType(@Nonnull Person person, @Nonnull DateType date) {
		if (person == null) {
			return null;
		}
		
		if (date == null) {
			return null;
		}
		
		person.setBirthdate(date.getValue());
		
		switch (date.getPrecision()) {
			case DAY:
				person.setBirthdateEstimated(false);
				break;
			case MONTH:
			case YEAR:
				person.setBirthdateEstimated(true);
				break;
		}
		
		return person;
	}
}
