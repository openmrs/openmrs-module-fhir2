/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.mockito.Mock;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

public abstract class BaseFhirProvenanceResourceTest<T extends DomainResource> {
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String AGENT_TYPE_CODE = "author";
	
	private static final String AGENT_TYPE_DISPLAY = "Author";
	
	private static final String AGENT_ROLE_CODE = "AUT";
	
	private static final String AGENT_ROLE_DISPLAY = "author";
	
	private static final String UPDATE = "Update";
	
	private static final String REVISE = "revise";
	
	private static final String CREATE = "CREATE";
	
	private static final String CREATE_DISPLAY = "create";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	public void setProvenanceResources(T resource) {
		resource.setContained(Arrays.asList(onCreateDataOperation(), onUpdateDataOperation()));
	}
	
	private Provenance onUpdateDataOperation() {
		Coding coding = new Coding();
		coding.setCode(UPDATE);
		coding.setDisplay(REVISE);
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		
		return new Provenance().setActivity(new CodeableConcept().addCoding(coding)).setRecorded(new Date())
		        .addAgent(addAgent());
	}
	
	private Provenance onCreateDataOperation() {
		Coding coding = new Coding();
		coding.setCode(CREATE);
		coding.setDisplay(CREATE_DISPLAY);
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		
		return new Provenance().setActivity(new CodeableConcept().addCoding(coding)).setRecorded(new Date())
		        .addAgent(addAgent());
	}
	
	private Provenance.ProvenanceAgentComponent addAgent() {
		User user = new User();
		user.setUuid(PRACTITIONER_UUID);
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(createPractitionerReference());
		return new Provenance.ProvenanceAgentComponent().setType(addAgentType()).addRole(addAgentRole())
		        .setWho(practitionerReferenceTranslator.toFhirResource(user));
	}
	
	private CodeableConcept addAgentType() {
		return new CodeableConcept().addCoding(new Coding().setCode(AGENT_TYPE_CODE)
		        .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE).setDisplay(AGENT_TYPE_DISPLAY));
	}
	
	private CodeableConcept addAgentRole() {
		return new CodeableConcept().addCoding(new Coding().setCode(AGENT_ROLE_CODE)
		        .setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE).setDisplay(AGENT_ROLE_DISPLAY));
	}
	
	private Reference createPractitionerReference() {
		return new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
	}
	
}
