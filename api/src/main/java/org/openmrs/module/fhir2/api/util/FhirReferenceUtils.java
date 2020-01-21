/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FhirReferenceUtils {

	
	public static Reference addPatientReference(Patient patient) {
		Reference reference = new Reference();
		String patientUri = FhirConstants.PATIENT +"/" +patient.getUuid();
		reference.setReference(patientUri);
		String nameDisplay = patient.getPersonName().getFullName()
				+ "("
				+ FhirConstants.IDENTIFIER
				+ ":"
				+ patient.getPatientIdentifier().getIdentifier()
				+ ")";
		reference.setDisplay(nameDisplay);
		return reference;
	}
	
	public static String extractUuid(@NotNull String uri) {
		if (uri == null) {
			return null;
		} else {
			return uri.contains("/") ? uri.substring(uri.indexOf("/") + 1) : uri;
		}
	}
}
