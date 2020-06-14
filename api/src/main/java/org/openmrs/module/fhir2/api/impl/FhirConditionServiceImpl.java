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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.exceptions.FhirNotImplementedException;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.springframework.stereotype.Component;

@Component
@Getter(AccessLevel.PROTECTED)
public class FhirConditionServiceImpl<U extends OpenmrsObject & Auditable> extends BaseFhirService<Condition, U> implements FhirConditionService {
	
	private static final String MESSAGE = "";
	
	@Override
	public Condition get(String uuid) {
		throw new FhirNotImplementedException(MESSAGE);
	}
	
	@Override
	protected FhirDao<U> getDao() {
		throw new FhirNotImplementedException(MESSAGE);
	}
	
	@Override
	protected OpenmrsFhirTranslator<U, Condition> getTranslator() {
		throw new FhirNotImplementedException(MESSAGE);
	}
	
	@Override
	public IBundleProvider searchConditions(ReferenceAndListParam patientParam, TokenAndListParam code,
	        TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityAndListParam onsetAge,
	        DateRangeParam recordedDate, SortSpec sort) {
		throw new FhirNotImplementedException(MESSAGE);
	}
	
	@Override
	public Condition saveCondition(Condition condition) {
		throw new FhirNotImplementedException(MESSAGE);
	}
}
