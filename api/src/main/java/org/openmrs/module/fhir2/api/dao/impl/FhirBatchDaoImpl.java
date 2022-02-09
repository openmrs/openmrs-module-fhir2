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

import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hibernate.Criteria;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirBatchDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirBatch;

import java.util.Optional;

import static org.hibernate.criterion.Restrictions.eq;

public class FhirBatchDaoImpl extends BaseFhirDao<FhirBatch> implements FhirBatchDao {


	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.OPENMRS_FHIR_EXT_BATCH_IDENTIFIER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
				case FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
							.forEach(param -> handleAndListParam((TokenAndListParam) param.getParam(),
									t -> Optional.of(eq("et.uuid", t.getValue())))
									.ifPresent(t -> criteria.createAlias("encounterType", "et").add(t)));
					break;
			}
		});
	}

}
