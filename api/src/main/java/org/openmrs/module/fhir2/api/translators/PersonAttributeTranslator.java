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

import org.hl7.fhir.r4.model.Extension;
import org.openmrs.PersonAttribute;

public interface PersonAttributeTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.PersonAttribute, Extension> {
	
	/**
	 * Maps a {@link PersonAttribute} to a {@link Extension}
	 *
	 * @param personAttribute the attribute to translate in extension
	 * @return the corresponding FHIR Extension
	 */
	@Override
	Extension toFhirResource(@Nonnull PersonAttribute personAttribute);
	
	/**
	 * Maps a {@link Extension} to a {@link PersonAttribute}
	 *
	 * @param extension the extension with attribute information
	 * @return the corresponding Person Attribute
	 */
	@Override
	PersonAttribute toOpenmrsType(@Nonnull Extension extension);
	
	/**
	 * Maps a {@link Extension} to an existing {@link PersonAttribute}
	 *
	 * @param personAttribute the attribute to update
	 * @param extension the extension with attribute information
	 * @return the updated Person Attribute
	 */
	@Override
	PersonAttribute toOpenmrsType(@Nonnull PersonAttribute personAttribute, @Nonnull Extension extension);
}
