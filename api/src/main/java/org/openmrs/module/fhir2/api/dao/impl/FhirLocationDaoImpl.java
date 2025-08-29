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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirLocationDaoImpl extends BaseFhirDao<Location> implements FhirLocationDao {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private LocationService locationService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public Location get(@Nonnull Integer id) {
		return locationService.getLocation(id);
	}
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Location, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleName(criteriaContext, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.CITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCity(criteriaContext, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.STATE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleState(criteriaContext, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.COUNTRY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCountry(criteriaContext, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.POSTALCODE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handlePostalCode(criteriaContext, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleLocationReference(criteriaContext, criteriaContext.getRoot(),
					    (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.TAG_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTag(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	@Override
	public List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(@Nonnull Location location,
	        @Nonnull String locationAttributeTypeUuid) {
		OpenmrsFhirCriteriaContext<LocationAttribute, LocationAttribute> criteriaContext = createCriteriaContext(
		    LocationAttribute.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().join("location").get("locationId"),
		        location.getId()),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().join("attributeType").get("uuid"),
		        locationAttributeTypeUuid),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("voided"), false)));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList();
	}
	
	@Override
	public Map<Location, List<LocationAttribute>> getActiveAttributesByLocationsAndAttributeTypeUuid(
	        @Nonnull Collection<Location> location, @Nonnull String locationAttributeTypeUuid) {
		final CriteriaBuilder criteriaBuilder = getSessionFactory().getCurrentSession().getCriteriaBuilder();
		final CriteriaQuery<LocationAttribute> criteria = criteriaBuilder.createQuery(LocationAttribute.class);
		final Root<LocationAttribute> locationAttributeRoot = criteria.from(LocationAttribute.class);
		
		final Predicate byId = locationAttributeRoot.get("location").get("locationId")
		        .in(location.stream().map(Location::getLocationId).collect(toList()));
		final Predicate byAttributeType = criteriaBuilder.equal(locationAttributeRoot.get("attributeType").get("uuid"),
		    locationAttributeTypeUuid);
		final Predicate byVoided = criteriaBuilder.equal(locationAttributeRoot.get("voided"), false);
		
		criteria.where(byId, byAttributeType, byVoided);
		
		return getSessionFactory().getCurrentSession().createQuery(criteria).getResultList().stream()
		        .collect(groupingBy(LocationAttribute::getLocation));
	}
	
	private <U> void handleName(OpenmrsFhirCriteriaContext<Location, U> criteriaContext, StringAndListParam namePattern) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), namePattern,
		    (name) -> propertyLike(criteriaContext, criteriaContext.getRoot(), "name", name))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleCity(OpenmrsFhirCriteriaContext<Location, U> criteriaContext, StringAndListParam cityPattern) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), cityPattern,
		    (city) -> propertyLike(criteriaContext, criteriaContext.getRoot(), "cityVillage", city))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleCountry(OpenmrsFhirCriteriaContext<Location, U> criteriaContext,
	        StringAndListParam countryPattern) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), countryPattern,
		    (country) -> propertyLike(criteriaContext, criteriaContext.getRoot(), "country", country))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handlePostalCode(OpenmrsFhirCriteriaContext<Location, U> criteriaContext,
	        StringAndListParam postalCodePattern) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), postalCodePattern,
		    (postalCode) -> propertyLike(criteriaContext, criteriaContext.getRoot(), "postalCode", postalCode))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleState(OpenmrsFhirCriteriaContext<Location, U> criteriaContext, StringAndListParam statePattern) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), statePattern,
		    (state) -> propertyLike(criteriaContext, criteriaContext.getRoot(), "stateProvince", state))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleTag(OpenmrsFhirCriteriaContext<Location, U> criteriaContext, TokenAndListParam tags) {
		if (tags != null) {
			criteriaContext.addJoin("tags", "t");
			handleAndListParam(criteriaContext.getCriteriaBuilder(), tags,
			    (tag) -> criteriaContext.getJoin("t").map(
			        locationTag -> criteriaContext.getCriteriaBuilder().equal(locationTag.get("name"), tag.getValue())))
			                .ifPresent(criteriaContext::addPredicate);
		}
	}
	
	protected <T, U> Optional<Predicate> handleLocationReference(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
			 @Nonnull From<?, ?> locationAlias, ReferenceAndListParam locationAndReferences) {
		if (locationAndReferences == null) {
			return Optional.empty();
		}
		
		List<ReferenceOrListParam> locationOrReference = locationAndReferences.getValuesAsQueryTokens();
		
		if (locationOrReference == null || locationOrReference.isEmpty()) {
			return Optional.empty();
		}
		
		if (locationOrReference.size() > 1) {
			throw new IllegalArgumentException("Only one location reference is supported");
		}
		
		List<ReferenceParam> locationReferences = locationOrReference.get(0).getValuesAsQueryTokens();
		
		if (locationReferences == null || locationReferences.isEmpty()) {
			return Optional.empty();
		}
		
		if (locationReferences.size() > 1) {
			throw new IllegalArgumentException("Only one location reference is supported");
		}
		
		ReferenceParam locationReference = locationReferences.get(0);
		
		// TODO Fix this code
		// **NOTE: this is a *bug* in the current HAPI FHIR implementation, "below" should be the "queryParameterQualifier",
		// not the resource type; likely need update this when/fix the HAPI FHIR implementation is fixed**
		// this is to support queries of the type "Location?partof=below:uuid"
		if ("below".equalsIgnoreCase(locationReference.getResourceType())) {
			//			int searchDepth = globalPropertyService
			//			        .getGlobalPropertyAsInteger(FhirConstants.SUPPORTED_LOCATION_HIERARCHY_SEARCH_DEPTH, 5);
			//
			//			List<Predicate> belowReferenceCriteria = new ArrayList<>();
			//
			//			// we need to add a join to the parentLocation for each level of hierarchy we want to search, and add "equals" criterion for each level
			//			int depth = 1;
			//			while (depth <= searchDepth) {
			//				belowReferenceCriteria.add(eq("ancestor" + depth + ".uuid", locationReference.getIdPart()));
			//				criteria.createAlias(depth == 1 ? "parentLocation" : "ancestor" + (depth - 1) + ".parentLocation",
			//				    "ancestor" + depth, JoinType.LEFT_OUTER_JOIN);
			//				depth++;
			//			}
			//
			//			// "or" these call together so that we return the location if any of the joined ancestor location uuids match
			//			criteria.add(or(belowReferenceCriteria.toArray(new Criterion[0])));
			return Optional.empty();
		} else {
			// this is to support queries of the type "Location?partof=uuid" or chained search like "Location?partof:Location=Location:name=xxx"
			return super.<T, U> handleLocationReference(criteriaContext, locationAlias, locationAndReferences);
		}
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Location.SP_NAME:
				return criteriaContext.getRoot().get("name");
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_CITY:
				return criteriaContext.getRoot().get("cityVillage");
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_STATE:
				return criteriaContext.getRoot().get("stateProvince");
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_COUNTRY:
				return criteriaContext.getRoot().get("country");
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_POSTALCODE:
				return criteriaContext.getRoot().get("postalCode");
			default:
				return super.paramToProp(criteriaContext, param);
			
		}
	}
	
	@Override
	public LocationTag getLocationTagByName(@Nonnull String tag) {
		return locationService.getLocationTagByName(tag);
	}
	
	@Override
	public LocationTag createLocationTag(@Nonnull LocationTag tag) {
		return locationService.saveLocationTag(tag);
	}
	
	@Override
	public LocationAttributeType getLocationAttributeTypeByUuid(@Nonnull String uuid) {
		return locationService.getLocationAttributeTypeByUuid(uuid);
	}
}
