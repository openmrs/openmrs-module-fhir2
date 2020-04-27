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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Provenance;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(AccessLevel.PACKAGE)
public abstract class BaseProvenanceHandlingTranslator {
	
	private static final String AGENT_TYPE_CODE = "author";
	
	private static final String AGENT_TYPE_DISPLAY = "Author";
	
	private static final String AGENT_ROLE_CODE = "AUT";
	
	private static final String AGENT_ROLE_DISPLAY = "author";
	
	@Autowired
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	protected CodeableConcept createActivity() {
		Coding coding = new Coding();
		coding.setCode("CREATE");
		coding.setDisplay("create");
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		return new CodeableConcept().addCoding(coding);
	}
	
	protected CodeableConcept updateActivity() {
		Coding coding = new Coding();
		coding.setCode("UPDATE");
		coding.setDisplay("revise");
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		return new CodeableConcept().addCoding(coding);
	}
	
	protected Provenance.ProvenanceAgentComponent createAgentComponent(User user) {
		Provenance.ProvenanceAgentComponent agentComponent = new Provenance.ProvenanceAgentComponent();
		return agentComponent.setWho(practitionerReferenceTranslator.toFhirResource(user)).addRole(addAgentRole())
		        .setType(createAgentType());
	}
	
	protected CodeableConcept createAgentType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		return codeableConcept.addCoding(new Coding().setCode(AGENT_TYPE_CODE)
		        .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE).setDisplay(AGENT_TYPE_DISPLAY));
	}
	
	protected CodeableConcept addAgentRole() {
		CodeableConcept codeableConcept = new CodeableConcept();
		return codeableConcept.addCoding(new Coding().setCode(AGENT_ROLE_CODE)
		        .setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE).setDisplay(AGENT_ROLE_DISPLAY));
	}
	
}
