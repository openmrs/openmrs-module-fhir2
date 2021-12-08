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

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Location;

public interface LocationTypeTranslator extends ToFhirTranslator<Location, List<CodeableConcept>>, UpdatableOpenmrsTranslator<Location, List<CodeableConcept>> {
	
	/**
	 * Maps an {@link Location} to a type {@link CodeableConcept}
	 *
	 * @param omrsLocation the location to translate
	 * @return the FHIR CodeableConcept resource for the location's type
	 */
	@Override
	List<CodeableConcept> toFhirResource(@Nonnull Location omrsLocation);
	
	/**
	 * Maps an {@link Location} to a {@link Address}
	 *
	 * @param location the location resource to update
	 * @param type the location type to translate
	 * @return the updated OpenMRS location
	 */
	@Override
	Location toOpenmrsType(@Nonnull Location location, @Nonnull List<CodeableConcept> type);
}

