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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.translators.VisitPeriodTranslator;

@RunWith(MockitoJUnitRunner.class)
public class VisitPeriodTranslatorImplTest extends TestCase {
	
	VisitPeriodTranslator visitPeriodTranslator;
	
	Date periodStart, periodEnd;
	
	@SneakyThrows
	@Before
	public void setup() {
		visitPeriodTranslator = new VisitPeriodTranslatorImpl();
		
		periodStart = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("10-Jan-2019 10:11:00");
		periodEnd = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse("10-Jan-2019 11:00:00");
	}
	
	@Test
	public void toFhirResource_shouldMapPairOfDatesToPeriod() {
		ImmutablePair pair = new ImmutablePair(periodStart, periodEnd);
		
		Period result = visitPeriodTranslator.toFhirResource(pair);
		
		assertThat(result, notNullValue());
		assertThat(result.getStart(), notNullValue());
		assertThat(result.getEnd(), notNullValue());
		assertThat(result.getStart(), equalTo(periodStart));
		assertThat(result.getEnd(), equalTo(periodEnd));
	}
	
	@Test
	public void toOpenmrsObject_shouldMapPeriodToPairOfDates() {
		Encounter fhirEncounter = new Encounter();
		
		Period period = new Period();
		period.setStart(periodStart);
		period.setEnd(periodEnd);
		
		fhirEncounter.setPeriod(period);
		
		ImmutablePair result = visitPeriodTranslator.toOpenmrsType(period);
		
		assertThat(result, notNullValue());
		assertThat(result.getKey(), notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getKey(), equalTo(periodStart));
		assertThat(result.getValue(), equalTo(periodEnd));
	}
	
}
