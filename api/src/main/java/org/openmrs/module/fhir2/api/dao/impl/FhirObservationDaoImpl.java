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

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.mappings.ObservationCategoryMap;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirObservationDaoImpl extends BaseFhirDao<Obs> implements FhirObservationDao {
	
	@Autowired
	private ObservationCategoryMap categoryMap;
	
	@Override
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER).isEmpty()) {
			
			Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(typeToken.getRawType());
			
			setupSearchParams(criteria, theParams);
			
			criteria.setProjection(
			    Projections.projectionList().add(property("uuid")).add(property("concept")).add(property("obsDatetime")));
			
			@SuppressWarnings("unchecked")
			List<Object[]> results = criteria.list();
			
			return getLastnUuids(handleGrouping(results), getMaxParameter(theParams)).stream().distinct()
			        .collect(Collectors.toList());
		}
		
		return super.getSearchResultUuids(theParams);
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(p -> handleEncounterReference("e", (ReferenceAndListParam) p.getParam())
					        .ifPresent(c -> criteria.createAlias("encounter", "e").add(c)));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteria,
					    (ReferenceAndListParam) patientReference.getParam(), "person"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteria, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(category -> handleConceptClass(criteria, (TokenAndListParam) category.getParam()));
					break;
				case FhirConstants.VALUE_CODED_SEARCH_HANDLER:
					entry.getValue().forEach(
					    valueCoded -> handleValueCodedConcept(criteria, (TokenAndListParam) valueCoded.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.HAS_MEMBER_SEARCH_HANDLER:
					entry.getValue().forEach(hasMemberReference -> handleHasMemberReference(criteria,
					    (ReferenceAndListParam) hasMemberReference.getParam()));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    quantity -> handleQuantity(quantity.getPropertyName(), (QuantityAndListParam) quantity.getParam())
					            .ifPresent(criteria::add));
					break;
				case FhirConstants.VALUE_STRING_SEARCH_HANDLER:
					entry.getValue().forEach(
					    string -> handleValueStringParam(string.getPropertyName(), (StringAndListParam) string.getParam())
					            .ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	private void handleHasMemberReference(Criteria criteria, ReferenceAndListParam hasMemberReference) {
		if (hasMemberReference != null) {
			if (lacksAlias(criteria, "gm")) {
				criteria.createAlias("groupMembers", "gm");
			}
			
			handleAndListParam(hasMemberReference, hasMemberRef -> {
				if (hasMemberRef.getChain() != null) {
					if (Observation.SP_CODE.equals(hasMemberRef.getChain())) {
						TokenAndListParam code = new TokenAndListParam()
						        .addAnd(new TokenParam().setValue(hasMemberRef.getValue()));
						
						if (lacksAlias(criteria, "c")) {
							criteria.createAlias("gm.concept", "c");
						}
						
						return handleCodeableConcept(criteria, code, "c", "cm", "crt");
					}
				} else {
					if (StringUtils.isNotBlank(hasMemberRef.getIdPart())) {
						return Optional.of(eq("gm.uuid", hasMemberRef.getIdPart()));
					}
				}
				
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}
	
	private Optional<Criterion> handleValueStringParam(@Nonnull String propertyName, StringAndListParam valueStringParam) {
		return handleAndListParam(valueStringParam, v -> propertyLike(propertyName, v.getValue()));
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private void handleConceptClass(Criteria criteria, TokenAndListParam category) {
		if (category != null) {
			if (lacksAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			
			if (lacksAlias(criteria, "cc")) {
				criteria.createAlias("c.conceptClass", "cc");
			}
		}
		
		handleAndListParam(category, (param) -> {
			if (param.getValue() == null) {
				return Optional.empty();
			}
			
			return Optional.of(Subqueries.propertyIn("cc.uuid", categoryMap.queryConceptClassByCategory(param.getValue())));
		}).ifPresent(criteria::add);
	}
	
	private void handleValueCodedConcept(Criteria criteria, TokenAndListParam valueConcept) {
		if (valueConcept != null) {
			if (lacksAlias(criteria, "vc")) {
				criteria.createAlias("valueCoded", "vc");
			}
			handleCodeableConcept(criteria, valueConcept, "vc", "vcm", "vcrt").ifPresent(criteria::add);
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
	
	private List<String> getTopNRankedUuids(List<Object[]> list, int max) {
		List<String> results = new ArrayList<>(list.size());
		int currentRank = 0;
		
		for (int var = 0; var < list.size() && currentRank < max; var++) {
			currentRank++;
			results.add((String) list.get(var)[0]);
			Date currentObsDate = (Date) list.get(var)[2];
			
			if (var == list.size() - 1) {
				return results;
			}
			
			//Adding all Obs which have the same Datetime as the current Obs Datetime since they will have the same rank
			Date nextObsDate = (Date) list.get(var + 1)[2];
			while (nextObsDate.equals(currentObsDate)) {
				results.add((String) list.get(var + 1)[0]);
				var++;
				
				if (var + 1 == list.size()) {
					return results;
				} else {
					nextObsDate = (Date) list.get(var + 1)[2];
				}
			}
		}
		return results;
	}
	
	private HashMap<Object, List<Object[]>> handleGrouping(List<Object[]> observations) {
		return (HashMap<Object, List<Object[]>>) observations.stream().collect(Collectors.groupingBy(o -> o[1]));
	}
	
	private List<String> getLastnUuids(HashMap<Object, List<Object[]>> groupingByObsCode, int max) {
		List<String> results = new ArrayList<>();
		for (HashMap.Entry entry : groupingByObsCode.entrySet()) {
			@SuppressWarnings("unchecked")
			List<Object[]> obsList = (List<Object[]>) entry.getValue();
			obsList.sort((a, b) -> (((Date) b[2]).compareTo((Date) a[2])));
			List<String> lastnUuidsInGroup = getTopNRankedUuids(obsList, max);
			results.addAll(lastnUuidsInGroup);
		}
		return results;
	}
}
