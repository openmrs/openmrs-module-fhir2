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

import org.hl7.fhir.r4.model.Person;

public interface PersonTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.Person, Person> {
	
	/**
	 * Maps an {@link org.openmrs.Person} to a {@link org.hl7.fhir.r4.model.Person}
	 * 
	 * @param person the person to translate
	 * @return the corresponding FHIR person resource
	 */
	@Override
	Person toFhirResource(org.openmrs.Person person);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Person} to an {@link org.openmrs.Person}
	 * 
	 * @param person the FHIR person to translate
	 * @return the corresponding OpenMRS person
	 */
	@Override
	org.openmrs.Person toOpenmrsType(Person person);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Person} to an existing {@link org.openmrs.Person}
	 * 
	 * @param currentPerson the existing OpenMRS person to update
	 * @param person the FHIR person to translate
	 * @return the updated OpenMRS person
	 */
	@Override
	org.openmrs.Person toOpenmrsType(org.openmrs.Person currentPerson, Person person);
}
