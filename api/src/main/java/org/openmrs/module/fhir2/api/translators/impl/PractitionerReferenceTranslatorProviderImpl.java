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
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PractitionerReferenceTranslatorProviderImpl implements PractitionerReferenceTranslator<Provider> {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirPractitionerDao practitionerDao;
	
	@Override
	public Reference toFhirResource(@Nonnull Provider provider) {
		if (provider == null) {
			return null;
		}
		return createPractitionerReference(provider);
	}
	
	@Override
	public Provider toOpenmrsType(@Nonnull Reference reference) {
		if (reference == null || !reference.hasReference()) {
			return null;
		}
		
		if (getReferenceType(reference).map(ref -> !ref.equals(FhirConstants.PRACTITIONER)).orElse(false)) {
			throw new IllegalArgumentException("Reference must be to an Provider not a " + getReferenceType(reference));
		}
		
		return getReferenceId(reference).map(uuid -> practitionerDao.get(uuid)).orElse(null);
	}
}
