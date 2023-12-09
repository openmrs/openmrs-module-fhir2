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
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
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
		OpenmrsFhirCriteriaContext<Encounter> criteriaContext = createCriteriaContext();
		
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			setupSearchParams(criteriaContext, theParams);
			
			OpenmrsFhirCriteriaContext<Object[]> objCriteriaContext = createObjectCriteriaContext();
			objCriteriaContext.getCriteriaQuery().multiselect(objCriteriaContext.getRoot().get("uuid"), objCriteriaContext.getRoot().get("encounterDatetime"));
			
			List<LastnResult<String>> results = objCriteriaContext.getEntityManager().createQuery(objCriteriaContext.getCriteriaQuery()).getResultList().stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		handleVoidable(criteriaContext);
		setupSearchParams(criteriaContext, theParams);
		handleSort(criteriaContext, theParams.getSortSpec());
		
		createStringCriteriaContext().getCriteriaQuery().select(createStringCriteriaContext().getRoot().get("uuid"));
		return createStringCriteriaContext().getEntityManager().createQuery(createStringCriteriaContext().getCriteriaQuery()).getResultList().stream().distinct().collect(Collectors.toList());
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected void handleDate(OpenmrsFhirCriteriaContext<Encounter> criteriaContext, DateRangeParam dateRangeParam) {
		handleDateRange("encounterDatetime", dateRangeParam).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	@Override
	protected void handleEncounterType(OpenmrsFhirCriteriaContext<Encounter> criteriaContext, TokenAndListParam tokenAndListParam) {
		handleAndListParam(tokenAndListParam, t -> {
			Join<Encounter, EncounterType> et = criteriaContext.getRoot().join("encounterType");
			return Optional.of(criteriaContext.getCriteriaBuilder().equal(et.get("uuid"), t.getValue()));
		}).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	@Override
	protected void handleParticipant(OpenmrsFhirCriteriaContext<Encounter> criteriaContext, ReferenceAndListParam referenceAndListParam) {
		criteriaContext.getRoot().join("encounterProviders");
		handleParticipantReference(criteriaContext, referenceAndListParam);
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
		
		return (Predicate) Restrictions.eq(path + "fulfillerStatus",
		    Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
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
