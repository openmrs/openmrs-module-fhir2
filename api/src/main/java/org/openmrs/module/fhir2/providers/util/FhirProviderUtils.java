/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.util;

import ca.uhn.fhir.rest.api.MethodOutcome;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.instance.model.api.IAnyResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FhirProviderUtils {
	
	private final static org.hl7.fhir.r4.model.CodeableConcept MSG_DELETED_R4 = new org.hl7.fhir.r4.model.CodeableConcept();
	static {
		MSG_DELETED_R4.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/operation-outcome")
		        .setCode("MSG_DELETED").setDisplay("This resource has been deleted");
		MSG_DELETED_R4.setText("This resource has been deleted");
	}
	
	private final static org.hl7.fhir.dstu3.model.CodeableConcept MSG_DELETED_R3 = new org.hl7.fhir.dstu3.model.CodeableConcept();
	static {
		MSG_DELETED_R3.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/operation-outcome")
		        .setCode("MSG_DELETED").setDisplay("This resource has been deleted");
		MSG_DELETED_R3.setText("This resource has been deleted");
	}
	
	public static MethodOutcome buildCreate(IAnyResource resource) {
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(true);
		return buildWithResource(methodOutcome, resource);
	}
	
	public static MethodOutcome buildUpdate(IAnyResource resource) {
		MethodOutcome methodOutcome = new MethodOutcome();
		methodOutcome.setCreated(false);
		return buildWithResource(methodOutcome, resource);
	}
	
	public static org.hl7.fhir.r4.model.OperationOutcome buildDelete(org.hl7.fhir.r4.model.Resource resource) {
		org.hl7.fhir.r4.model.OperationOutcome outcome = new org.hl7.fhir.r4.model.OperationOutcome();
		outcome.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.INFORMATION)
		        .setCode(org.hl7.fhir.r4.model.OperationOutcome.IssueType.INFORMATIONAL).setDetails(MSG_DELETED_R4);
		return outcome;
	}
	
	public static org.hl7.fhir.dstu3.model.OperationOutcome buildDelete(org.hl7.fhir.dstu3.model.Resource resource) {
		org.hl7.fhir.dstu3.model.OperationOutcome outcome = new org.hl7.fhir.dstu3.model.OperationOutcome();
		outcome.addIssue().setSeverity(org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.INFORMATION)
		        .setCode(org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.INFORMATIONAL).setDetails(MSG_DELETED_R3);
		return outcome;
	}
	
	private static MethodOutcome buildWithResource(MethodOutcome methodOutcome, IAnyResource resource) {
		if (resource != null) {
			methodOutcome.setResource(resource);
		}
		
		return methodOutcome;
	}
}
