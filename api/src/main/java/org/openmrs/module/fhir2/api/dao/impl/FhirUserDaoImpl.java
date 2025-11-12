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

import java.util.Optional;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirUserDaoImpl extends BasePractitionerDao<User> implements FhirUserDao {
	
	@Override
	@Transactional(readOnly = true)
	public User getUserByUserName(String username) {
		OpenmrsFhirCriteriaContext<User, User> criteriaContext = createCriteriaContext(User.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot())
		        .where(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("username"), username));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList().stream()
		        .findFirst().orElse(null);
	}
	
	@Override
	protected <U> void handleIdentifier(OpenmrsFhirCriteriaContext<User, U> criteriaContext, TokenAndListParam identifier) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), identifier,
		    param -> Optional.of(
		        criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("username"), param.getValue())))
		                .ifPresent(criteriaContext::addPredicate);
	}
}
