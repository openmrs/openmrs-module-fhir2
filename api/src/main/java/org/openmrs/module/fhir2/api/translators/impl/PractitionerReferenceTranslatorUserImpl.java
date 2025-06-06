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

import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createPractitionerReference;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceType;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PractitionerReferenceTranslatorUserImpl implements PractitionerReferenceTranslator<User> {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirUserDao userDao;
	
	@Override
	public Reference toFhirResource(@Nonnull User user) {
		if (user == null) {
			return null;
		}
		return createPractitionerReference(user);
	}
	
	@Override
	public User toOpenmrsType(@Nonnull Reference reference) {
		if (reference == null || !reference.hasReference()) {
			return null;
		}
		
		if (getReferenceType(reference).map(ref -> !ref.equals(FhirConstants.PRACTITIONER)).orElse(false)) {
			throw new IllegalArgumentException(
			        "Reference must be to an User not a " + getReferenceType(reference).orElse(""));
		}
		
		return getReferenceId(reference).map(uuid -> userDao.get(uuid)).orElse(null);
	}
}
