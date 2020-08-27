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

public interface EncounterReferenceTranslator<T> extends OpenmrsFhirTranslator<T, Reference> {
	
	/**
	 * Maps an {@link Encounter} to a FHIR reference
	 *
	 * @param encounter the encounter to translate
	 * @return the corresponding FHIR reference
	 */
	@Override
	Reference toFhirResource(T encounter);
	
	/**
	 * Maps a FHIR reference to an {@link Encounter}
	 *
	 * @param encounter the FHIR reference to translate
	 * @return the corresponding OpenMRS encounter
	 */
	@Override
	T toOpenmrsType(Reference encounter);
}
