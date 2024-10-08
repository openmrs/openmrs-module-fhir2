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

import java.util.Optional;

import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Base class for DAOs implementing the search for FHIR Practitioners
 *
 * @param <T>
 */
public abstract class BasePractitionerDao<T extends OpenmrsObject & Auditable> extends BasePersonDao<T> {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		criteria.createAlias("person", "p");
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleIdentifier(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteria, entry.getValue()));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteria, entry);
					break;
				case FhirConstants.PROVIDER_ROLE_SEARCH_HANDLER:
					handlerProviderRoles(criteria, (StringOrListParam) entry.getValue());
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	protected abstract void handleIdentifier(Criteria criteria, TokenAndListParam identifier);
	
	protected void handlerProviderRoles(Criteria criteria, StringOrListParam providerRole) {
		handleOrListParam(providerRole, param -> handleProviderRole(criteria, param)).ifPresent(criteria::add);
	}
	
	protected Optional<Criterion> handleProviderRole(Criteria criteria, StringParam providerRole) {
		if (lacksAlias(criteria, "pr")) {
			criteria.createAlias("providerRole", "pr");
		}
		return Optional.of(eq("pr.uuid", providerRole));
	}
	
	@Override
	protected String getSqlAlias() {
		return "p_";
	}
	
	@Override
	protected String getPersonProperty() {
		return "p";
	}
}
