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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirUserDaoImpl extends BasePractitionerDao<User> implements FhirUserDao {
	
	@Override
	public User getUserByUserName(String username) {
		EntityManager em = getSessionFactory().getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		
		cq.select(root).where(cb.equal(root.get("username"), username));
		
		TypedQuery<User> query = em.createQuery(cq);
		return query.getResultList().stream().findFirst().orElse(null);
	}

	
	@Override
	protected void handleIdentifier(CriteriaBuilder criteriaBuilder, TokenAndListParam identifier) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
		Root<User> root = criteriaQuery.from(User.class);
		
		List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(identifier, param -> Optional.of(finalCriteriaBuilder.equal(root.get("username"), param.getValue())))
		        .ifPresent(t -> {
			        predicates.add(t);
			        criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		        });
		criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
	}
}
