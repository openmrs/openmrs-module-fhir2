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
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createLocationReference;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceType;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationReferenceTranslatorImpl implements LocationReferenceTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirLocationDao locationDao;
	
	@Override
	public Reference toFhirResource(@Nonnull Location location) {
		if (location == null || location.getRetired()) {
			return null;
		}
		
		return createLocationReference(location);
	}
	
	@Override
	public Location toOpenmrsType(@Nonnull Reference locationReference) {
		if (locationReference == null || !locationReference.hasReference()) {
			return null;
		}
		
		if (getReferenceType(locationReference).map(ref -> !ref.equals(FhirConstants.LOCATION)).orElse(true)) {
			throw new IllegalArgumentException(
			        "Reference must be to a Location not a " + getReferenceType(locationReference).orElse(""));
		}
		
		return getReferenceId(locationReference).map(uuid -> locationDao.get(uuid)).orElse(null);
	}
}
