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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.impl.FhirConditionDaoImpl_2_2;
import org.openmrs.module.fhir2.api.translators.impl.ConditionTranslatorImpl_2_2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirConditionServiceImpl_2_2 implements FhirConditionService {
	
	@Inject
	private FhirConditionDaoImpl_2_2 dao;
	
	@Inject
	private ConditionTranslatorImpl_2_2 conditionTranslator;
	
	@Override
	@Transactional(readOnly = true)
	public Condition getConditionByUuid(String uuid) {
		return conditionTranslator.toFhirResource(dao.getConditionByUuid(uuid));
	}
	
	@Override
	public Condition saveCondition(Condition condition) {
		return conditionTranslator.toFhirResource(dao.saveCondition(conditionTranslator.toOpenmrsType(condition)));
	}
}
