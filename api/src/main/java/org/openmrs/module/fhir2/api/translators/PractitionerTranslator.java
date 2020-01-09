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

import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Provider;

public interface PractitionerTranslator extends OpenmrsFhirUpdatableTranslator<Provider, Practitioner> {
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Practitioner} to an existing {@link org.openmrs.Provider}
	 * 
	 * @param existingProvider the existingProvider to update
	 * @param practitioner the FHIR practitioner to map
	 * @return an updated version of the OpenMrs existingProvider
	 */
	@Override
	Provider toOpenmrsType(Provider existingProvider, Practitioner practitioner);
	
	/**
	 * Maps {@link org.openmrs.Provider}to a {@link org.hl7.fhir.r4.model.Practitioner} resource
	 * 
	 * @param provider the OpenMRS provider to translate
	 * @return the corresponding FHIR practitioner resource
	 */
	@Override
	Practitioner toFhirResource(Provider provider);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Practitioner} to {@link org.openmrs.Provider}
	 * 
	 * @param practitioner the FHIR practitioner to translate
	 * @return the corresponding OpenMrs provider
	 */
	@Override
	Provider toOpenmrsType(Practitioner practitioner);
}
