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
import org.openmrs.module.fhir2.api.dao.Impl.FhirConditionDaoImpl_2_0;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class FhirConditionServiceImpl_2_0 implements FhirConditionService {
	
	@Inject
	private FhirConditionDaoImpl_2_0 dao;
	
	@Inject
	private ConditionTranslator<org.openmrs.module.emrapi.conditionslist.Condition> conditionTranslator;
	
	@Override
	public Condition getConditionByUuid(String uuid) {
		return conditionTranslator.toFhirResource(dao.getConditionByUuid(uuid));
	}
}
