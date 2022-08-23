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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;

public interface ValueSetTranslator extends OpenmrsFhirTranslator<Concept, ValueSet> {
	
	/**
	 * Not implemented for now
	 *
	 * @param resource the FHIR resource to translate
	 * @return null
	 */
	@Override
	Concept toOpenmrsType(@Nonnull ValueSet resource);
	
	/**
	 * Maps a set of concepts to a {@link ValueSet}
	 *
	 * @param concept the root concept of conceptSet
	 * @return the corresponding valueSet
	 */
	@Override
	ValueSet toFhirResource(@Nonnull Concept concept);
}
