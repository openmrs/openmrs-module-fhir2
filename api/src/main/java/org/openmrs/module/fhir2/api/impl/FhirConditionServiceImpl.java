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

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.FhirException;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.springframework.stereotype.Component;

@Component
public class FhirConditionServiceImpl implements FhirConditionService {
	
	@Override
	public Condition getConditionByUuid(String uuid) {
		throw new FhirException("Please install org.openmrs.module.fhir2conditions");
	}
	
	@Override
	public Collection<Condition> searchConditions(ReferenceAndListParam patientParam, ReferenceAndListParam subjectParam,
	        TokenAndListParam code, TokenAndListParam clinicalStatus, DateRangeParam onsetDate,
	        QuantityAndListParam onsetAge, DateRangeParam recordedDate, SortSpec sort) {
		throw new FhirException("Please install org.openmrs.module.fhir2conditions");
	}
	
	@Override
	public Condition saveCondition(Condition condition) {
		throw new FhirException("Please install org.openmrs.module.fhir2conditions");
	}
}
