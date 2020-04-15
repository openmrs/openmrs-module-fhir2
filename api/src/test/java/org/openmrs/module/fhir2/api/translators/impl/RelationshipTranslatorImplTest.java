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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.RelationshipType;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class RelationshipTranslatorImplTest {
	
	private static final String RELATIONSHIP_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String B_IS_TO_A = "Child";
	
	private RelationshipTranslatorImpl relationshipTranslator;
	
	private RelationshipType relationshipType;
	
	@Before
	public void setup() {
		relationshipTranslator = new RelationshipTranslatorImpl();
		
		relationshipType = new RelationshipType();
		relationshipType.setUuid(RELATIONSHIP_TYPE_UUID);
		relationshipType.setbIsToA(B_IS_TO_A);
	}
	
	@Test
	public void shouldTranslateRelationshipTypeToCodeableConcept() {
		CodeableConcept result = relationshipTranslator.toFhirResource(relationshipType);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCodingFirstRep().getCode(), equalTo(B_IS_TO_A));
		assertThat(result.getCodingFirstRep().getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_RELATIONSHIP_TYPE));
		
	}
	
	@Test
	public void shouldTranslateToNullIfRelationshipTypeIsNull() {
		assertThat(relationshipTranslator.toFhirResource(null), nullValue());
	}
	
}
