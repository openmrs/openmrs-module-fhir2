/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hibernate.criterion.Restrictions.eq;
import static org.openmrs.module.fhir2.FhirConstants.TITLE_SEARCH_HANDLER;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.*;
import org.openmrs.api.FormService;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirQuestionnaireDaoImpl extends BaseFhirDao<Form> implements FhirQuestionnaireDao {
	
	@Autowired
	private FormService formService;
	
	@Override
	public Form get(@Nonnull String uuid) {
		return formService.getFormByUuid(uuid);
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		criteria.add(eq("set", true));
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(criteria, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleTitle(Criteria criteria, StringAndListParam titlePattern) {
		criteria.createAlias("names", "cn");
		handleAndListParam(titlePattern, (title) -> propertyLike("cn.name", title)).ifPresent(criteria::add);
	}
	
}
