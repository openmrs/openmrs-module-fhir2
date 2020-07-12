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

import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Encounter;
import org.openmrs.Visit;

public interface VisitReferenceTranslator extends OpenmrsFhirTranslator<Visit, Reference> {
	
	/**
	 * Maps an {@link Encounter} to a FHIR reference
	 *
	 * @param visit the encounter to translate
	 * @return the corresponding FHIR encounter reference
	 */
	@Override
	Reference toFhirResource(Visit visit);
	
	/**
	 * Maps a FHIR reference to an {@link Visit}
	 *
	 * @param visit the FHIR encounter reference to translate
	 * @return the corresponding OpenMRS visit
	 */
	@Override
	Visit toOpenmrsType(Reference visit);
}
