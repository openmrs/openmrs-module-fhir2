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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;

@RunWith(MockitoJUnitRunner.class)
public class VisitReferenceTranslatorImplTest {
	
	private static final String VISIT_UUID = "276379ef-07ce-4108-b5e0-c4dc21964b4f";
	
	@Mock
	private FhirVisitDao dao;
	
	private VisitReferenceTranslatorImpl visitReferenceTranslator;
	
	@Before
	public void setup() {
		visitReferenceTranslator = new VisitReferenceTranslatorImpl();
		visitReferenceTranslator.setDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertVisitToReference() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		Reference result = visitReferenceTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(visitReferenceTranslator.getReferenceId(result).orElse(null), equalTo(VISIT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfVisitNull() {
		Reference result = visitReferenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToVisit() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + VISIT_UUID)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier().setValue(VISIT_UUID));
		
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		when(dao.get(VISIT_UUID)).thenReturn(visit);
		Visit result = visitReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		Visit result = visitReferenceTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterHasNoIdentifier() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + VISIT_UUID)
		        .setType(FhirConstants.ENCOUNTER);
		
		Visit result = visitReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterIdentifierHasNoValue() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + VISIT_UUID)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier());
		
		Visit result = visitReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsNotForEncounter() {
		Reference reference = new Reference().setReference("Unknown" + "/" + VISIT_UUID).setType("Unknown");
		
		visitReferenceTranslator.toOpenmrsType(reference);
	}
	
}
