/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerReferenceTranslatorUserImpl extends AbstractReferenceHandlingTranslator implements PractitionerReferenceTranslator<User> {
	
	@Inject
	private FhirUserService userService;
	
	@Override
	public Reference toFhirResource(User user) {
		if (user == null) {
			return null;
		}
		return createPractitionerReference(user);
	}
	
	@Override
	public User toOpenmrsType(Reference reference) {
		if (reference == null) {
			return null;
		}
		if (!getReferenceType(reference).equals("Practitioner")) {
			throw new IllegalArgumentException("Reference must be to an User not a " + reference.getType());
		}
		
		String uuid = getReferenceId(reference);
		if (uuid == null) {
			return null;
		}
		
		return userService.getUserByUuid(uuid);
	}
}
