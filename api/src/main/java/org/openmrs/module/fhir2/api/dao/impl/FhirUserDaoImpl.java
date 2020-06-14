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

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirUserDaoImpl extends BaseFhirDao<User> implements FhirUserDao {
	
	@Override
	public User getUserByUuid(String uuid) {
		return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public User getUserByUserName(String username) {
		return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(eq("username", username))
		        .uniqueResult();
	}
	
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
		handleAndListParam(name, param -> propertyLike("username", param)).ifPresent(criteria::add);
	}
	
	@Override
	protected void handleIdentifier(Criteria criteria, TokenAndListParam identifier) {
		handleAndListParam(identifier, param -> Optional.of(eq("systemId", param.getValue()))).ifPresent(criteria::add);
	}
}
