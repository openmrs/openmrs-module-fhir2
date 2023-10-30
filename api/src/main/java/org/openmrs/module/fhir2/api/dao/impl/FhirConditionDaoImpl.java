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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
public class FhirConditionDaoImpl extends BaseFhirDao<Condition> implements FhirConditionDao<Condition> {
	
	private List<Predicate> predicates = new ArrayList<>();
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public Condition get(@Nonnull String uuid) {
		return super.get(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_CONDITIONS)
	public Condition createOrUpdate(@Nonnull Condition newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_CONDITIONS)
	public Condition delete(@Nonnull String uuid) {
		return super.delete(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public List<Condition> getSearchResults(@Nonnull SearchParameterMap theParams) {
		return super.getSearchResults(theParams);
	}
	
	private ConditionClinicalStatus convertStatus(String status) {
		if ("active".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.ACTIVE;
		}
		return ConditionClinicalStatus.INACTIVE;
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Condition> criteriaQuery = criteriaBuilder.createQuery(Condition.class);
		Root<Condition> root = criteriaQuery.from(Condition.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(finalCriteriaBuilder, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCode(finalCriteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
					entry.getValue()
					        .forEach(param -> handleClinicalStatus(finalCriteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDateRange(param.getPropertyName(), (DateRangeParam) param.getParam())
					                .ifPresent(predicates::add));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleOnsetAge(finalCriteriaBuilder, (QuantityAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicates::add);
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	private void handleCode(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Condition> criteriaQuery = criteriaBuilder.createQuery(Condition.class);
		Root<Condition> root = criteriaQuery.from(Condition.class);
		
		if (code != null) {
			root.join("condition.coded").alias("cd");
			handleCodeableConcept(criteriaBuilder, code, "cd", "map", "term").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleClinicalStatus(CriteriaBuilder criteriaBuilder, TokenAndListParam status) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Condition> criteriaQuery = criteriaBuilder.createQuery(Condition.class);
		Root<Condition> root = criteriaQuery.from(Condition.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(status,
		    tokenParam -> Optional
		            .of(finalCriteriaBuilder.equal(root.get("clinicalStatus"), convertStatus(tokenParam.getValue()))))
		                    .ifPresent(predicates::add);
		criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
	}
	
	private void handleOnsetAge(CriteriaBuilder criteriaBuilder, QuantityAndListParam onsetAge) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Condition> criteriaQuery = criteriaBuilder.createQuery(Condition.class);
		
		handleAndListParam(onsetAge, onsetAgeParam -> handleAgeByDateProperty("onsetDate", onsetAgeParam))
		        .ifPresent(predicates::add);
		criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
	}
	
	@Override
	protected Optional<Predicate> handleLastUpdated(DateRangeParam param) {
		return super.handleLastUpdatedImmutable(param);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Condition.SP_ONSET_DATE:
				return "onsetDate";
			case org.hl7.fhir.r4.model.Condition.SP_RECORDED_DATE:
				return "dateCreated";
		}
		
		return super.paramToProp(param);
	}
}
