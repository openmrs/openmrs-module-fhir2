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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.module.fhir2.FhirConstants;

public class ConditionVerificationStatusTranslatorImpl_2_2Test {
	
	private static final String CONFIRMED = "confirmed";
	
	private static final String PROVISIONAL = "provisional";
	
	private ConditionVerificationStatusTranslatorImpl_2_2 verificationStatusTranslator;
	
	@Before
	public void setup() {
		verificationStatusTranslator = new ConditionVerificationStatusTranslatorImpl_2_2();
	}
	
	@Test
	public void shouldTranslateConfirmedVerificationStatusToFhirType() {
		CodeableConcept codeableConcept = verificationStatusTranslator.toFhirResource(ConditionVerificationStatus.CONFIRMED);
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding().isEmpty(), is(false));
		assertThat(codeableConcept.getCoding().get(0).getCode().toLowerCase(), equalTo(CONFIRMED));
		assertThat(codeableConcept.getCoding().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_URI));
	}
	
	@Test
	public void shouldTranslateProvisionalVerificationStatusToFhirType() {
		CodeableConcept codeableConcept = verificationStatusTranslator
		        .toFhirResource(ConditionVerificationStatus.PROVISIONAL);
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding().isEmpty(), is(false));
		assertThat(codeableConcept.getCoding().get(0).getCode().toLowerCase(), equalTo(PROVISIONAL));
		assertThat(codeableConcept.getCoding().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_URI));
	}
	
	@Test
	public void shouldTranslateProvisionalVerificationStatusToOpenMrsType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode("confirmed").setSystem(FhirConstants.OPENMRS_URI));
		assertThat(verificationStatusTranslator.toOpenmrsType(codeableConcept),
		    equalTo(ConditionVerificationStatus.CONFIRMED));
	}
	
	@Test
	public void shouldTranslateConfirmedVerificationStatusToOpenMrsType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode("provisional").setSystem(FhirConstants.OPENMRS_URI));
		assertThat(verificationStatusTranslator.toOpenmrsType(codeableConcept),
		    equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
	
	@Test
	public void shouldReturnNullifVerificationIsNull() {
		assertThat(verificationStatusTranslator.toFhirResource(null), nullValue());
	}
	
}
