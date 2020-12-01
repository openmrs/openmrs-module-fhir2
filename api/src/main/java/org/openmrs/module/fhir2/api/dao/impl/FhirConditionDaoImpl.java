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
import static org.hibernate.criterion.Subqueries.propertyIn;

import javax.annotation.Nonnull;

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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.openmrs.Obs;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PUBLIC)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class FhirConditionDaoImpl extends BaseFhirDao<Obs> implements FhirConditionDao<Obs> {
	
	@Qualifier("sessionFactory")
	@Autowired
	private SessionFactory sessionFactory;
	
	private final boolean isRetireable;
	
	private final boolean isVoidable;
	
	protected FhirConditionDaoImpl() {
		this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
		this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
	};
	
	private LocalDateTimeFactory localDateTimeFactory;
	
	@Override
	@Authorized(PrivilegeConstants.GET_OBS)
	public Obs get(@Nonnull String uuid) {
		return super.get(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public Obs createOrUpdate(@Nonnull Obs newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_OBS)
	public Obs delete(@Nonnull String uuid) {
		return super.delete(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(eq("concept.conceptId", FhirConstants.CONDITION_OBSERVATION_CONCEPT_ID));
		
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Obs.class);
		Criteria detachedExecutableCriteria = detachedCriteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		
		if (isVoidable) {
			handleVoidable(detachedExecutableCriteria);
		} else if (isRetireable) {
			handleRetireable(detachedExecutableCriteria);
		}
		
		setupSearchParams(detachedExecutableCriteria, theParams);
		handleSort(detachedExecutableCriteria, theParams.getSortSpec());
		
		detachedCriteria.setProjection(Projections.property("uuid"));
		
		criteria.add(propertyIn("uuid", detachedCriteria));
		criteria.setProjection(Projections.groupProperty("uuid"));
		
		@SuppressWarnings("unchecked")
		List<String> results = criteria.list();
		
		return results;
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> matchingResourceUuids,
	        int firstResult, int lastResult) {
		return super.getSearchResults(theParams, matchingResourceUuids, firstResult, lastResult);
	}
	
	private Optional<Criterion> handleAgeByDateProperty(@Nonnull String datePropertyName, @Nonnull QuantityParam age) {
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
	
	private void handleCode(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("valueCoded", "vc");
			handleCodeableConcept(criteria, code, "vc", "map", "term").ifPresent(criteria::add);
		}
	}
	
	private void handleOnsetAge(Criteria criteria, QuantityAndListParam onsetAge) {
		handleAndListParam(onsetAge, onsetAgeParam -> handleAgeByDateProperty("onsetDate", onsetAgeParam))
		        .ifPresent(criteria::add);
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
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
