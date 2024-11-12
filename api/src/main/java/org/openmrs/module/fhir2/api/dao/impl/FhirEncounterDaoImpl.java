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

import static org.hl7.fhir.r4.model.Encounter.SP_DATE;
import static org.openmrs.module.fhir2.api.util.LastnOperationUtils.getTopNRankedIds;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
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
import lombok.NonNull;
import lombok.Setter;
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
		OpenmrsFhirCriteriaContext<Encounter, Encounter> criteriaContext = createCriteriaContext(Encounter.class);
		
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			setupSearchParams(criteriaContext, theParams);
			
			EntityManager em = sessionFactory.getCurrentSession();
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<Encounter> root = criteriaQuery.from(Encounter.class);
			
			criteriaQuery.multiselect(root.get("uuid"), root.get("encounterDatetime"));
			
			List<LastnResult<String>> results = em.createQuery(criteriaQuery).getResultList().stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		handleVoidable(criteriaContext);
		setupSearchParams(criteriaContext, theParams);
		handleSort(criteriaContext, theParams.getSortSpec());
		
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
		Root<Encounter> root = query.from(Encounter.class);
		
		query.select(root.get("uuid"));
		return em.createQuery(query).getResultList().stream().distinct().collect(Collectors.toList());
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected <U> void handleDate(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext, DateRangeParam dateRangeParam) {
		handleDateRange(criteriaContext, "encounterDatetime", dateRangeParam).ifPresent(criteriaContext::addPredicate);
	}
	
	@Override
	protected <U> void handleEncounterType(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		Join<?, ?> join = criteriaContext.addJoin("encounterType", "et");
		
		handleAndListParam(criteriaContext.getCriteriaBuilder(), tokenAndListParam,
		    t -> Optional.of(criteriaContext.getCriteriaBuilder().equal(join.get("uuid"), t.getValue())))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	@Override
	protected <U> void handleParticipant(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext,
	        ReferenceAndListParam referenceAndListParam) {
		From<?, ?> epJoin = criteriaContext.addJoin("encounterProviders", "ep");
		handleParticipantReference(criteriaContext, referenceAndListParam, epJoin);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(OpenmrsFhirCriteriaContext<V, U> criteriaContext, @NonNull String param) {
		switch (param) {
			case SP_DATE:
				return criteriaContext.getRoot().get("encounterDatetime");
			default:
				return null;
		}
	}
	
	@Override
	protected <T, U> Predicate generateNotCompletedOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String path) {
		return criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().join(path).get("fulfillerStatus")),
		    criteriaContext.getCriteriaBuilder().notEqual(criteriaContext.getRoot().join(path).get("fulfillerStatus"),
		        Order.FulfillerStatus.COMPLETED));
	}
	
	@Override
	protected <T, U> Predicate generateFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String path, String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().join(path).get("fulfillerStatus"),
		    Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
	}
	
	@Override
	protected <T, U> Predicate generateNotFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String path, String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().join(path).get("fulfillerStatus")),
		    criteriaContext.getCriteriaBuilder().notEqual(criteriaContext.getRoot().join(path).get("fulfillerStatus"),
		        Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase())));
	}
}
