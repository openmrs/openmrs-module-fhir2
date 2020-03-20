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

import java.util.Collections;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.ListResource;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.translators.ListTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ListTranslatorImpl implements ListTranslator<Cohort> {
	
	@Override
	public ListResource toFhirResource(Cohort cohort) {
		if (cohort == null) {
			return null;
		}
		
		ListResource list = new ListResource();
		list.setId(cohort.getUuid());
		list.setMode(ListResource.ListMode.WORKING);
		list.setTitle(cohort.getName());
		
		if (cohort.getDateChanged() != null) {
			list.setDate(cohort.getDateChanged());
		} else {
			list.setDate(cohort.getDateCreated());
		}
		
		list.setNote(Collections.singletonList(new Annotation().setText(cohort.getDescription())));
		
		if (!cohort.getVoided()) {
			list.setStatus(ListResource.ListStatus.CURRENT);
		} else {
			list.setStatus(ListResource.ListStatus.RETIRED);
		}
		
		return list;
	}
	
	@Override
	public Cohort toOpenmrsType(Cohort cohort, ListResource cohortList) {
		if (cohortList == null) {
			return cohort;
		}
		
		cohort.setUuid(cohortList.getId());
		cohort.setName(cohortList.getTitle());
		
		if (cohort.getDateCreated() == null) {
			cohort.setDateCreated(cohortList.getDate());
		} else {
			cohort.setDateChanged(cohortList.getDate());
		}
		
		switch (cohortList.getStatus()) {
			case CURRENT:
				cohort.setVoided(false);
			case RETIRED:
				cohort.setVoided(true);
		}
		
		return cohort;
	}
	
}
