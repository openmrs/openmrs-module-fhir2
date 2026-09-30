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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Obs;

@ExtendWith(MockitoExtension.class)
public class ObservationEffectiveDatetimeTranslatorImplTest {
	
	private ObservationEffectiveDatetimeTranslatorImpl datetimeTranslator;
	
	private Obs obs;
	
	@BeforeEach
	public void setup() {
		datetimeTranslator = new ObservationEffectiveDatetimeTranslatorImpl();
		obs = new Obs();
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfCalledWithNullObject() {
		Type type = datetimeTranslator.toFhirResource(null);
		assertThat(type, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateObsDatetimeToDatetimeType() {
		obs.setObsDatetime(new Date());
		
		Type datetimeType = datetimeTranslator.toFhirResource(obs);
		assertThat(datetimeType, notNullValue());
		assertThat(((DateTimeType) datetimeType).getValue(), notNullValue());
		assertThat(((DateTimeType) datetimeType).getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldThrowExceptionIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> datetimeTranslator.toOpenmrsType(obs, null));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDatetimeTypeToObsDatetime() {
		DateTimeType dateTime = new DateTimeType();
		dateTime.setValue(new Date());
		
		Obs result = datetimeTranslator.toOpenmrsType(obs, dateTime);
		assertThat(result, notNullValue());
		assertThat(result.getObsDatetime(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInstantTypeToObsDatetime() {
		InstantType dateTime = new InstantType();
		dateTime.setValue(new Date());
		
		Obs result = datetimeTranslator.toOpenmrsType(obs, dateTime);
		assertThat(result, notNullValue());
		assertThat(result.getObsDatetime(), DateMatchers.sameDay(new Date()));
	}
	
}
