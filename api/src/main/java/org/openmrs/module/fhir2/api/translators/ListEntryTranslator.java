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

import java.util.List;

import org.hl7.fhir.r4.model.ListResource;

public interface ListEntryTranslator<T> extends ToFhirTranslator<T, List<ListResource.ListEntryComponent>>, UpdatableOpenmrsTranslator<T, List<ListResource.ListEntryComponent>> {
	
	/**
	 * Maps an OpenMRS data object {@link org.hl7.fhir.r4.model.ListResource.ListEntryComponent}
	 *
	 * @param data the OpenMRS object to translate
	 * @return the corresponding FHIR list entries
	 */
	@Override
	List<ListResource.ListEntryComponent> toFhirResource(@Nonnull T data);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ListResource} to an OpenMRS data object
	 *
	 * @param resource the FHIR list component to translate
	 * @param existingObject the OpenMRS object to translate to
	 * @return the corresponding OpenMRS object
	 */
	@Override
	T toOpenmrsType(@Nonnull T existingObject, @Nonnull List<ListResource.ListEntryComponent> resource);
}
