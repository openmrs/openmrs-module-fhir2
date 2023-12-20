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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

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
import org.openmrs.EncounterType;
import org.openmrs.Obs;
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
	public List<Encounter> getSearchResults(@NonNull SearchParameterMap theParams) {
		OpenmrsFhirCriteriaContext<Encounter> criteriaContext = getSearchResultCriteria(theParams);
		handleSort(criteriaContext, theParams.getSortSpec());
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("encounterId")));
			
		criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setFirstResult(theParams.getFromIndex());
		if (theParams.getToIndex() != Integer.MAX_VALUE) {
			int maxResults = theParams.getToIndex() - theParams.getFromIndex();
			criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setMaxResults(maxResults);
		}
			
		List<Encounter> results;
		if (hasDistinctResults()) {
			results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
		} else {
			OpenmrsFhirCriteriaContext<Long> longOpenmrsFhirCriteriaContext = createCriteriaContext(Long.class);
			longOpenmrsFhirCriteriaContext.getCriteriaQuery().subquery(Long.class).select(longOpenmrsFhirCriteriaContext
					.getCriteriaBuilder().countDistinct(longOpenmrsFhirCriteriaContext.getRoot().get("id")));
				
			longOpenmrsFhirCriteriaContext.getCriteriaQuery().select(longOpenmrsFhirCriteriaContext.getRoot())
					.where(longOpenmrsFhirCriteriaContext.getCriteriaBuilder()
							.in(longOpenmrsFhirCriteriaContext.getRoot().get("id"))
							.value(longOpenmrsFhirCriteriaContext.getCriteriaQuery().subquery(Long.class)));
				
				//TODO: gonna come back to it later
				//			handleSort(projectionCriteriaBuilder, theParams.getSortSpec(), this::paramToProps).ifPresent(
				//					orders -> orders.forEach(order -> projectionList.add(Projections.property(order.getPropertyName()))));
				//			criteria.setProjection(projectionList);
				//			List<Integer> ids = new ArrayList<>();
				//			if (projectionList.getLength() > 1) {
				//				for (Object[] o : ((List<Object[]>) criteria.list())) {
				//					ids.add((Integer) o[0]);
				//				}
				//			} else {
				//				ids = criteria.list();
				//			}
				
			longOpenmrsFhirCriteriaContext.getCriteriaQuery().select(longOpenmrsFhirCriteriaContext.getRoot()).where(
						longOpenmrsFhirCriteriaContext.getCriteriaBuilder().in(longOpenmrsFhirCriteriaContext.getRoot().get("id")));
				// Need to reapply ordering
			handleSort(criteriaContext, theParams.getSortSpec());
			criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("id")));
				
			results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
			}
			return results.stream().map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		OpenmrsFhirCriteriaContext<Encounter> criteriaContext = createCriteriaContext(Encounter.class);
		
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			setupSearchParams(criteriaContext, theParams);
			
			OpenmrsFhirCriteriaContext<Object[]> objCriteriaContext = createCriteriaContext(Object[].class);
			objCriteriaContext.getCriteriaQuery().multiselect(objCriteriaContext.getRoot().get("uuid"),
			    objCriteriaContext.getRoot().get("encounterDatetime"));
			
			List<LastnResult<String>> results = objCriteriaContext.getEntityManager()
			        .createQuery(objCriteriaContext.getCriteriaQuery()).getResultList().stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		handleVoidable(criteriaContext);
		setupSearchParams(criteriaContext, theParams);
		handleSort(criteriaContext, theParams.getSortSpec());
		
		OpenmrsFhirCriteriaContext<String> context = createCriteriaContext(String.class);
		context.getCriteriaQuery().select(context.getRoot().get("uuid"));
		return context.getEntityManager().createQuery(context.getCriteriaQuery()).getResultList().stream().distinct()
		        .collect(Collectors.toList());
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected void handleDate(OpenmrsFhirCriteriaContext<Encounter> criteriaContext, DateRangeParam dateRangeParam) {
		handleDateRange(criteriaContext, "encounterDatetime", dateRangeParam).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	@Override
	protected void handleEncounterType(OpenmrsFhirCriteriaContext<Encounter> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		handleAndListParam(tokenAndListParam, t -> {
			Join<Encounter, EncounterType> et = criteriaContext.getRoot().join("encounterType");
			return Optional.of(criteriaContext.getCriteriaBuilder().equal(et.get("uuid"), t.getValue()));
		}).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	@Override
	protected void handleParticipant(OpenmrsFhirCriteriaContext<Encounter> criteriaContext,
	        ReferenceAndListParam referenceAndListParam) {
		criteriaContext.getRoot().join("encounterProviders");
		handleParticipantReference(criteriaContext, referenceAndListParam);
	}
	
	@Override
	protected <V> String paramToProp(OpenmrsFhirCriteriaContext<V> criteriaContext, @NonNull String param) {
		switch (param) {
			case SP_DATE:
				return "encounterDatetime";
			default:
				return null;
		}
	}
	
	@Override
	protected <T> Predicate generateNotCompletedOrderQuery(OpenmrsFhirCriteriaContext<T> criteriaContext,String path) {
		return criteriaContext.getCriteriaBuilder().or(criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().join(path).get("fulfillerStatus")),
				criteriaContext.getCriteriaBuilder().notEqual(criteriaContext.getRoot().join(path).get("fulfillerStatus"),Order.FulfillerStatus.COMPLETED));
	}
	
	@Override
	protected <T> Predicate generateFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T> criteriaContext, String path,
			String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder()
				.equal(criteriaContext.getRoot().join(path)
						.get("fulfillerStatus"),Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
	}
	
	@Override
	protected <T> Predicate generateNotFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<T> criteriaContext, String path,
			String fulfillerStatus) {
		return criteriaContext.getCriteriaBuilder().or(criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().join(path).get("fulfillerStatus")),
				criteriaContext.getCriteriaBuilder()
						.notEqual(criteriaContext.getRoot()
								.join(path).get("fulfillerStatus"),Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase())));
	}
}
