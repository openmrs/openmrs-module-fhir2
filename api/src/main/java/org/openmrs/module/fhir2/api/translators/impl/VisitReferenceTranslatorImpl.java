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

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class VisitReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements EncounterReferenceTranslator<Visit> {
	
	@Autowired
	private FhirVisitDao dao;
	
	@Override
	public Reference toFhirResource(@Nonnull Visit visit) {
		if (visit == null) {
			return null;
		}
		
		return createEncounterReference(visit);
	}
	
	@Override
	public Visit toOpenmrsType(@Nonnull Reference reference) {
		if (reference == null || !reference.hasReference()) {
			return null;
		}
		
		if (getReferenceType(reference).map(ref -> !ref.equals(FhirConstants.ENCOUNTER)).orElse(true)) {
			throw new IllegalArgumentException(
			        "Reference must be to an Encounter not a " + getReferenceType(reference).orElse(""));
		}
		
		return getReferenceId(reference).map(uuid -> dao.get(uuid)).orElse(null);
	}
}
