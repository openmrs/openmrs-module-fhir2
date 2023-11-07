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
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Base class for DAOs implementing the search for FHIR Practitioners
 *
 * @param <T>
 */
public abstract class BasePractitionerDao<T extends OpenmrsObject & Auditable> extends BasePersonDao<T> {
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		root.join("person").alias("p");
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleIdentifier(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteriaBuilder, entry.getValue()));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaBuilder, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
	}
	
	protected abstract void handleIdentifier(CriteriaBuilder criteriaBuilder, TokenAndListParam identifier);
	
	@Override
	protected String getSqlAlias() {
		return "p_";
	}
	
	@Override
	protected String getPersonProperty() {
		return "p";
	}
}
