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
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ge;
import static org.hibernate.criterion.Restrictions.le;
import static org.hibernate.criterion.Restrictions.not;

import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirConditionDaoImpl_2_2 extends BaseFhirDao<Condition> implements FhirConditionDao<Condition> {
	
	@Autowired
	private LocalDateTimeFactory localDateTimeFactory;
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public Condition get(String uuid) {
		return super.get(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_CONDITIONS)
	public Condition createOrUpdate(Condition newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_CONDITIONS)
	public Condition delete(String uuid) {
		return super.delete(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public List<String> getSearchResultUuids(SearchParameterMap theParams) {
		return super.getSearchResultUuids(theParams);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public List<Condition> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids,
	        int firstResult, int lastResult) {
		return super.getSearchResults(theParams, matchingResourceUuids, firstResult, lastResult);
	}
	
	private ConditionClinicalStatus convertStatus(String status) {
		if ("active".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.ACTIVE;
		}
		if ("inactive".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.INACTIVE;
		}
		// Note `history_of` is not a valid value in the FHIR spec:
		// http://www.hl7.org/fhir/valueset-condition-clinical.html
		// We are simply following the logic implemented in `ConditionClinicalStatusTranslatorImpl_2_2`.
		return ConditionClinicalStatus.HISTORY_OF;
	}
	
	private Optional<Criterion> handleAgeByDateProperty(@NotNull String datePropertyName, @NotNull QuantityParam age) {
		BigDecimal value = age.getValue();
		if (value == null) {
			throw new IllegalArgumentException("Age value should be provided in " + age);
		}
		
		String unit = age.getUnits();
		if (unit == null) {
			throw new IllegalArgumentException("Age unit should be provided in " + age);
		}
		
		LocalDateTime localDateTime = localDateTimeFactory.now();
		
		TemporalAmount temporalAmount;
		TemporalUnit temporalUnit;
		// TODO check if HAPI FHIR already defines these constant strings. These are mostly from
		// http://www.hl7.org/fhir/valueset-age-units.html with the exception of "s" which is not
		// listed but was seen in FHIR examples: http://www.hl7.org/fhir/datatypes-examples.html#Quantity
		switch (unit) {
			case "s":
				temporalUnit = ChronoUnit.SECONDS;
				temporalAmount = Duration.ofSeconds(value.longValue());
				break;
			case "min":
				temporalUnit = ChronoUnit.MINUTES;
				temporalAmount = Duration.ofMinutes(value.longValue());
				break;
			case "h":
				temporalUnit = ChronoUnit.HOURS;
				temporalAmount = Duration.ofHours(value.longValue());
				break;
			case "d":
				temporalUnit = ChronoUnit.DAYS;
				temporalAmount = Period.ofDays(value.intValue());
				break;
			case "wk":
				temporalUnit = ChronoUnit.WEEKS;
				temporalAmount = Period.ofWeeks(value.intValue());
				break;
			case "mo":
				temporalUnit = ChronoUnit.MONTHS;
				temporalAmount = Period.ofMonths(value.intValue());
				break;
			case "a":
				temporalUnit = ChronoUnit.YEARS;
				temporalAmount = Period.ofYears(value.intValue());
				break;
			default:
				throw new IllegalArgumentException(
				        "Invalid unit " + unit + " in age " + age + " should be one of 'min', 'h', 'd', 'wk', 'mo', 'a'");
		}
		
		localDateTime = localDateTime.minus(temporalAmount);
		
		ParamPrefixEnum prefix = age.getPrefix();
		if (prefix == null) {
			prefix = ParamPrefixEnum.EQUAL;
		}
		
		if (prefix == ParamPrefixEnum.EQUAL || prefix == ParamPrefixEnum.NOT_EQUAL) {
			// Create a range for the targeted unit; the interval length is determined by the unit and
			// its center is `offsetSeconds` in the past.
			final long offset;
			
			// Duration only supports hours as a chunk of seconds
			if (temporalUnit == ChronoUnit.HOURS) {
				offset = temporalAmount.get(ChronoUnit.SECONDS) / (2 * 3600);
			} else {
				offset = temporalAmount.get(temporalUnit) / 2;
			}
			
			LocalDateTime lowerBoundDateTime = LocalDateTime.from(localDateTime).minus(Duration.of(offset, temporalUnit));
			Date lowerBound = Date.from(lowerBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			LocalDateTime upperBoundDateTime = LocalDateTime.from(localDateTime).plus(offset, temporalUnit);
			Date upperBound = Date.from(upperBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			if (prefix == ParamPrefixEnum.EQUAL) {
				return Optional.of(and(ge(datePropertyName, lowerBound), le(datePropertyName, upperBound)));
			} else {
				return Optional.of(not(and(ge(datePropertyName, lowerBound), le(datePropertyName, upperBound))));
			}
		}
		
		switch (prefix) {
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
			case STARTS_AFTER:
				return Optional
				        .of(ge(datePropertyName, Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional
				        .of(le(datePropertyName, Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			// Ignoring ENDS_BEFORE as it is not meaningful for age.
		}
		
		return Optional.empty();
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCode(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
					entry.getValue().forEach(param -> handleClinicalStatus(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDateRange(param.getPropertyName(), (DateRangeParam) param.getParam())
					                .ifPresent(criteria::add));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleOnsetAge(criteria, (QuantityAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
		return super.handleLastUpdatedImmutable(param);
	}
	
	private void handleCode(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("condition.coded", "cd");
			handleCodeableConcept(criteria, code, "cd", "map", "term").ifPresent(criteria::add);
		}
	}
	
	private void handleClinicalStatus(Criteria criteria, TokenAndListParam status) {
		handleAndListParam(status, tokenParam -> Optional.of(eq("clinicalStatus", convertStatus(tokenParam.getValue()))))
		        .ifPresent(criteria::add);
	}
	
	private void handleOnsetAge(Criteria criteria, QuantityAndListParam onsetAge) {
		handleAndListParam(onsetAge, onsetAgeParam -> handleAgeByDateProperty("onsetDate", onsetAgeParam))
		        .ifPresent(criteria::add);
	}
}
