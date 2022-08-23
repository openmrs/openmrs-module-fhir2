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

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ICoding;
import org.openmrs.Concept;

public interface CodingTranslator extends ToFhirTranslator<Concept, Coding>, ToOpenmrsTranslator<Concept, ICoding> {
	
	/**
	 * Maps {@link Concept} to a {@link Coding}
	 *
	 * @param concept to translate
	 * @return the corresponding FHIR {@link Coding}
	 */
	@Override
	Coding toFhirResource(@Nonnull Concept concept);
	
	/**
	 * Maps a {@link ICoding} to an OpenMRS {@link Concept}
	 *
	 * @param coding the FHIR Coding to translate
	 * @return the corresponding OpenMRS {@link Concept}
	 */
	@Override
	Concept toOpenmrsType(@Nonnull ICoding coding);
}
