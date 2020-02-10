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
import org.openmrs.Encounter;
import org.openmrs.Obs;

public interface DiagnosticReportTranslator extends OpenmrsFhirUpdatableTranslator<Obs, DiagnosticReport> {
	
	/**
	 * Maps a {@link Obs} to a {@link DiagnosticReport}
	 *
	 * @param obsGroup the Obs group object to translate
	 * @return the corresponding FHIR DiagnosticReport
	 */
	@Override
	DiagnosticReport toFhirResource(Obs obsGroup);
	
	/**
	 * Maps {@link DiagnosticReport} to {@link Obs}
	 *
	 * @param diagnosticReport the FHIR diagnostic report to translate
	 * @return the corresponding OpenMRS Obs
	 */
	@Override
	Obs toOpenmrsType(DiagnosticReport diagnosticReport);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Encounter} to an existing {@link org.openmrs.Encounter}
	 *
	 * @param existingObs the existing OBs to update
	 * @param diagnosticReport the diagnostic report to map
	 * @return an updated version of the existingObs
	 */
	@Override
	Obs toOpenmrsType(Obs existingObs, DiagnosticReport diagnosticReport);
}
