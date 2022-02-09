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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirBatchService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirBatchServiceImpl extends BaseFhirService<Bundle, org.openmrs.BaseOpenmrsData> implements FhirBatchService {

	@Override
	public IBundleProvider searchBatches(TokenAndListParam identifier, TokenAndListParam batchType) {
		SearchParameterMap theParams = new SearchParameterMap()
				.addParameter(FhirConstants.OPENMRS_FHIR_EXT_BATCH_IDENTIFIER, identifier)
				.addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, batchType);

		return null;
	}

	@Override
	protected FhirDao<BaseOpenmrsData> getDao() {
		return null;
	}

	@Override
	protected OpenmrsFhirTranslator<BaseOpenmrsData, Bundle> getTranslator() {
		return null;
	}
}
