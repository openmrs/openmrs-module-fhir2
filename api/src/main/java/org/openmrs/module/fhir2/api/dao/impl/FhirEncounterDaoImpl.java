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

import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hl7.fhir.r4.model.Encounter.SP_DATE;
import static org.openmrs.module.fhir2.api.util.LastnOperationUtils.getTopNRankedIds;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.LastnResult;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirEncounterDaoImpl extends BaseEncounterDao<Encounter> implements FhirEncounterDao {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			EntityManager entityManager = sessionFactory.getCurrentSession();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<Encounter> encounterRoot = criteriaQuery.from(Encounter.class);
			
			setupSearchParams(criteriaBuilder, theParams);
			
			criteriaQuery.multiselect(encounterRoot.get("uuid"),encounterRoot.get("encounterDatetime"));
			
			@SuppressWarnings("unchecked")
			TypedQuery<Object[]> encounterTypedQuery = entityManager.createQuery(criteriaQuery);
			List<LastnResult<String>> results = encounterTypedQuery.getResultList().stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<Encounter> encounterRoot = criteriaQuery.from(Encounter.class);
		
		handleVoidable(criteriaBuilder,criteriaQuery,encounterRoot);
		
		setupSearchParams(criteriaBuilder, theParams);
		handleSort(criteriaBuilder, theParams.getSortSpec());
		
		criteriaQuery.multiselect(encounterRoot.get("uuid"));
		
		@SuppressWarnings("unchecked")
		TypedQuery<String> encounterTypedQuery = entityManager.createQuery(criteriaQuery);
		List<String> results = encounterTypedQuery.getResultList();
		
		return results.stream().distinct().collect(Collectors.toList());
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected void handleDate(CriteriaBuilder criteriaBuilder, DateRangeParam dateRangeParam) {
		handleDateRange("encounterDatetime", dateRangeParam).ifPresent(criteriaBuilder::and);
	}
	
	@Override
	protected void handleEncounterType(CriteriaBuilder criteriaBuilder, TokenAndListParam tokenAndListParam) {
		EntityManager manager = sessionFactory.getCurrentSession();
		criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery();
		Root<Object> root = criteriaQuery.from(Object.class);
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		
		handleAndListParam(tokenAndListParam, t -> {
			Join<?, ?> et = (Join<?, ?>) root.join("encounterType", JoinType.INNER).alias("et");
			return Optional.of(finalCriteriaBuilder.equal(et.get("uuid"), t.getValue()));
		}).ifPresent(criteriaQuery::where);
	}
	
	@Override
	protected void handleParticipant(CriteriaBuilder criteriaBuilder, ReferenceAndListParam referenceAndListParam) {
		EntityManager manager = sessionFactory.getCurrentSession();
		criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery();
		Root<Object> root = criteriaQuery.from(Object.class);
		
		root.join("encounterProviders").alias("ep");
		handleParticipantReference(criteriaBuilder, referenceAndListParam);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case SP_DATE:
				return "encounterDatetime";
			default:
				return null;
		}
	}
	
	@Override
	protected Predicate generateNotCompletedOrderQuery(String path) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return (Predicate) Restrictions.or(Restrictions.isNull(path + "fulfillerStatus"),
		    Restrictions.ne(path + "fulfillerStatus", Order.FulfillerStatus.COMPLETED));
		
	}
	
	@Override
	protected Predicate generateFulfillerStatusRestriction(String path, String fulfillerStatus) {
		
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return (Predicate) Restrictions.eq(path + "fulfillerStatus", Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
	}
	
	@Override
	protected Predicate generateNotFulfillerStatusRestriction(String path, String fulfillerStatus) {
		
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return (Predicate) Restrictions.or(Restrictions.isNull(path + "fulfillerStatus"),
		    Restrictions.ne(path + "fulfillerStatus", Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase())));
	}
}
