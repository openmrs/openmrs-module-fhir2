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

import java.util.List;

import org.hl7.fhir.r4.model.ListResource;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.translators.ListEntryTranslator;
import org.springframework.stereotype.Component;

@Component
public class CohortListEntryTranslatorImpl implements ListEntryTranslator<Cohort> {
	
	@Override
	public List<ListResource.ListEntryComponent> toFhirResource(@Nonnull Cohort cohort) {
		return null;
	}
	
	@Override
	public Cohort toOpenmrsType(@Nonnull Cohort existingCohort, @Nonnull List<ListResource.ListEntryComponent> resource) {
		return existingCohort;
	}
}
