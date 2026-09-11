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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the problem-list and encounter-diagnosis backings of FHIR {@code Condition}. Which
 * one owns a write is left entirely to the handlers' {@code canHandle}; a category naming neither
 * (an unknown code in the official system, or a coding from an unrelated system) is claimed by
 * neither, and {@link BaseCompositeFhirService} rejects it as a bad request.
 */
@Component
public class FhirConditionServiceImpl extends BaseCompositeFhirService<Condition> implements FhirConditionService {
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		return doSearch(conditionSearchParams.toSearchParameterMap());
	}
}
