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

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyIn;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.ConceptClass;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Obs.Status;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@Slf4j
public class FhirServiceRequestDaoImpl extends BaseFhirDao<TestOrder> implements FhirServiceRequestDao<TestOrder> {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(criteria, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.HAS_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleHasAndListParam(criteria, (HasAndListParam) param.getParam()));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteria,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteria, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(criteria,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange((DateRangeParam) dateRangeParam.getParam())
					        .ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private Optional<Criterion> handleDateRange(DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(Stream.of(
		    Optional.of(or(toCriteriaArray(Stream.of(handleDate("scheduledDate", dateRangeParam.getLowerBound()),
		        handleDate("dateActivated", dateRangeParam.getLowerBound()))))),
		    Optional.of(or(toCriteriaArray(Stream.of(handleDate("dateStopped", dateRangeParam.getUpperBound()),
		        handleDate("autoExpireDate", dateRangeParam.getUpperBound())))))))));
	}
	
	/**
	 * Handle _has parameters that are passed in to constrain the ServiceRequest resource on properties
	 * of dependent resources
	 */
	private void handleHasAndListParam(Criteria criteria, HasAndListParam hasAndListParam) {
		if (criteria == null) {
			log.warn("handleHasAndListParam called without criteria.");
			return;
		}
		
		if (hasAndListParam == null) {
			log.warn("handleHasAndListParam called without param or param list.");
			return;
		}
		
		hasAndListParam.getValuesAsQueryTokens().forEach(hasOrListParam -> {
			List<HasParam> queryTokens = hasOrListParam.getValuesAsQueryTokens();
			if (queryTokens.isEmpty()) {
				return;
			}
			
			// Making the assumption that any "orListParams" match everything except for the value
			HasParam hasParam = queryTokens.get(0);
			Set<String> values = queryTokens.stream().map(HasParam::getParameterValue).collect(Collectors.toSet());
			
			switch (hasParam.getTargetResourceType()) {
				case FhirConstants.OBSERVATION:
					handleHasObservation(criteria, hasParam, values);
					break;
				default:
					log.warn("_has parameter not supported: " + hasParam.getQueryParameterQualifier());
			}
		});
	}
	
	private void handleHasObservation(Criteria criteria, HasParam hasParam, Set<String> values) {
		String projection;
		switch (hasParam.getReferenceFieldName()) {
			case Observation.SP_BASED_ON:
				projection = "order";
				break;
			default:
				log.warn("Failed to add has constraint for non-existent reference " + hasParam.getReferenceFieldName());
				// ensure no entries are found
				criteria.add(Restrictions.isNull("id"));
				return;
		}
		
		String parameterName = hasParam.getParameterName();
		String parameterValue = hasParam.getParameterValue();
		DetachedCriteria observationCriteria = createObservationCriteria(projection, parameterName, parameterValue);
		
		criteria.add(propertyIn("id", observationCriteria));
	}
	
	// the detached criteria in this file should all return a subquery of ids to make usage of them consistent
	private DetachedCriteria createObservationCriteria(String projection, String parameterName, String parameterValue) {
		DetachedCriteria observationQuery = DetachedCriteria.forClass(Obs.class);
		observationQuery.setProjection(Projections.property(projection));
		
		if (parameterName == null) {
			// just check for existence of any observation if no further propertyname is given
			return observationQuery;
		}
		
		switch (parameterName) {
			case Observation.SP_DATE:
				addSimpleDateSearch(observationQuery, "obsDatetime", parameterValue);
				break;
			
			case Observation.SP_PATIENT:
			case Observation.SP_SUBJECT:
				addReferenceSearchByUuid(observationQuery, "person", parameterValue);
				break;
			
			case Observation.SP_VALUE_CONCEPT:
				addReferenceSearchByUuid(observationQuery, "valueCoded", parameterValue);
				break;
			
			case Observation.SP_VALUE_DATE:
				addSimpleDateSearch(observationQuery, "valueDatetime", parameterValue);
				break;
			
			case Observation.SP_HAS_MEMBER:
				DetachedCriteria memberQuery = DetachedCriteria.forClass(Obs.class);
				memberQuery.setProjection(Projections.property("obsGroup"));
				
				if (parameterValue != null) {
					memberQuery.add(Restrictions.eq("uuid", parameterValue));
				}
				
				observationQuery.add(propertyIn("id", memberQuery));
				break;
			
			case Observation.SP_VALUE_STRING:
				addSimpleSearch(observationQuery, "valueText", parameterValue);
				break;
			
			case Observation.SP_IDENTIFIER:
				addSimpleSearch(observationQuery, "uuid", parameterValue);
				break;
			
			case Observation.SP_ENCOUNTER:
				addReferenceSearchByUuid(observationQuery, "encounter", parameterValue);
				break;
			
			case Observation.SP_CATEGORY:
				if (parameterValue == null) {
					observationQuery.add(Restrictions.isNotNull("concept"));
					break;
				}
				
				// TODO: where are the possible values defined? Are all values in ConceptClass relevant?
				switch (parameterValue) {
					case "laboratory":
						addReferenceSearchByUuid(observationQuery, "concept", ConceptClass.LABSET_UUID);
						break;
					
					default:
						log.warn(
						    "Failed to add has constraint for observation category with unknown concept  " + parameterValue);
						// ensure no entries are found
						addNoResultsCriteria(observationQuery);
				}
				break;
			
			case Observation.SP_STATUS:
				addEnumSearch(observationQuery, "status", Obs.Status.class, parameterValue);
				break;
			
			case Observation.SP_CODE:
				addReferenceSearchByUuid(observationQuery, "concept", parameterValue);
				break;
			
			case Observation.SP_VALUE_QUANTITY:
				addSimpleDoubleSearch(observationQuery, "valueNumeric", parameterValue);
				break;
			
			case Observation.SP_PERFORMER:
				DetachedCriteria encounterProviderQuery = DetachedCriteria.forClass(EncounterProvider.class);
				encounterProviderQuery.setProjection(Projections.property("encounter"));
				
				if (parameterValue != null) {
					encounterProviderQuery.createAlias("provider", "provider");
					encounterProviderQuery.add(Restrictions.eq("provider.uuid", parameterValue));
				}
				
				observationQuery.add(propertyIn("encounter", encounterProviderQuery));
				break;
			
			// TODO: add explanation on why these values are not implemented
			case Observation.SP_CODE_VALUE_STRING:
			case Observation.SP_PART_OF:
			case Observation.SP_CODE_VALUE_DATE:
			case Observation.SP_CODE_VALUE_QUANTITY:
			case Observation.SP_CODE_VALUE_CONCEPT:
				
				// only meaningful mapping would be the accessionIdentifier, not a whole specimen
			case Observation.SP_SPECIMEN:
				
				// no meaningful mapping in OpenMRS
			case Observation.SP_FOCUS:
			case Observation.SP_DERIVED_FROM:
			case Observation.SP_METHOD:
			case Observation.SP_DATA_ABSENT_REASON:
			case Observation.SP_DEVICE:
				
				// OpenMRS does not support Components yet, this includes the SP_COMPONENT_* space and SP_COMBO_* space
			case Observation.SP_COMPONENT_DATA_ABSENT_REASON:
			case Observation.SP_COMPONENT_CODE_VALUE_QUANTITY:
			case Observation.SP_COMPONENT_VALUE_QUANTITY:
			case Observation.SP_COMPONENT_CODE_VALUE_CONCEPT:
			case Observation.SP_COMPONENT_VALUE_CONCEPT:
			case Observation.SP_COMPONENT_CODE:
			case Observation.SP_COMBO_DATA_ABSENT_REASON:
			case Observation.SP_COMBO_CODE:
			case Observation.SP_COMBO_CODE_VALUE_QUANTITY:
			case Observation.SP_COMBO_CODE_VALUE_CONCEPT:
			case Observation.SP_COMBO_VALUE_QUANTITY:
			case Observation.SP_COMBO_VALUE_CONCEPT:
				log.warn("Failed to add has constraint for observation search parameter " + parameterValue
				        + ": Not Implemented.");
				// ensure no entries are found
				addNoResultsCriteria(observationQuery);
				break;
			
			default:
				log.warn("Failed to add has constraint for observation search parameter " + parameterValue
				        + ": Invalid search parameter.");
				// ensure no entries are found
				addNoResultsCriteria(observationQuery);
				break;
		}
		
		return observationQuery;
	}
	
	private <T extends Enum<T>> void addEnumSearch(DetachedCriteria observationQuery, String parameterField,
	        Class<T> enumClass, String parameterValue) {
		if (parameterValue == null) {
			addSimpleSearch(observationQuery, parameterField, parameterValue);
			return;
		}
		
		try {
			T enumValue = Enum.valueOf(enumClass, parameterValue);
			addSimpleSearch(observationQuery, parameterField, enumValue);
		}
		catch (IllegalArgumentException e) {
			log.warn("Failed to parse Enum " + parameterValue);
			// ensure no entries are found
			addNoResultsCriteria(observationQuery);
		}
	}
	
	private void addReferenceSearchByUuid(DetachedCriteria observationQuery, String parameterField, String parameterValue) {
		if (parameterValue == null) {
			observationQuery.add(Restrictions.isNotNull(parameterField));
			return;
		}
		
		observationQuery.createAlias(parameterField, parameterField);
		observationQuery.add(Restrictions.eq(parameterField + ".uuid", parameterValue));
	}
	
	private void addSimpleDateSearch(DetachedCriteria observationQuery, String parameterField, String parameterValue) {
		if (parameterValue == null) {
			addSimpleSearch(observationQuery, parameterField, parameterValue);
			return;
		}
		
		try {
			Date valueDate = DateFormat.getDateTimeInstance().parse(parameterValue);
			addSimpleSearch(observationQuery, parameterField, valueDate);
		}
		catch (ParseException e) {
			log.warn("Failed to parse Date " + parameterValue);
			// ensure no entries are found
			addNoResultsCriteria(observationQuery);
		}
	}
	
	private void addSimpleDoubleSearch(DetachedCriteria observationQuery, String parameterField, String parameterValue) {
		if (parameterValue == null) {
			addSimpleSearch(observationQuery, parameterField, parameterValue);
			return;
		}
		
		try {
			Double valueInteger = Double.parseDouble(parameterValue);
			addSimpleSearch(observationQuery, parameterField, valueInteger);
		}
		catch (NumberFormatException e) {
			log.warn("Failed to parse Double " + parameterValue);
			// ensure no entries are found
			addNoResultsCriteria(observationQuery);
		}
	}
	
	private void addSimpleSearch(DetachedCriteria observationQuery, String parameterField, Object parameterValue) {
		if (parameterValue == null) {
			observationQuery.add(Restrictions.isNotNull(parameterField));
			return;
		}
		
		observationQuery.add(Restrictions.eq(parameterField, parameterValue));
	}
	
	private void addNoResultsCriteria(DetachedCriteria observationQuery) {
		observationQuery.add(Restrictions.isNull("id"));
	}
}
