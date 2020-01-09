/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.util;

import ca.uhn.fhir.rest.api.MethodOutcome;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;

@NoArgsConstructor
public class MethodOutComeUtils {
	
	private static final String UPDATE_PATTERN = "%s with id %s was successfully updated";
	
	private static final String CREATE_PATTERN = "%s was successfully created with id %s";
	
	public static MethodOutcome buildUpdate(DomainResource resource) {
		return buildWithPattern(resource, UPDATE_PATTERN);
	}
	
	public static MethodOutcome buildCreate(DomainResource resource) {
		return buildWithPattern(resource, CREATE_PATTERN);
	}
	
	private static MethodOutcome buildWithPattern(DomainResource resource, String messagePattern) {
		return buildWithResource(resource,
		    String.format(messagePattern, resource.getClass().getSimpleName(), resource.getId()));
	}
	
	private static MethodOutcome buildWithResource(DomainResource resource, String message) {
		MethodOutcome methodOutcome = new MethodOutcome();
		if (resource != null) {
			methodOutcome.setId(new IdType(resource.getClass().getSimpleName(), resource.getId()));
		}
		
		CodeableConcept concept = new CodeableConcept();
		Coding coding = concept.addCoding();
		coding.setDisplay(message);
		
		OperationOutcome outcome = new OperationOutcome();
		outcome.addIssue().setDetails(concept);
		methodOutcome.setOperationOutcome(outcome);
		return methodOutcome;
	}
	
}
