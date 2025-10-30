/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.internals;

import javax.annotation.Nonnull;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender;
import org.openmrs.Person;
import org.openmrs.module.fhir2.api.dao.impl.BaseDao;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

//@formatter:off
/**
 * A helper component providing FHIR search query building utilities.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * <pre>{@code
 * @Component
 * public class CustomFhirDaoImpl {
 *     @Autowired
 *     private FhirSearchQueryHelper searchQueryHelper;
 *
 *     protected void setupSearchParams(...) {
 *         searchQueryHelper.handleNames(criteriaContext, name, given, family, personAlias);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * <strong>Method Categories:</strong>
 * </p>
 * <ul>
 * <li>Date/Time Handling: {@link #handleDate}, {@link #handleDateRange},
 * {@link #handleAgeByDateProperty}</li>
 * <li>Quantity Handling: {@link #handleQuantity(BaseFhirCriteriaHolder, String, QuantityParam)},
 * {@link #handleQuantity(BaseFhirCriteriaHolder, String, QuantityAndListParam)}</li>
 * <li>Reference Handling: {@link #handlePatientReference(OpenmrsFhirCriteriaContext, ReferenceAndListParam)}, {@link #handlePatientReference( OpenmrsFhirCriteriaContext, ReferenceAndListParam, String)}, {@link #handleEncounterReference( BaseFhirCriteriaHolder, ReferenceAndListParam, String)}, {@link #handleEncounterReference( BaseFhirCriteriaHolder, ReferenceAndListParam, String, String)},
 * {@link #handleLocationReference}</li>
 * <li>CodeableConcept Handling: {@link #handleCodeableConcept}</li>
 * <li>String Matching: {@link #propertyLike}</li>
 * <li>Boolean Handling: {@link #handleBoolean}</li>
 * <li>Name Handling: {@link #handleNames}</li>
 * <li>Address Handling: {@link #handlePersonAddress}</li>
 * <li>Order Handling: {@link #handleQueryForActiveOrders(OpenmrsFhirCriteriaContext)}, {@link #handleQueryForActiveOrders( OpenmrsFhirCriteriaContext, String)}, {@link #handleQueryForActiveOrders(OpenmrsFhirCriteriaContext, Date)}, {@link #handleQueryForActiveOrders(OpenmrsFhirCriteriaContext, String, Date)}
 * {@link #handleQueryForCancelledOrders}</li>
 * </ul>
 *
 * Note that by default classes that extend {@link org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao} will have
 * this class available via a getter.
 */
//@formatter:on
@Component
@Slf4j
public class FhirSearchQueryHelper extends BaseDao {
	
	private static final BigDecimal APPROX_RANGE = new BigDecimal("0.1");
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private LocalDateTimeFactory localDateTimeFactory;
	
	// ========== Status Conversion ==========
	
	public TokenAndListParam convertStringStatusToBoolean(TokenAndListParam statusParam) {
		if (statusParam != null) {
			return handleAndListParam(statusParam).map(this::convertStringStatusToBoolean).collect(TokenAndListParam::new,
			    TokenAndListParam::addAnd, (tp1, tp2) -> tp2.getValuesAsQueryTokens().forEach(tp1::addAnd));
		}
		
		return null;
	}
	
	public TokenOrListParam convertStringStatusToBoolean(TokenOrListParam statusParam) {
		if (statusParam != null) {
			return handleOrListParam(statusParam).map(s -> {
				switch (s.getValue()) {
					case "active":
						return Optional.of("false");
					case "inactive":
						return Optional.of("true");
					default:
						return Optional.empty();
				}
			}).filter(Optional::isPresent).map(Optional::get).collect(TokenOrListParam::new,
			    (tp, v) -> tp.add(String.valueOf(v)), (tp1, tp2) -> tp2.getListAsCodings().forEach(tp1::add));
		}
		
		return null;
	}
	
	// ========== Age Handling ==========
	
	public <V> Optional<Predicate> handleAgeByDateProperty(BaseFhirCriteriaHolder<V> criteriaContext,
	        @Nonnull String datePropertyName, @Nonnull QuantityParam age) {
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
			
			LocalDateTime lowerBoundDateTime = localDateTime.minus(Duration.of(offset, temporalUnit));
			Date lowerBound = Date.from(lowerBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			LocalDateTime upperBoundDateTime = localDateTime.plus(offset, temporalUnit);
			Date upperBound = Date.from(upperBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			if (prefix == ParamPrefixEnum.EQUAL) {
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder()
				            .greaterThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), lowerBound),
				    criteriaContext.getCriteriaBuilder().lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName),
				        upperBound)));
			} else {
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder()
				        .not(criteriaContext.getCriteriaBuilder().and(
				            criteriaContext.getCriteriaBuilder()
				                    .greaterThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), lowerBound),
				            criteriaContext.getCriteriaBuilder()
				                    .lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), upperBound))));
			}
		}
		
		switch (prefix) {
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
			case STARTS_AFTER:
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(
				    criteriaContext.getRoot().get(datePropertyName),
				    Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.ofNullable(
				    criteriaContext.getCriteriaBuilder().lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName),
				        Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			// Ignoring ENDS_BEFORE as it is not meaningful for age.
		}
		
		return Optional.empty();
	}
	
	// ========== Boolean Handling ==========
	
	/**
	 * Handler for a {@link TokenOrListParam} that represents boolean values
	 *
	 * @param criteriaContext the active {@link BaseFhirCriteriaHolder} for the current query
	 * @param propertyName the name of the property in the query to use
	 * @param booleanToken the {@link TokenOrListParam} to handle
	 * @return a {@link Predicate} to be added to the query indicating that the property matches the
	 *         given value
	 */
	public <T> Optional<Predicate> handleBoolean(BaseFhirCriteriaHolder<T> criteriaContext, String propertyName,
	        TokenAndListParam booleanToken) {
		if (booleanToken == null) {
			return Optional.empty();
		}
		
		// note that we use a custom implementation here as Boolean.valueOf() and Boolean.parse() only determine whether
		// the string matches "true". We could potentially be passed a non-valid Boolean value here.
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), booleanToken, token -> {
			if (token.getValue().equalsIgnoreCase("true")) {
				return handleBooleanProperty(criteriaContext, propertyName, true);
			} else if (token.getValue().equalsIgnoreCase("false")) {
				return handleBooleanProperty(criteriaContext, propertyName, false);
			}
			
			return Optional.empty();
		});
	}
	
	/**
	 * Handler for an individual boolean value that represents boolean values
	 *
	 * @param criteriaContext the active {@link BaseFhirCriteriaHolder} for the current query
	 * @param propertyName the name of the property in the query to use
	 * @param booleanVal the value to restrict this boolean property to
	 * @return a {@link Predicate} to be added to the query indicating that the property matches the
	 *         given value
	 */
	public <T> Optional<Predicate> handleBooleanProperty(BaseFhirCriteriaHolder<T> criteriaContext, String propertyName,
	        boolean booleanVal) {
		return Optional
		        .of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get(propertyName), booleanVal));
	}
	
	// ========== CodeableConcept Handling ==========
	
	public <T, U> Optional<Predicate> handleCodeableConcept(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        TokenAndListParam concepts, @Nonnull From<?, ?> conceptAlias, @Nonnull String conceptMapAlias,
	        @Nonnull String conceptReferenceTermAlias) {
		if (concepts == null) {
			return Optional.empty();
		}
		
		return handleAndListParamBySystem(criteriaContext.getCriteriaBuilder(), concepts, (system, tokens) -> {
			if (system.isEmpty()) {
				Predicate inConceptId = criteriaContext.getCriteriaBuilder().in(conceptAlias.get("conceptId"))
				        .value(criteriaContext.getCriteriaBuilder()
				                .literal(tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList())));
				Predicate inUuid = criteriaContext.getCriteriaBuilder().in(conceptAlias.get("uuid"))
				        .value(criteriaContext.getCriteriaBuilder().literal(tokensToList(tokens)));
				
				return Optional.of(criteriaContext.getCriteriaBuilder().or(inConceptId, inUuid));
			} else {
				Join<?, ?> conceptMapAliasJoin = criteriaContext.addJoin(conceptAlias, "conceptMappings", conceptMapAlias);
				criteriaContext.addJoin(conceptMapAliasJoin, "conceptReferenceTerm", conceptReferenceTermAlias);
				
				return handleQueryForSystem(criteriaContext, system, tokensToList(tokens), conceptReferenceTermAlias);
			}
		});
	}
	
	// ========== Date Handling ==========
	
	/**
	 * A handler for a {@link DateParam}, which represents a date and a comparator
	 *
	 * @param criteriaContext the active {@link BaseFhirCriteriaHolder} for the current query
	 * @param propertyName the name of the property in the query to use
	 * @param dateParam the {@link DateParam} to handle
	 * @return a {@link Predicate} to be added to the query for the indicated date parameter
	 */
	public <T> Optional<Predicate> handleDate(BaseFhirCriteriaHolder<T> criteriaContext, String propertyName,
	        DateParam dateParam) {
		if (dateParam == null) {
			return Optional.empty();
		}
		
		int calendarPrecision = dateParam.getPrecision().getCalendarConstant();
		if (calendarPrecision > Calendar.SECOND) {
			calendarPrecision = Calendar.SECOND;
		}
		// TODO We may want to not use the default Calendar
		Date dateStart = DateUtils.truncate(dateParam.getValue(), calendarPrecision);
		Date dateEnd = DateUtils.ceiling(dateParam.getValue(), calendarPrecision);
		
		// TODO This does not properly handle FHIR Periods and Timings, though its unclear if we are using those
		// see https://www.hl7.org/fhir/search.html#date
		switch (dateParam.getPrefix()) {
			case EQUAL:
				return Optional.of(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName),
				        dateStart),
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd)));
			case NOT_EQUAL:
				return Optional.of(criteriaContext.getCriteriaBuilder().not(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName),
				        dateStart),
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional.of(criteriaContext.getCriteriaBuilder()
				        .lessThanOrEqualTo(criteriaContext.getRoot().get(propertyName), dateEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(criteriaContext.getCriteriaBuilder()
				        .greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName), dateStart));
			case STARTS_AFTER:
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().greaterThan(criteriaContext.getRoot().get(propertyName), dateEnd));
			case ENDS_BEFORE:
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd));
		}
		
		return Optional.empty();
	}
	
	/**
	 * A handler for a {@link DateRangeParam}, which represents an inclusive set of {@link DateParam}s
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateRangeParam the {@link DateRangeParam} to handle
	 * @return a {@link Predicate} to be added to the query for the indicated date range
	 */
	public <T, U> Optional<Predicate> handleDateRange(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String propertyName,
	        DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(criteriaContext.getCriteriaBuilder()
		        .and(toCriteriaArray(Stream.of(handleDate(criteriaContext, propertyName, dateRangeParam.getLowerBound()),
		            handleDate(criteriaContext, propertyName, dateRangeParam.getUpperBound())))));
	}
	
	// ========== Reference Handling ==========
	
	public <V> void handleEncounterReference(BaseFhirCriteriaHolder<V> criteriaContext,
	        ReferenceAndListParam encounterReference, @Nonnull String encounterAlias) {
		handleEncounterReference(criteriaContext, encounterReference, encounterAlias, "encounter");
	}
	
	public <V> void handleEncounterReference(BaseFhirCriteriaHolder<V> criteriaContext,
	        ReferenceAndListParam encounterReference, @Nonnull String encounterAlias, @Nonnull String associationPath) {
		
		if (encounterReference == null) {
			return;
		}
		Join<?, ?> associationPathEncounterJoin = criteriaContext.addJoin(associationPath, encounterAlias);
		
		handleAndListParam(criteriaContext.getCriteriaBuilder(), encounterReference, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Encounter.SP_TYPE:
						Join<?, ?> associationPathEncounterEncounterTypeJoin = criteriaContext
						        .addJoin(associationPathEncounterJoin, "encounterType", "et");
						return propertyLike(criteriaContext, associationPathEncounterEncounterTypeJoin, "uuid",
						    new StringParam(token.getValue(), true));
				}
			} else {
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().equal(associationPathEncounterJoin.get("uuid"), token.getIdPart()));
			}
			
			return Optional.empty();
		}).ifPresent(criteriaContext::addPredicate);
	}
	
	// ========== Gender Handling ==========
	
	public <T, U> Optional<Predicate> handleGender(OpenmrsFhirCriteriaContext<T, U> criteriaContext, From<?, ?> from,
	        @Nonnull String propertyName, @Nullable TokenAndListParam gender) {
		if (gender == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), gender, token -> {
			try {
				AdministrativeGender administrativeGender = AdministrativeGender.fromCode(token.getValue());
				
				if (administrativeGender == null) {
					return Optional.of(criteriaContext.getCriteriaBuilder().isNull(from.get(propertyName)));
				}
				
				switch (administrativeGender) {
					case MALE:
						return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "M"));
					case FEMALE:
						return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "F"));
					case OTHER:
					case UNKNOWN:
					case NULL:
						return Optional.of(criteriaContext.getCriteriaBuilder().isNull(from.get(propertyName)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), token.getValue()));
		});
	}
	
	public <V, U> Optional<Predicate> handleLocationReference(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        @Nonnull From<?, ?> locationAlias, ReferenceAndListParam locationReferences) {
		
		if (locationReferences == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), locationReferences, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Location.SP_NAME:
						return propertyLike(criteriaContext, locationAlias, "name", token.getValue());
					case Location.SP_ADDRESS_CITY:
						return propertyLike(criteriaContext, locationAlias, "cityVillage", token.getValue());
					case Location.SP_ADDRESS_STATE:
						return propertyLike(criteriaContext, locationAlias, "stateProvince", token.getValue());
					case Location.SP_ADDRESS_POSTALCODE:
						return propertyLike(criteriaContext, locationAlias, "postalCode", token.getValue());
					case Location.SP_ADDRESS_COUNTRY:
						return propertyLike(criteriaContext, locationAlias, "country", token.getValue());
				}
			} else {
				return Optional.of(criteriaContext.getCriteriaBuilder().equal(locationAlias.get("uuid"), token.getIdPart()));
			}
			
			return Optional.empty();
		});
	}
	
	public <T, U> Optional<Predicate> handleMedicationReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull From<?, ?> medicationAlias, ReferenceAndListParam medicationReference) {
		if (medicationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), medicationReference, token -> Optional
		        .of(criteriaContext.getCriteriaBuilder().equal(medicationAlias.get("uuid"), token.getIdPart())));
	}
	
	public <V, U> Optional<Predicate> handleMedicationRequestReference(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        @Nonnull From<?, ?> drugOrderAlias, ReferenceAndListParam drugOrderReference) {
		if (drugOrderReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), drugOrderReference,
		    token -> Optional.of(criteriaContext.getCriteriaBuilder()
		            .equal(getRootOrJoin(criteriaContext, drugOrderAlias).get("uuid"), token.getIdPart())));
	}
	
	// ========== Name Handling ==========
	
	public <V, U> void handleNames(OpenmrsFhirCriteriaContext<V, U> criteriaContext, StringAndListParam name,
	        StringAndListParam given, StringAndListParam family, From<?, ?> person) {
		
		if (name == null && given == null && family == null) {
			return;
		}
		
		Join<?, ?> personNameAliasJoin = criteriaContext.addJoin(person, "names", "pn",
		    (personNameAlias) -> criteriaContext.getCriteriaBuilder().equal(personNameAlias.get("voided"), false));
		
		if (name != null) {
			handleAndListParamAsStream(criteriaContext.getCriteriaBuilder(), name,
			    (nameParam) -> Arrays.stream(StringUtils.split(nameParam.getValue(), " \t,"))
			            .map(token -> new StringParam().setValue(token).setExact(nameParam.isExact())
			                    .setContains(nameParam.isContains()))
			            .map(tokenParam -> Arrays.asList(
			                propertyLike(criteriaContext, personNameAliasJoin, "givenName", tokenParam),
			                propertyLike(criteriaContext, personNameAliasJoin, "middleName", tokenParam),
			                propertyLike(criteriaContext, personNameAliasJoin, "familyName", tokenParam)))
			            .flatMap(Collection::stream)).ifPresent(criteriaContext::addPredicate);
		}
		
		if (given != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), given,
			    (givenName) -> propertyLike(criteriaContext, personNameAliasJoin, "givenName", givenName))
			            .ifPresent(criteriaContext::addPredicate);
		}
		
		if (family != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), family,
			    (familyName) -> propertyLike(criteriaContext, personNameAliasJoin, "familyName", familyName))
			            .ifPresent(criteriaContext::addPredicate);
		}
	}
	
	// ========== Participant Handling ==========
	
	public <T, U> Optional<Predicate> handleParticipantReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam participantReference, From<?, ?> epJoin) {
		if (participantReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), participantReference, participantToken -> {
			if (participantToken.getChain() != null) {
				switch (participantToken.getChain()) {
					case Practitioner.SP_IDENTIFIER:
						criteriaContext.addJoin(epJoin, "provider", "p");
						return criteriaContext.getJoin("p").map(providerJoin -> criteriaContext.getCriteriaBuilder()
						        .like(providerJoin.get("identifier"), participantToken.getValue()));
					case Practitioner.SP_GIVEN: {
						Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
						Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
						    "ps");
						Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson, "names",
						    "pn");
						
						return Optional.of(criteriaContext.getCriteriaBuilder()
						        .like(encounterProviderPersonName.get("givenName"), participantToken.getValue()));
					}
					case Practitioner.SP_FAMILY: {
						Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
						Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
						    "ps");
						Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson, "names",
						    "pn");
						
						return Optional.of(criteriaContext.getCriteriaBuilder()
						        .like(encounterProviderPersonName.get("familyName"), participantToken.getValue()));
					}
					case Practitioner.SP_NAME: {
						Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
						Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
						    "ps");
						Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson, "names",
						    "pn");
						
						List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
						
						for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
							predicateList
							        .add(propertyLike(criteriaContext, encounterProviderPersonName, "givenName", token));
							predicateList
							        .add(propertyLike(criteriaContext, encounterProviderPersonName, "middleName", token));
							predicateList
							        .add(propertyLike(criteriaContext, encounterProviderPersonName, "familyName", token));
						}
						
						return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(predicateList)));
					}
				}
			} else {
				Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
				return Optional.of(criteriaContext.getCriteriaBuilder().equal(encounterProviderProvider.get("uuid"),
				    participantToken.getValue()));
			}
			
			return Optional.empty();
		});
	}
	
	// ========== Patient Reference Handling ==========
	
	public <T, U> void handlePatientReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam patientReference) {
		handlePatientReference(criteriaContext, patientReference, "patient");
	}
	
	public <T, U> void handlePatientReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam patientReference, String associationPath) {
		if (patientReference != null && patientReference.size() > 0) {
			Join<?, ?> personJoin = criteriaContext.addJoin(associationPath, "p");
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), patientReference, patientToken -> {
				if (patientToken.getChain() != null) {
					switch (patientToken.getChain()) {
						case Patient.SP_IDENTIFIER:
							Join<?, ?> associationPathIdentifiersJoin = criteriaContext.addJoin(criteriaContext
							        .getCriteriaBuilder().treat((Join<?, Person>) personJoin, org.openmrs.Patient.class),
							    "identifiers", "pi");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathIdentifiersJoin.get("identifier"), patientToken.getValue()));
						case Patient.SP_GIVEN: {
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(personJoin, "names", "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathNamesJoin.get("givenName"), patientToken.getValue()));
						}
						case Patient.SP_FAMILY: {
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(personJoin, "names", "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathNamesJoin.get("familyName"), patientToken.getValue()));
						}
						case Patient.SP_NAME:
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(personJoin, "names", "pn");
							
							List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(patientToken.getValue(), " \t,")) {
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "givenName", token));
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "middleName", token));
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "familyName", token));
							}
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(criterionList)));
					}
				} else {
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(personJoin.get("uuid"), patientToken.getIdPart()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaContext::addPredicate);
		}
	}
	
	// ========== Address Handling ==========
	
	public <V, U> Optional<Predicate> handlePersonAddress(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        From<?, ?> aliasPrefix, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	        StringAndListParam country) {
		if (city == null && state == null && postalCode == null && country == null) {
			return Optional.empty();
		}
		
		List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
		
		if (city != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), city,
			    c -> propertyLike(criteriaContext, aliasPrefix, "cityVillage", c)));
		}
		
		if (state != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), state,
			    c -> propertyLike(criteriaContext, aliasPrefix, "stateProvince", c)));
		}
		
		if (postalCode != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), postalCode,
			    c -> propertyLike(criteriaContext, aliasPrefix, "postalCode", c)));
		}
		
		if (country != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), country,
			    c -> propertyLike(criteriaContext, aliasPrefix, "country", c)));
		}
		
		return Optional.of(criteriaContext.getCriteriaBuilder().and(toCriteriaArray(predicateList.stream())));
	}
	
	// ========== Provider Reference Handling ==========
	
	// Added this method to allow handling classes with provider instead of encounterProvider
	public <T, U> void handleProviderReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam providerReference) {
		if (providerReference != null) {
			Join<?, ?> orderer = criteriaContext.addJoin("orderer", "ord");
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), providerReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							return Optional.of(criteriaContext.getCriteriaBuilder().like(orderer.get("identifier"),
							    participantToken.getValue()));
						case Practitioner.SP_GIVEN: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder().like(ordererName.get("givenName"),
							    participantToken.getValue()));
						}
						case Practitioner.SP_FAMILY: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder().like(ordererName.get("familyName"),
							    participantToken.getValue()));
						}
						case Practitioner.SP_NAME: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								predicateList.add(propertyLike(criteriaContext, ordererName, "givenName", token));
								predicateList.add(propertyLike(criteriaContext, ordererName, "middleName", token));
								predicateList.add(propertyLike(criteriaContext, ordererName, "familyName", token));
							}
							
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(predicateList)));
						}
					}
				} else {
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(orderer.get("uuid"), participantToken.getIdPart()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaContext::addPredicate);
		}
	}
	
	// ========== Quantity Handling ==========
	
	/**
	 * A handler for a {@link QuantityParam}, which represents a quantity and a comparator
	 *
	 * @param criteriaContext the active {@link BaseFhirCriteriaHolder} for the current query
	 * @param propertyName the name of the property in the query to use
	 * @param quantityParam the {@link QuantityParam} to handle
	 * @return a {@link Predicate} to be added to the query for the indicated date parameter
	 */
	public <T> Optional<Predicate> handleQuantity(BaseFhirCriteriaHolder<T> criteriaContext, String propertyName,
	        QuantityParam quantityParam) {
		if (quantityParam == null) {
			return Optional.empty();
		}
		
		BigDecimal value = quantityParam.getValue();
		if (quantityParam.getPrefix() == null || quantityParam.getPrefix() == ParamPrefixEnum.APPROXIMATE) {
			String plainString = quantityParam.getValue().toPlainString();
			int dotIdx = plainString.indexOf('.');
			
			BigDecimal approxRange = APPROX_RANGE.multiply(value);
			if (dotIdx == -1) {
				double lowerBound = value.subtract(approxRange).doubleValue();
				double upperBound = value.add(approxRange).doubleValue();
				return Optional.of(criteriaContext.getCriteriaBuilder().between(criteriaContext.getRoot().get(propertyName),
				    lowerBound, upperBound));
			} else {
				int precision = plainString.length() - (dotIdx);
				double mul = Math.pow(10, -precision);
				double val = mul * 5.0d;
				double lowerBound = value.subtract(new BigDecimal(val)).doubleValue();
				double upperBound = value.add(new BigDecimal(val)).doubleValue();
				return Optional.of(criteriaContext.getCriteriaBuilder().between(criteriaContext.getRoot().get(propertyName),
				    lowerBound, upperBound));
			}
		} else {
			double val = value.doubleValue();
			switch (quantityParam.getPrefix()) {
				case EQUAL:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get(propertyName), val));
				case NOT_EQUAL:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().notEqual(criteriaContext.getRoot().get(propertyName), val));
				case LESSTHAN_OR_EQUALS:
					return Optional.of(criteriaContext.getCriteriaBuilder()
					        .lessThanOrEqualTo(criteriaContext.getRoot().get(propertyName), val));
				case LESSTHAN:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), val));
				case GREATERTHAN_OR_EQUALS:
					return Optional.of(criteriaContext.getCriteriaBuilder()
					        .greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName), val));
				case GREATERTHAN:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().greaterThan(criteriaContext.getRoot().get(propertyName), val));
			}
		}
		
		return Optional.empty();
	}
	
	public <T> Optional<Predicate> handleQuantity(BaseFhirCriteriaHolder<T> criteriaContext, @Nonnull String propertyName,
	        QuantityAndListParam quantityAndListParam) {
		if (quantityAndListParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), quantityAndListParam,
		    quantityParam -> handleQuantity(criteriaContext, propertyName, quantityParam));
	}
	
	// ========== Order Handling ==========
	
	public <V, U> Predicate handleQueryForActiveOrders(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext) {
		return handleQueryForActiveOrders(criteriaContext, new Date());
	}
	
	public <V, U> Predicate handleQueryForActiveOrders(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        Date onDate) {
		return handleQueryForActiveOrders(criteriaContext, "", onDate);
	}
	
	public <V, U> Predicate handleQueryForActiveOrders(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        String path) {
		return handleQueryForActiveOrders(criteriaContext, path, new Date());
	}
	
	public <V, U> Predicate handleQueryForActiveOrders(OpenmrsFhirCriteriaContext<V, U> criteriaContext, String path,
	        Date onDate) {
		// ACTIVE = date activated null or less than or equal to current datetime, date stopped null or in the future, auto expire date null or in the future
		return criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateActivated")),
		        criteriaContext.getCriteriaBuilder().lessThan(getRootOrJoin(criteriaContext, path).get("dateActivated"),
		            onDate)),
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateStopped")),
		        criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("dateStopped"),
		            onDate)),
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("autoExpireDate")),
		        criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("autoExpireDate"),
		            onDate)));
	}
	
	public <V, U> Predicate handleQueryForCancelledOrders(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        @Nullable String path) {
		Date now = new Date();
		
		return criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateStopped")),
		    criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("dateStopped"), now));
	}
	
	// ========== CodeableConcept System Querying ==========
	
	public <V, U> Optional<Predicate> handleQueryForSystem(OpenmrsFhirCriteriaContext<V, U> criteriaContext, String system,
	        List<String> codes, String conceptReferenceTermAlias) {
		OpenmrsFhirCriteriaSubquery<FhirConceptSource, String> conceptSourceSubquery = criteriaContext
		        .addSubquery(FhirConceptSource.class, String.class);
		conceptSourceSubquery.addPredicate(
		    conceptSourceSubquery.getCriteriaBuilder().equal(conceptSourceSubquery.getRoot().get("url"), system));
		conceptSourceSubquery.getSubquery().select(conceptSourceSubquery.getRoot().get("conceptSource"));
		
		return criteriaContext.getJoin(conceptReferenceTermAlias)
		        .map((conceptReferenceTermJoin) -> criteriaContext.getCriteriaBuilder().and(
		            criteriaContext.getCriteriaBuilder().in(conceptReferenceTermJoin.get("conceptSource"))
		                    .value(conceptSourceSubquery.finalizeQuery()),
		            criteriaContext.getCriteriaBuilder().in(conceptReferenceTermJoin.get("code")).value(codes)));
	}
	
	// ========== String Matching ==========
	
	/**
	 * This function returns a {@link Optional<Predicate>} which, if present, contains a "like" query
	 * for a specified property on the specified table. If a predicate is returned it is always a prefix
	 * search, meaning that the generated query will have a clause like: <pre>@code{
	 *     WHERE <propertyName> LIKE '<value>%'
	 * }</pre>
	 *
	 * @param criteriaContext The {@link BaseFhirCriteriaHolder} for the current criteriaContext
	 * @param from The {@link From} object representing the table to get the property from
	 * @param propertyName The name of the property to look for
	 * @param value The string-value to match the prefix of the property against. Note that if the
	 *            supplied value is a null or empty string, no predicate is returned.
	 * @return An {@link Optional<Predicate>} to check this property for this value.
	 * @param <T> The root type of the criteriaContext
	 */
	public <T> Optional<Predicate> propertyLike(@Nonnull BaseFhirCriteriaHolder<T> criteriaContext, @Nonnull From<?, ?> from,
	        @Nonnull String propertyName, @javax.annotation.Nullable String value) {
		if (value == null || value.trim().isEmpty()) {
			return Optional.empty();
		}
		
		return propertyLike(criteriaContext, from, propertyName, new StringParam(value));
	}
	
	/**
	 * This function returns a {@link Optional<Predicate>} which, if present, contains a "like" query
	 * for a specified property on the specified table. If a predicate is returned it is always a prefix
	 * search, meaning that the generated query will have a clause like: <pre>@code{
	 *     WHERE <propertyName> LIKE '<value>%'
	 * }</pre>
	 *
	 * @param criteriaContext The {@link BaseFhirCriteriaHolder} for the current criteriaContext
	 * @param from The {@link From} object representing the table to get the property from
	 * @param propertyName The name of the property to look for
	 * @param param A {@link StringParam} that describes the value to search for, including whether it
	 *            is intended to be an exact match or a contains query. Note that a null param or an
	 *            empty string that is not an exact match will not return a predicate
	 * @return An {@link Optional<Predicate>} to check this property for this value.
	 * @param <T> The root type of the criteriaContext
	 */
	public <T> Optional<Predicate> propertyLike(BaseFhirCriteriaHolder<T> criteriaContext, From<?, ?> from,
	        @Nonnull String propertyName, StringParam param) {
		if (param == null || (!param.isExact() && param.getValue().trim().isEmpty())) {
			return Optional.empty();
		}
		
		Predicate likePredicate;
		if (param.isExact()) {
			likePredicate = criteriaContext.getCriteriaBuilder().equal(from.get(propertyName), param.getValue());
		} else if (param.isContains()) {
			likePredicate = criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "%" + param.getValue() + "%");
		} else {
			likePredicate = criteriaContext.getCriteriaBuilder().like(from.get(propertyName), param.getValue() + "%");
		}
		
		return Optional.of(likePredicate);
	}
}
