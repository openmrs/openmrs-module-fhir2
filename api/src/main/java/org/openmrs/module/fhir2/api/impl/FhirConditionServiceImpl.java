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

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirConditionServiceImpl extends BaseCompositeFhirService<Condition> implements FhirConditionService {
	
	@Override
	public Condition create(@Nonnull Condition condition) {
		// FHIR R4 doesn't strictly require category, but every OpenMRS backing for Condition needs to
		// know which table to write to. Reject up front when the body either omits a recognised
		// category coding or uses the official system with an unknown code — same message the legacy
		// orchestrator surfaced. (FHIR R5 relaxed this; revisit when we move targets.)
		if (!FhirUtils.getOpenmrsConditionType(condition).isPresent()) {
			throw new InvalidRequestException(
			        "Condition.category provided must be one of problem-list-item or encounter-diagnosis");
		}
		
		return super.create(condition);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		return doSearch(conditionSearchParams.toSearchParameterMap());
	}
}
