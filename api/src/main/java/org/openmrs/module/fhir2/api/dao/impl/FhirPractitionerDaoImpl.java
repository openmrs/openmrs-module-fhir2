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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
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
import org.hibernate.sql.JoinType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPractitionerDaoImpl extends BasePractitionerDao<Provider> implements FhirPractitionerDao {
	
	private List<Predicate> predicateList = new ArrayList<>();
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected void handleIdentifier(CriteriaBuilder criteriaBuilder, TokenAndListParam identifier) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Provider> criteriaQuery = criteriaBuilder.createQuery(Provider.class);
		Root<Provider> root = criteriaQuery.from(Provider.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(identifier, param -> Optional.of(finalCriteriaBuilder.equal(root.get("identifier"), param.getValue())))
		        .ifPresent(predicateList::add);
		criteriaQuery.distinct(true).where(predicateList.toArray(new Predicate[] {}));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ProviderAttribute> getActiveAttributesByPractitionerAndAttributeTypeUuid(@Nonnull Provider provider,
	        @Nonnull String providerAttributeTypeUuid) {
		return (List<ProviderAttribute>) getSessionFactory().getCurrentSession().createCriteria(ProviderAttribute.class)
		        .createAlias("provider", "p", JoinType.INNER_JOIN, eq("p.id", provider.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", providerAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
}
