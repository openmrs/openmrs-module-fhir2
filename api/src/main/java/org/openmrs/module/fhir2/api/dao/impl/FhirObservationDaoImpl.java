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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Concept;
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
	
	@Override
	public List<Obs> getSearchResults(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(typeToken.getRawType());
			
			setupSearchParams(criteria, theParams);
			
			criteria.addOrder(Order.asc("concept")).addOrder(Order.desc("obsDatetime"));
			
			List<Obs> results = new ArrayList<>();
			int firstResult = 0;
			final int maxGroupCount = getMaxParameter(theParams);
			final int batchSize = 100;
			Concept prevConcept = null;
			Date prevObsDatetime = null;
			int groupCount = maxGroupCount;
			while (results.size() < theParams.getToIndex()) {
				criteria.setFirstResult(firstResult);
				criteria.setMaxResults(batchSize);
				List<Obs> observations = criteria.list();
				for (Obs obs : observations) {
					if (prevConcept == obs.getConcept()) {
						if (groupCount > 0 || obs.getObsDatetime().equals(prevObsDatetime)) {
							//Load only as many results as requested per group or more if time matches
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
						//Load only as many results as requested per page
						break;
					}
				}
				
				if (observations.size() < batchSize) {
					break;
				} else {
					firstResult += batchSize;
				}
			}
			
			int toIndex = results.size() > theParams.getToIndex() ? theParams.getToIndex() : results.size();
			return results.subList(theParams.getFromIndex(), toIndex).stream().map(this::deproxyResult)
			        .collect(Collectors.toList());
		}
		
		return super.getSearchResults(theParams);
	}
	
	@Override
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
		Root<Object[]> root = criteriaQuery.from(Object[].class);
		TypedQuery<Object[]> typedQuery = manager.createQuery(criteriaQuery);
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			setupSearchParams(criteriaBuilder, theParams);
			criteriaQuery.orderBy(criteriaBuilder.asc(root.get("concept"))).orderBy(criteriaBuilder.desc(root.get("obsDatetime")));
			
			criteriaBuilder.setProjection(Projections.projectionList().add(Projections.groupProperty("concept.id"))
			        .add(Projections.groupProperty("obsDatetime")).add(Projections.rowCount()));
			
			applyExactTotal(theParams, criteriaBuilder);
			List<Object[]> rows = typedQuery.getResultList();
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
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			ReferenceAndListParam encountersReferences = new ReferenceAndListParam();
			ReferenceOrListParam referenceOrListParam = new ReferenceOrListParam();
			
			List<String> encounters = encounterDao.getSearchResultUuids(theParams);
			
			encounters.forEach(encounter -> referenceOrListParam.addOr(new ReferenceParam().setValue(encounter)));
			encountersReferences.addAnd(referenceOrListParam);
			
			theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encountersReferences);
		}
		
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(p -> handleEncounterReference(criteriaBuilder, (ReferenceAndListParam) p.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteriaBuilder,
					    (ReferenceAndListParam) patientReference.getParam(), "person"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteriaBuilder, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(category -> handleConceptClass(criteriaBuilder, (TokenAndListParam) category.getParam()));
					break;
				case FhirConstants.VALUE_CODED_SEARCH_HANDLER:
					entry.getValue().forEach(
					    valueCoded -> handleValueCodedConcept(criteriaBuilder, (TokenAndListParam) valueCoded.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.HAS_MEMBER_SEARCH_HANDLER:
					entry.getValue().forEach(hasMemberReference -> handleHasMemberReference(criteriaBuilder,
					    (ReferenceAndListParam) hasMemberReference.getParam()));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    quantity -> handleQuantity(quantity.getPropertyName(), (QuantityAndListParam) quantity.getParam())
					            .ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.VALUE_STRING_SEARCH_HANDLER:
					entry.getValue().forEach(
					    string -> handleValueStringParam(string.getPropertyName(), (StringAndListParam) string.getParam())
					            .ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
	}
	
	private void handleHasMemberReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam hasMemberReference) {
		if (hasMemberReference != null) {
			if (lacksAlias(criteriaBuilder, "gm")) {
				criteriaBuilder.createAlias("groupMembers", "gm");
			}
			
			handleAndListParam(hasMemberReference, hasMemberRef -> {
				if (hasMemberRef.getChain() != null) {
					if (Observation.SP_CODE.equals(hasMemberRef.getChain())) {
						TokenAndListParam code = new TokenAndListParam()
						        .addAnd(new TokenParam().setValue(hasMemberRef.getValue()));
						
						if (lacksAlias(criteriaBuilder, "c")) {
							root.join("gm.concept").alias("c");
						}
						
						return handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt");
					}
				} else {
					if (StringUtils.isNotBlank(hasMemberRef.getIdPart())) {
						return Optional.of(criteriaBuilder.equal(root.get("gm.uuid"), hasMemberRef.getIdPart()));
					}
				}
				
				return Optional.empty();
			}).ifPresent(criteriaBuilder::and);
		}
	}
	
	private Optional<Predicate> handleValueStringParam(@Nonnull String propertyName, StringAndListParam valueStringParam) {
		return handleAndListParam(valueStringParam, v -> propertyLike(propertyName, v.getValue()));
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private void handleConceptClass(CriteriaBuilder criteriaBuilder, TokenAndListParam category) {
		if (category != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			if (lacksAlias(criteriaBuilder, "cc")) {
				root.join("c.conceptClass").alias("cc");
			}
		}
		
		handleAndListParam(category, (param) -> {
			if (param.getValue() == null) {
				return Optional.empty();
			}
			
			return Optional.of(Subqueries.propertyIn("cc.uuid", categoryMap.queryConceptClassByCategory(param.getValue())));
		}).ifPresent(criteriaBuilder::and);
	}
	
	private void handleValueCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam valueConcept) {
		if (valueConcept != null) {
			if (lacksAlias(criteriaBuilder, "vc")) {
				root.join("valueCoded").alias("vc");
			}
			handleCodeableConcept(criteriaBuilder, valueConcept, "vc", "vcm", "vcrt").ifPresent(criteria::add);
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
