/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Obs;

public interface DiagnosticReportTranslator extends ToFhirTranslator<Obs, DiagnosticReport> {
	
	/**
	 * Maps a {@link Obs} to a {@link DiagnosticReport}
	 *
	 * @param obsGroup the Obs group object to translate
	 * @return the corresponding FHIR DiagnosticReport
	 */
	DiagnosticReport toFhirResource(Obs obsGroup);
}
