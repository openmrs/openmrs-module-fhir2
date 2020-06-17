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
import java.util.Calendar;
import java.util.Date;
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
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.CalendarFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirConditionDaoImpl_2_2 extends BaseFhirDao<Condition> implements FhirConditionDao<Condition> {
	// TODO: Change the BaseDaoImpl inheritance pattern to one of composition; here and everywhere else.
	
	@Autowired
	private CalendarFactory calendarFactory;
	
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
		int unitSeconds = -1;
		// TODO check if HAPI FHIR already defines these constant strings. These are mostly from
		// http://www.hl7.org/fhir/valueset-age-units.html with the exception of "s" which is not
		// listed but was seen in FHIR examples: http://www.hl7.org/fhir/datatypes-examples.html#Quantity
		switch (unit) {
			case "s":
				unitSeconds = 1;
				break;
			case "min":
				unitSeconds = 60;
				break;
			case "h":
				unitSeconds = 3600;
				break;
			case "d":
				unitSeconds = 24 * 3600;
				break;
			case "wk":
				unitSeconds = 7 * 24 * 3600;
				break;
			case "mo":
				unitSeconds = 30 * 24 * 3600;
				break;
			case "a":
				unitSeconds = 365 * 24 * 3600;
				break;
		}
		if (unitSeconds < 0) {
			throw new IllegalArgumentException(
			        "Invalid unit " + unit + " in age " + age + " should be one of 'min', 'h', 'd', 'wk', 'mo', 'a'");
		}
		Calendar cal = calendarFactory.getCalendar();
		int offsetSeconds = value.multiply(new BigDecimal(-1 * unitSeconds)).intValue();
		cal.add(Calendar.SECOND, offsetSeconds);
		ParamPrefixEnum prefix = age.getPrefix();
		if (prefix == null) {
			prefix = ParamPrefixEnum.EQUAL;
		}
		if (prefix == ParamPrefixEnum.EQUAL || prefix == ParamPrefixEnum.NOT_EQUAL) {
			// Create a range for the targeted unit; the interval length is determined by the unit and
			// its center is `offsetSeconds` in the past.
			cal.add(Calendar.SECOND, -1 * unitSeconds / 2);
			Date lowerBound = cal.getTime();
			cal.add(Calendar.SECOND, unitSeconds);
			Date upperBound = cal.getTime();
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
				return Optional.of(ge(datePropertyName, cal.getTime()));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(le(datePropertyName, cal.getTime()));
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
				case FhirConstants.STATUS_SEARCH_HANDLER:
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
			}
		});
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
	
	@Override
	public Condition saveCondition(Condition condition) {
		Session session = getSessionFactory().getCurrentSession();
		Date endDate = condition.getEndDate() != null ? condition.getEndDate() : new Date();
		if (condition.getEndReason() != null) {
			condition.setEndDate(endDate);
		}
		
		Condition existingCondition = get(condition.getUuid());
		if (condition.equals(existingCondition)) {
			return existingCondition;
		}
		if (existingCondition == null) {
			session.saveOrUpdate(condition);
			return condition;
		}
		
		condition = Condition.newInstance(condition);
		condition.setPreviousVersion(existingCondition);
		
		if (existingCondition.getClinicalStatus().equals(condition.getClinicalStatus())) {
			existingCondition.setVoided(true);
			session.saveOrUpdate(existingCondition);
			session.saveOrUpdate(condition);
			return condition;
		}
		Date onSetDate = condition.getOnsetDate() != null ? condition.getOnsetDate() : new Date();
		existingCondition.setEndDate(onSetDate);
		session.saveOrUpdate(existingCondition);
		condition.setOnsetDate(onSetDate);
		session.saveOrUpdate(condition);
		
		return condition;
	}
}
