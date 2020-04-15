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

import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;

public interface PersonTelecomTranslator extends ToFhirTranslator<Person, List<ContactPoint>>, ToOpenmrsTranslator<Set<PersonAttribute>, List<ContactPoint>> {
	
	/**
	 * Maps {@link org.openmrs.PersonAttribute} to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 *
	 * @param person the OpenMRS person, from which personAttributes to translate is obtained.
	 * @return the corresponding list of {@link org.hl7.fhir.r4.model.ContactPoint} resource
	 */
	@Override
	List<ContactPoint> toFhirResource(Person person);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ContactPoint} to an {@link org.openmrs.PersonAttribute}
	 *
	 * @param contactPoints the FHIR contactPoints to translate
	 * @return the corresponding OpenMRS set of {@link org.openmrs.PersonAttribute}
	 */
	@Override
	Set<PersonAttribute> toOpenmrsType(List<ContactPoint> contactPoints);
}
