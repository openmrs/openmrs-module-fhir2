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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.NonNull;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.LastnResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirEncounterDaoImpl extends BaseEncounterDao<Encounter> implements FhirEncounterDao {
	
	@Override
	@Transactional(readOnly = true)
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			OpenmrsFhirCriteriaContext<Encounter, Object[]> criteriaContext = createCriteriaContext(Encounter.class,
			    Object[].class);
			
			setupSearchParams(criteriaContext, theParams);
			
			criteriaContext.getCriteriaQuery().multiselect(criteriaContext.getRoot().get("uuid"),
			    criteriaContext.getRoot().get("encounterDatetime"));
			
			List<LastnResult<String>> results = criteriaContext.getEntityManager()
			        .createQuery(criteriaContext.finalizeQuery()).getResultList().stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		OpenmrsFhirCriteriaContext<Encounter, String> criteriaContext = createCriteriaContext(Encounter.class, String.class);
		
		handleVoidable(criteriaContext);
		setupSearchParams(criteriaContext, theParams);
		handleSort(criteriaContext, theParams.getSortSpec());
		
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot().get("uuid"));
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList().stream()
		        .distinct().collect(Collectors.toList());
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	protected int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected <U> Optional<Predicate> handleDate(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext,
	        DateRangeParam dateRangeParam) {
		return getSearchQueryHelper().handleDateRange(criteriaContext, "encounterDatetime", dateRangeParam);
	}
	
	@Override
	protected <U> Optional<Predicate> handleEncounterType(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		if (tokenAndListParam == null || tokenAndListParam.size() == 0) {
			return Optional.empty();
		}
		
		Join<?, ?> join = criteriaContext.addJoin("encounterType", "et");
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), tokenAndListParam,
		    t -> Optional.of(criteriaContext.getCriteriaBuilder().equal(join.get("uuid"), t.getValue())));
	}
	
	@Override
	protected <U> Optional<Predicate> handleParticipant(OpenmrsFhirCriteriaContext<Encounter, U> criteriaContext,
	        ReferenceAndListParam referenceAndListParam) {
		if (referenceAndListParam == null || referenceAndListParam.size() == 0) {
			return Optional.empty();
		}
		
		From<?, ?> epJoin = criteriaContext.addJoin("encounterProviders", "ep");
		return getSearchQueryHelper().handleParticipantReference(criteriaContext, referenceAndListParam, epJoin);
	}
	
	@Override
	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	protected <T, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext, @NonNull String param) {
		switch (param) {
			case SP_DATE:
				return criteriaContext.getRoot().get("encounterDatetime");
			default:
				return null;
		}
	}
	
	@Override
	protected <T, U> Predicate generateNotCompletedOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        Join<?, ?> ordersJoin) {
		return criteriaContext.getCriteriaBuilder()
		        .or(criteriaContext.getCriteriaBuilder().isNull(ordersJoin.get("fulfillerStatus")), criteriaContext
		                .getCriteriaBuilder().notEqual(ordersJoin.get("fulfillerStatus"), Order.FulfillerStatus.COMPLETED));
	}
	
	@Override
	public <T, U> Predicate generateFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        Join<?, ?> ordersJoin, String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder().equal(ordersJoin.get("fulfillerStatus"),
		    Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
	}
	
	@Override
	protected <T, U> Predicate generateNotFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        Join<?, ?> ordersJoin, String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().isNull(ordersJoin.get("fulfillerStatus")),
		    criteriaContext.getCriteriaBuilder().notEqual(ordersJoin.get("fulfillerStatus"),
		        Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase())));
	}
}
