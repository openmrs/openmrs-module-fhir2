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
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class BaseProvenanceHandlingTranslatorTest {
	
	private static final String USER_UUID = "ddc25312-9798-4e6c-b8f8-269f2dd07cfd";
	
	private static final String CREATE = "CREATE";
	
	private static final String UPDATE = "UPDATE";
	
	private static final String REVISE = "revise";
	
	private static final String AGENT_TYPE_CODE = "author";
	
	private static final String AGENT_TYPE_DISPLAY = "Author";
	
	private static final String AGENT_ROLE_CODE = "AUT";
	
	private static final String AGENT_ROLE_DISPLAY = "author";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	private BaseProvenanceHandlingTranslator<?> provenanceHandlingTranslator;
	
	@Before
	@SuppressWarnings("rawtypes")
	public void setup() {
		provenanceHandlingTranslator = new BaseProvenanceHandlingTranslator() {
			
		};
		provenanceHandlingTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
	}
	
	@Test
	public void shouldCreateActivity() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.createActivity();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding(), not(empty()));
	}
	
	@Test
	public void shouldCreateActivityWithCorrectDataOperation() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.createActivity();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCodingFirstRep(), notNullValue());
		assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(CREATE));
		assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo("create"));
		assertThat(codeableConcept.getCodingFirstRep().getSystem(), equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldUpdateActivity() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.updateActivity();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding(), not(empty()));
	}
	
	@Test
	public void shouldUpdateActivityWithCorrectDataOperation() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.updateActivity();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCodingFirstRep(), notNullValue());
		assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(UPDATE));
		assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo(REVISE));
		assertThat(codeableConcept.getCodingFirstRep().getSystem(), equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldAddAgentType() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.createAgentType();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCodingFirstRep(), notNullValue());
		assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(AGENT_TYPE_CODE));
		assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo(AGENT_TYPE_DISPLAY));
		assertThat(codeableConcept.getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE));
	}
	
	@Test
	public void shouldAddAgentRole() {
		CodeableConcept codeableConcept = provenanceHandlingTranslator.addAgentRole();
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCodingFirstRep(), notNullValue());
		assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo(AGENT_ROLE_DISPLAY));
		assertThat(codeableConcept.getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
	
	@Test
	public void shouldCreateAgentComponent() {
		User user = new User();
		user.setUuid(USER_UUID);
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		
		Provenance.ProvenanceAgentComponent agentComponent = provenanceHandlingTranslator.createAgentComponent(user);
		assertThat(agentComponent, notNullValue());
		assertThat(agentComponent.getWho(), notNullValue());
		assertThat(agentComponent.getWho(), equalTo(practitionerRef));
		assertThat(agentComponent.getRoleFirstRep(), notNullValue());
		assertThat(agentComponent.getRoleFirstRep().getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(agentComponent.getRoleFirstRep().getCodingFirstRep().getDisplay(), equalTo(AGENT_ROLE_DISPLAY));
		assertThat(agentComponent.getRoleFirstRep().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
}
