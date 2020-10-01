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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.springframework.stereotype.Component;

@Component
@Getter(AccessLevel.PROTECTED)
public class FhirConditionServiceImpl<U extends OpenmrsObject & Auditable> extends BaseFhirService<Condition, U> implements FhirConditionService {
	
	private static final String MESSAGE = "Condition is not implemented for this version of OpenMRS";
	
	@Override
	public Condition get(@Nonnull String uuid) {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	protected FhirDao<U> getDao() {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	public Condition create(@Nonnull Condition newResource) {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	public Condition update(@Nonnull String uuid, @Nonnull Condition updatedResource) {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	public Condition delete(@Nonnull String uuid) {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	protected OpenmrsFhirTranslator<U, Condition> getTranslator() {
		throw new NotImplementedOperationException(MESSAGE);
	}
	
	@Override
	public IBundleProvider searchConditions(ReferenceAndListParam patientParam, TokenAndListParam code,
	        TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityAndListParam onsetAge,
	        DateRangeParam recordedDate, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort) {
		throw new NotImplementedOperationException(MESSAGE);
	}
}
