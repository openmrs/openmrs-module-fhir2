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

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.sql.JoinType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPractitionerDaoImpl extends BaseFhirDao<Provider> implements FhirPractitionerDao {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		handleBooleanProperty("retired", false).ifPresent(criteria::add);
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleName(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleIdentifier(criteria, (TokenAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	private void handleName(Criteria criteria, StringAndListParam name) {
		handleAndListParam(name, param -> propertyLike("name", param)).ifPresent(criteria::add);
	}
	
	@Override
	protected void handleIdentifier(Criteria criteria, TokenAndListParam identifier) {
		handleAndListParam(identifier, param -> Optional.of(eq("identifier", param.getValue()))).ifPresent(criteria::add);
	}
	
	@Override
	public List<ProviderAttribute> getActiveAttributesByPractitionerAndAttributeTypeUuid(Provider provider,
	        String providerAttributeTypeUuid) {
		return (List<ProviderAttribute>) getSessionFactory().getCurrentSession().createCriteria(ProviderAttribute.class)
		        .createAlias("provider", "p", JoinType.INNER_JOIN, eq("p.id", provider.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", providerAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
}
