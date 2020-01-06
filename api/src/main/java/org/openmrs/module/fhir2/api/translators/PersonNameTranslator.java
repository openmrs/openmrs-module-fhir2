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

import org.hl7.fhir.r4.model.HumanName;
import org.openmrs.PersonName;

public interface PersonNameTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.PersonName, org.hl7.fhir.r4.model.HumanName> {
	
	/**
	 * Maps a {@link PersonName} to a {@link HumanName}
	 * 
	 * @param name the name to translate
	 * @return the corresponding FHIR name
	 */
	@Override
	HumanName toFhirResource(PersonName name);
	
	/**
	 * Maps a {@link HumanName} to a {@link PersonName}
	 * 
	 * @param name the name to translate
	 * @return the corresponding OpenMRS name
	 */
	@Override
	PersonName toOpenmrsType(HumanName name);

	/**
	 * Maps a {@link HumanName} to an existing {@link PersonName}
	 *
	 * @param personName the person name to update
	 * @param name the name to translate
	 * @return the updated person name
	 */
	@Override
	PersonName toOpenmrsType(PersonName personName, HumanName name);
}
