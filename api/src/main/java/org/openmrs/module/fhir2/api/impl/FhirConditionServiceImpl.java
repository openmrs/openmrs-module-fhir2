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
 * Orchestrates the problem-list and encounter-diagnosis backings of FHIR {@code Condition}. Unless
 * {@code meta.profile} names a backing, a new {@code Condition} goes to whichever handler's
 * {@code canHandle} accepts its category. A category naming neither (an unknown code in the
 * official system, or a coding from an unrelated system), or naming both, is rejected as a bad
 * request.
 * <p>
 * {@code category} is a standard search parameter that the backings' DAOs do not apply, so a search
 * routed by {@code _profile} is still filtered by it.
 */
@Component
public class FhirConditionServiceImpl extends BaseCompositeFhirService<Condition> implements FhirConditionService {
	
	@Override
	protected boolean profileRoutedSearchConsultsAcceptsSearch() {
		return true;
	}
	
	@Override
	protected boolean resolvesOverlappingClaimsByOrder() {
		return false;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		return doSearch(conditionSearchParams.toSearchParameterMap());
	}
}
