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
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerReferenceTranslatorImpl extends AbstractReferenceHandlingTranslator implements PractitionerReferenceTranslator {
	
	@Inject
	private FhirPractitionerDao practitionerDao;
	
	@Override
	public Reference toFhirResource(Provider provider) {
		if (provider == null) {
			return null;
		}
		
		return createPractitionerReference(provider);
	}
	
	@Override
	public Provider toOpenmrsType(Reference practitioner) {
		if (practitioner == null) {
			return null;
		}
		
		if (!getReferenceType(practitioner).equals(FhirConstants.PRACTITIONER)) {
			throw new IllegalArgumentException("Reference must refer to a Practitioner, not a " + practitioner.getType());
		}
		
		String uuid = getReferenceId(practitioner);
		if (uuid == null) {
			return null;
		}
		
		return practitionerDao.getProviderByUuid(uuid);
	}
}
