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

import org.hibernate.Criteria;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirObservationDaoImpl extends BaseFhirDaoImpl<Obs> implements FhirObservationDao {
	
	@Override
	public Obs getObsByUuid(String uuid) {
		return super.get(uuid);
	}
	
	@Override
	public Obs getModelClazz() {
		return new Obs();
	}
	
	@Override
	public Criteria search(SearchParameterMap theParams) {
		return super.search(theParams);
	}
	
	@Override
	protected String paramToProp(String paramName) {
		if ("date".equals(paramName)) {
			return "obsDatetime";
		}
		
		return null;
	}
}
