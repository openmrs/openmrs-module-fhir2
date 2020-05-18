/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.ListResource;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.openmrs.module.fhir2.api.dao.FhirListDao;
import org.openmrs.module.fhir2.api.translators.ListTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirCohortListServiceImpl extends BaseFhirService<ListResource, org.openmrs.Cohort> implements FhirListService<Cohort, ListResource> {
	
	@Autowired
	private FhirListDao<Cohort> dao;
	
	@Autowired
	private ListTranslator<Cohort> translator;
	
}
