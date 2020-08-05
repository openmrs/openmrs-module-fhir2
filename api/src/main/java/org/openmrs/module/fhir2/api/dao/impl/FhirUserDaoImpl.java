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

import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirUserDaoImpl extends BasePractitionerDao<User> implements FhirUserDao {
	
	@Override
	public User getUserByUserName(String username) {
		return (User) getSessionFactory().getCurrentSession().createCriteria(User.class).add(eq("username", username))
		        .uniqueResult();
	}
	
	@Override
	protected void handleIdentifier(Criteria criteria, TokenAndListParam identifier) {
		handleAndListParam(identifier, param -> Optional.of(eq("username", param.getValue()))).ifPresent(criteria::add);
	}
}
