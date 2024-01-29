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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

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
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
			OpenmrsFhirCriteriaContext<Obs, Obs> criteriaContext = createCriteriaContext(Obs.class);
			
			setupSearchParams(criteriaContext, theParams);
			
			criteriaContext.getCriteriaQuery().orderBy(
			    criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("concept")),
			    criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get("obsDatetime")));
			
			int firstResult = 0;
			final int maxGroupCount = getMaxParameter(theParams);
			final int batchSize = 100;
			Concept prevConcept = null;
			Date prevObsDatetime = null;
			int groupCount = maxGroupCount;
			
			while (criteriaContext.getResults().size() < theParams.getToIndex()) {
				criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery())
				        .setFirstResult(firstResult);
				criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setMaxResults(batchSize);
				
				List<Obs> observations = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery())
				        .getResultList();
				
				for (Obs obs : observations) {
					if (prevConcept == obs.getConcept()) {
						if (groupCount > 0 || obs.getObsDatetime().equals(prevObsDatetime)) {
							// Load only as many results as requested per group or more if time matches
							if (!obs.getObsDatetime().equals(prevObsDatetime)) {
								groupCount--;
							}
							prevObsDatetime = obs.getObsDatetime();
							criteriaContext.addResults(obs);
						}
					} else {
						prevConcept = obs.getConcept();
						prevObsDatetime = obs.getObsDatetime();
						groupCount = maxGroupCount;
						criteriaContext.addResults(obs);
						groupCount--;
					}
					
					if (criteriaContext.getResults().size() >= theParams.getToIndex()) {
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
			
			int toIndex = Math.min(criteriaContext.getResults().size(), theParams.getToIndex());
			return criteriaContext.getResults().subList(theParams.getFromIndex(), toIndex).stream().map(this::deproxyResult)
			        .collect(Collectors.toList());
		}
		
		return super.getSearchResults(theParams);
	}
	
	@Override
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			OpenmrsFhirCriteriaContext<Obs, Obs> criteriaContext = createCriteriaContext(Obs.class);
			setupSearchParams(criteriaContext, theParams);
			criteriaContext.getCriteriaQuery()
			        .orderBy(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("concept")))
			        .orderBy(criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get("obsDatetime")));
			
			criteriaContext.getCriteriaQuery().multiselect(criteriaContext.getRoot().get("concept.id"),
			    criteriaContext.getRoot().get("obsDatetime"),
			    criteriaContext.getCriteriaBuilder().count(criteriaContext.getRoot()));
			
			applyExactTotal(criteriaContext, theParams);
			
			OpenmrsFhirCriteriaContext<Object[], Object[]> context = createCriteriaContext(Object[].class);
			List<Object[]> rows = context.getEntityManager().createQuery(context.getCriteriaQuery()).getResultList();
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
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Obs, U> criteriaContext, SearchParameterMap theParams) {
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
					entry.getValue().forEach(
					    p -> handleEncounterReference(criteriaContext, (ReferenceAndListParam) p.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) patientReference.getParam(), "person"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(code -> handleCodedConcept(criteriaContext, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    category -> handleConceptClass(criteriaContext, (TokenAndListParam) category.getParam()));
					break;
				case FhirConstants.VALUE_CODED_SEARCH_HANDLER:
					entry.getValue().forEach(
					    valueCoded -> handleValueCodedConcept(criteriaContext, (TokenAndListParam) valueCoded.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(dateRangeParam -> handleDateRange(criteriaContext, dateRangeParam.getPropertyName(),
					            (DateRangeParam) dateRangeParam.getParam())
					                    .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
					break;
				case FhirConstants.HAS_MEMBER_SEARCH_HANDLER:
					entry.getValue().forEach(hasMemberReference -> handleHasMemberReference(criteriaContext,
					    (ReferenceAndListParam) hasMemberReference.getParam()));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(quantity -> handleQuantity(criteriaContext, quantity.getPropertyName(),
					            (QuantityAndListParam) quantity.getParam())
					                    .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
					break;
				case FhirConstants.VALUE_STRING_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(string -> handleValueStringParam(criteriaContext, string.getPropertyName(),
					            (StringAndListParam) string.getParam())
					                    .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue())
					        .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
					break;
			}
		});
	}
	
	private <U> void handleHasMemberReference(OpenmrsFhirCriteriaContext<Obs, U> criteriaContext,
	        ReferenceAndListParam hasMemberReference) {
		Join<?, ?> groupMembersJoin = criteriaContext.addJoin("groupMembers", "groupMembersJoin");
		if (hasMemberReference != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), hasMemberReference, hasMemberRef -> {
				if (hasMemberRef.getChain() != null) {
					if (Observation.SP_CODE.equals(hasMemberRef.getChain())) {
						TokenAndListParam code = new TokenAndListParam()
						        .addAnd(new TokenParam().setValue(hasMemberRef.getValue()));
						Join<?, ?> conceptJoin = criteriaContext.addJoin(groupMembersJoin, "concept", "c");
						return handleCodeableConcept(criteriaContext, code, conceptJoin, "cm", "crt");
					}
				} else {
					if (StringUtils.isNotBlank(hasMemberRef.getIdPart())) {
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(groupMembersJoin.get("uuid"),
						    hasMemberRef.getIdPart()));
					}
				}
				
				return Optional.empty();
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	private <T, U> Optional<Predicate> handleValueStringParam(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull String propertyName, StringAndListParam valueStringParam) {
		//TODO: needs further investigation
		Join<?, ?> propertyNameJoin = criteriaContext.addJoin(propertyName, propertyName);
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), valueStringParam,
		    v -> propertyLike(criteriaContext, propertyNameJoin, propertyName, v.getValue()));
	}
	
	private <U> void handleCodedConcept(OpenmrsFhirCriteriaContext<Obs, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?, ?> concept = criteriaContext.addJoin("concept", "c");
			handleCodeableConcept(criteriaContext, code, concept, "cm", "crt")
			        .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	private <U> void handleConceptClass(OpenmrsFhirCriteriaContext<Obs, U> criteriaContext, TokenAndListParam category) {
		if (category == null) {
			Join<?, ?> conceptJoin = criteriaContext.addJoin("concept", "c");
			Join<?, ?> conceptClassJoin = criteriaContext.addJoin(conceptJoin, "conceptClass", "cc");
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), category, (param) -> {
				if (param.getValue() == null) {
					return Optional.empty();
				}
				OpenmrsFhirCriteriaContext<String, U> context = createCriteriaContext(String.class);
				context.getCriteriaQuery().subquery(String.class).select(conceptClassJoin.get("uuid"))
				        .where(context.getCriteriaBuilder().equal(context.getRoot().get("category"), param.getValue()));
				
				return Optional.of(
				    context.getCriteriaBuilder().in(criteriaContext.getRoot().get("concept").get("conceptClass").get("uuid"))
				            .value(context.getCriteriaQuery().subquery(String.class)));
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	private <U> void handleValueCodedConcept(OpenmrsFhirCriteriaContext<Obs, U> criteriaContext,
	        TokenAndListParam valueConcept) {
		if (valueConcept != null) {
			Join<?, ?> valueCodedJoin = criteriaContext.addJoin("valueCoded", "vc");
			handleCodeableConcept(criteriaContext, valueConcept, valueCodedJoin, "vcm", "vcrt")
			        .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	@Override
	protected <V, U> String paramToProp(OpenmrsFhirCriteriaContext<V, U> criteriaContext, @NonNull String paramName) {
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
