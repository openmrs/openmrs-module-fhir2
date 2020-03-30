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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerReferenceTranslatorProviderImpl extends AbstractReferenceHandlingTranslator implements PractitionerReferenceTranslator<Provider> {
	
	@Autowired
	private FhirPractitionerService practitionerService;
	
	@Autowired
	private PractitionerTranslator<Provider> practitionerTranslator;
	
	@Override
	public Reference toFhirResource(Provider provider) {
		if (provider == null) {
			return null;
		}
		return createPractitionerReference(provider);
	}
	
	@Override
	public Provider toOpenmrsType(Reference reference) {
		if (reference == null) {
			return null;
		}
		if (!getReferenceType(reference).equals("Practitioner")) {
			throw new IllegalArgumentException("Reference must be to an Provider not a " + reference.getType());
		}
		
		String uuid = getReferenceId(reference);
		if (uuid == null) {
			return null;
		}
		return practitionerTranslator.toOpenmrsType(practitionerService.getPractitionerByUuid(uuid));
	}
}
