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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.mappings.ObservationCategoryMap;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirObservationDaoImpl extends BaseFhirDao<Obs> implements FhirObservationDao {
	
	@Autowired
	private ObservationCategoryMap categoryMap;
	
	@Autowired
	private FhirEncounterDao encounterDao;
	
	private final List<Predicate> predicates = new ArrayList<>();
	
	@Override
	public List<Obs> getSearchResults(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			EntityManager em = getSessionFactory().getCurrentSession();
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Obs> cq = cb.createQuery(Obs.class);
			Root<Obs> root = cq.from(Obs.class);
			
			setupSearchParams(cb, theParams);
			
			cq.orderBy(cb.asc(root.get("concept")), cb.desc(root.get("obsDatetime")));
			
			List<Obs> results = new ArrayList<>();
			int firstResult = 0;
			final int maxGroupCount = getMaxParameter(theParams);
			final int batchSize = 100;
			Concept prevConcept = null;
			Date prevObsDatetime = null;
			int groupCount = maxGroupCount;
			
			while (results.size() < theParams.getToIndex()) {
				TypedQuery<Obs> query = em.createQuery(cq);
				query.setFirstResult(firstResult);
				query.setMaxResults(batchSize);
				List<Obs> observations = query.getResultList();
				
				for (Obs obs : observations) {
					if (prevConcept == obs.getConcept()) {
						if (groupCount > 0 || obs.getObsDatetime().equals(prevObsDatetime)) {
							// Load only as many results as requested per group or more if time matches
							if (!obs.getObsDatetime().equals(prevObsDatetime)) {
								groupCount--;
							}
							prevObsDatetime = obs.getObsDatetime();
							results.add(obs);
						}
					} else {
						prevConcept = obs.getConcept();
						prevObsDatetime = obs.getObsDatetime();
						groupCount = maxGroupCount;
						results.add(obs);
						groupCount--;
					}
					
					if (results.size() >= theParams.getToIndex()) {
						// Load only as many results as requested per page
						break;
					}
				}
				
				if (observations.size() < batchSize) {
					break;
				} else {
					firstResult += batchSize;
				}
			}
			
			int toIndex = Math.min(results.size(), theParams.getToIndex());
			return results.subList(theParams.getFromIndex(), toIndex).stream().map(this::deproxyResult)
					.collect(Collectors.toList());
		}
		
		return super.getSearchResults(theParams);
	}
	
	@Override
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = criteriaBuilder.createQuery(Object[].class);
		Root<Object[]> root = cq.from(Object[].class);
		TypedQuery<Object[]> tq = em.createQuery(cq);
		
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			setupSearchParams(criteriaBuilder, theParams);
			cq.orderBy(criteriaBuilder.asc(root.get("concept")))
			        .orderBy(criteriaBuilder.desc(root.get("obsDatetime")));
			
			cq.multiselect(root.get("concept.id"), root.get("obsDatetime"), criteriaBuilder.count(root));
			
			applyExactTotal(criteriaBuilder, theParams);
			List<Object[]> rows = tq.getResultList();
			final int maxGroupCount = getMaxParameter(theParams);
			int groupCount = maxGroupCount;
			int count = 0;
			Integer prevConceptId = null;
			for (Object[] row : rows) {
				Integer conceptId = (Integer) row[0];
				Long rowCount = (Long) row[2];
				if (!conceptId.equals(prevConceptId)) {
					groupCount = maxGroupCount;
				}
				if (groupCount > 0) {
					count += rowCount;
					groupCount--;
				}
				prevConceptId = conceptId;
			}
			
			return count;
		}
		return super.getSearchResultsCount(theParams);
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Obs> criteriaQuery = criteriaBuilder.createQuery(Obs.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);
		
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			ReferenceAndListParam encountersReferences = new ReferenceAndListParam();
			ReferenceOrListParam referenceOrListParam = new ReferenceOrListParam();
			
			List<String> encounters = encounterDao.getSearchResultUuids(theParams);
			
			encounters.forEach(encounter -> referenceOrListParam.addOr(new ReferenceParam().setValue(encounter)));
			encountersReferences.addAnd(referenceOrListParam);
			
			theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encountersReferences);
		}
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    p -> handleEncounterReference(finalCriteriaBuilder, (ReferenceAndListParam) p.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) patientReference.getParam(), "person"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(code -> handleCodedConcept(finalCriteriaBuilder, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    category -> handleConceptClass(finalCriteriaBuilder, (TokenAndListParam) category.getParam()));
					break;
				case FhirConstants.VALUE_CODED_SEARCH_HANDLER:
					entry.getValue().forEach(
					    valueCoded -> handleValueCodedConcept(finalCriteriaBuilder, (TokenAndListParam) valueCoded.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(finalCriteriaBuilder::and));
					break;
				case FhirConstants.HAS_MEMBER_SEARCH_HANDLER:
					entry.getValue().forEach(hasMemberReference -> handleHasMemberReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) hasMemberReference.getParam()));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    quantity -> handleQuantity(quantity.getPropertyName(), (QuantityAndListParam) quantity.getParam())
					            .ifPresent(predicates::add));
					criteriaQuery.where(predicates.toArray(new Predicate[] {}));
					break;
				case FhirConstants.VALUE_STRING_SEARCH_HANDLER:
					entry.getValue().forEach(
					    string -> handleValueStringParam(string.getPropertyName(), (StringAndListParam) string.getParam())
					            .ifPresent(predicates::add));
					criteriaQuery.where(predicates.toArray(new Predicate[] {}));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicates::add);
					criteriaQuery.where(predicates.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	private void handleHasMemberReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam hasMemberReference) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Obs> criteriaQuery = criteriaBuilder.createQuery(Obs.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);
		
		if (hasMemberReference != null) {
			if (lacksAlias(criteriaBuilder, "gm")) {
				root.join("groupMembers").alias("gm");
			}
			
			CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
			handleAndListParam(hasMemberReference, hasMemberRef -> {
				if (hasMemberRef.getChain() != null) {
					if (Observation.SP_CODE.equals(hasMemberRef.getChain())) {
						TokenAndListParam code = new TokenAndListParam()
						        .addAnd(new TokenParam().setValue(hasMemberRef.getValue()));
						
						if (lacksAlias(finalCriteriaBuilder, "c")) {
							root.join("gm.concept").alias("c");
						}
						
						return handleCodeableConcept(finalCriteriaBuilder, code, "c", "cm", "crt");
					}
				} else {
					if (StringUtils.isNotBlank(hasMemberRef.getIdPart())) {
						return Optional.of(finalCriteriaBuilder.equal(root.get("gm.uuid"), hasMemberRef.getIdPart()));
					}
				}
				
				return Optional.empty();
			}).ifPresent(predicates::add);
			criteriaQuery.where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private Optional<Predicate> handleValueStringParam(@Nonnull String propertyName, StringAndListParam valueStringParam) {
		return handleAndListParam(valueStringParam, v -> propertyLike(propertyName, v.getValue()));
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Obs> criteriaQuery = criteriaBuilder.createQuery(Obs.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);
		
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(predicates::add);
			criteriaQuery.where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleConceptClass(CriteriaBuilder criteriaBuilder, TokenAndListParam category) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Obs> criteriaQuery = criteriaBuilder.createQuery(Obs.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);
		
		if (category != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			if (lacksAlias(criteriaBuilder, "cc")) {
				root.join("c.conceptClass").alias("cc");
			}
		}
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(category, (param) -> {
			if (param.getValue() == null) {
				return Optional.empty();
			}
			
			Subquery<String> subquery = criteriaQuery.subquery(String.class);
			Root<?> subRoot = subquery.from(ConceptClass.class);
			subquery.select(subRoot.get("uuid")).where(finalCriteriaBuilder.equal(subRoot.get("category"), param.getValue()));
			
			return Optional.of(finalCriteriaBuilder.in(root.get("concept").get("conceptClass").get("uuid")).value(subquery));
		}).ifPresent(predicates::add);
		criteriaQuery.where(predicates.toArray(new Predicate[] {}));
	}
	
	private void handleValueCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam valueConcept) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Obs> criteriaQuery = criteriaBuilder.createQuery(Obs.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);
		
		if (valueConcept != null) {
			if (lacksAlias(criteriaBuilder, "vc")) {
				root.join("valueCoded").alias("vc");
			}
			handleCodeableConcept(criteriaBuilder, valueConcept, "vc", "vcm", "vcrt").ifPresent(predicates::add);
			criteriaQuery.where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	@Override
	protected String paramToProp(@Nonnull String paramName) {
		if (Observation.SP_DATE.equals(paramName)) {
			return "obsDatetime";
		}
		
		return null;
	}
	
	@Override
	protected Obs deproxyResult(Obs result) {
		Obs obs = super.deproxyResult(result);
		obs.setConcept(deproxyObject(obs.getConcept()));
		return obs;
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
}
