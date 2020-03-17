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

import javax.inject.Inject;

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@Transactional
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class FhirConditionServiceImpl_2_0 implements FhirConditionService {
	
	@Inject
	private FhirConditionDao<org.openmrs.module.emrapi.conditionslist.Condition> dao;
	
	@Inject
	private ConditionTranslator<org.openmrs.module.emrapi.conditionslist.Condition> conditionTranslator;
	
	@Override
	@Transactional(readOnly = true)
	public Condition getConditionByUuid(String uuid) {
		return conditionTranslator.toFhirResource(dao.getConditionByUuid(uuid));
	}
	
	@Override
	public Collection<Condition> searchConditions(ReferenceAndListParam patientParam, ReferenceAndListParam subjectParam,
	        TokenOrListParam code, TokenOrListParam clinicalStatus, DateParam onsetDate, QuantityParam onsetAge,
	        DateParam recordedDate, @Sort SortSpec sort) {
		return dao.searchForConditions(patientParam, subjectParam, code, clinicalStatus, onsetDate, onsetAge, recordedDate,
		    sort).stream().map(conditionTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Condition saveCondition(Condition condition) {
		return conditionTranslator.toFhirResource(dao.saveCondition(conditionTranslator.toOpenmrsType(condition)));
	}
}
