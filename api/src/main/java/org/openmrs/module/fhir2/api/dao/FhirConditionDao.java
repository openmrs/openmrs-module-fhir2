/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import javax.validation.constraints.NotNull;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;

public interface FhirConditionDao<T> {
	
	T getConditionByUuid(@NotNull String uuid);
	
	T saveCondition(@NotNull T condition);
	
	Collection<T> searchForConditions(ReferenceAndListParam patientParam, ReferenceAndListParam subjectParam,
	        TokenAndListParam code, TokenAndListParam clinicalStatus, DateRangeParam onsetDate,
	        QuantityAndListParam onsetAge, DateRangeParam recordedDate, SortSpec sort);
}
