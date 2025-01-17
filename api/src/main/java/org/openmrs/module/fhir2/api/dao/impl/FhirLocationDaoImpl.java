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
import static org.hibernate.criterion.Restrictions.or;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.sql.JoinType;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirLocationDaoImpl extends BaseFhirDao<Location> implements FhirLocationDao {
	
	private static final int SUPPORTED_LOCATION_HIERARCHY_SEARCH_DEPTH = 9;
	
	@Autowired
	LocationService locationService;
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleName(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.CITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCity(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.STATE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleState(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.COUNTRY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCountry(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.POSTALCODE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePostalCode(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleLocationReference(criteria, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.TAG_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTag(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(@Nonnull Location location,
	        @Nonnull String locationAttributeTypeUuid) {
		return (List<LocationAttribute>) getSessionFactory().getCurrentSession().createCriteria(LocationAttribute.class)
		        .createAlias("location", "l", JoinType.INNER_JOIN, eq("l.id", location.getId()))
		        .createAlias("attributeType", "lat").add(eq("lat.uuid", locationAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
	
	private void handleName(Criteria criteria, StringAndListParam namePattern) {
		handleAndListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteria::add);
	}
	
	private void handleCity(Criteria criteria, StringAndListParam cityPattern) {
		handleAndListParam(cityPattern, (city) -> propertyLike("cityVillage", city)).ifPresent(criteria::add);
	}
	
	private void handleCountry(Criteria criteria, StringAndListParam countryPattern) {
		handleAndListParam(countryPattern, (country) -> propertyLike("country", country)).ifPresent(criteria::add);
	}
	
	private void handlePostalCode(Criteria criteria, StringAndListParam postalCodePattern) {
		handleAndListParam(postalCodePattern, (postalCode) -> propertyLike("postalCode", postalCode))
		        .ifPresent(criteria::add);
	}
	
	private void handleState(Criteria criteria, StringAndListParam statePattern) {
		handleAndListParam(statePattern, (state) -> propertyLike("stateProvince", state)).ifPresent(criteria::add);
	}
	
	private void handleTag(Criteria criteria, TokenAndListParam tags) {
		if (tags != null) {
			criteria.createAlias("tags", "t");
			handleAndListParam(tags, (tag) -> Optional.of(eq("t.name", tag.getValue()))).ifPresent(criteria::add);
		}
	}
	
	private void handleLocationReference(Criteria criteria, ReferenceAndListParam locationAndReferences) {
		
		if (locationAndReferences == null) {
			return;
		}
		
		List<ReferenceOrListParam> locationOrReference = locationAndReferences.getValuesAsQueryTokens();
		
		if (locationOrReference == null || locationOrReference.isEmpty()) {
			return;
		}
		
		if (locationOrReference.size() > 1) {
			throw new IllegalArgumentException("Only one location reference is supported");
		}
		
		List<ReferenceParam> locationReferences = locationOrReference.get(0).getValuesAsQueryTokens();
		
		if (locationReferences == null || locationReferences.isEmpty()) {
			return;
		}
		
		if (locationReferences.size() > 1) {
			throw new IllegalArgumentException("Only one location reference is supported");
		}
		
		ReferenceParam locationReference = locationReferences.get(0);
		
		// **NOTE: this is a *bug* in the current HAPI FHIR implementation, "below" should be the "queryParameterQualifier", not the resource type; likely need update this when/fix the HAPI FHIR implementation is fixed**
		// this is to support queries of the type "Location?partof=below:uuid"
		if ("below".equalsIgnoreCase(locationReference.getResourceType())) {
			List<Criterion> belowReferenceCriteria = new ArrayList<>();
			
			// we need to add a join to the parentLocation for each level of hierarchy we want to search, and add "equals" criterion for each level
			int depth = 1;
			while (depth <= SUPPORTED_LOCATION_HIERARCHY_SEARCH_DEPTH) {
				belowReferenceCriteria.add(eq("ancestor" + depth + ".uuid", locationReference.getIdPart()));
				criteria.createAlias(depth == 1 ? "parentLocation" : "ancestor" + (depth - 1) + ".parentLocation",
				    "ancestor" + depth, JoinType.LEFT_OUTER_JOIN);
				depth++;
			}
			
			// "or" these call together so that we return the location if any of the joined ancestor location uuids match
			criteria.add(or(belowReferenceCriteria.toArray(new Criterion[0])));
		} else {
			// this is to support queries of the type "Location?partof=uuid" or chained search like "Location?partof:Location=Location:name=xxx"
			handleLocationReference("loc", locationAndReferences)
			        .ifPresent(loc -> criteria.createAlias("parentLocation", "loc").add(loc));
		}
		
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Location.SP_NAME:
				return "name";
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_CITY:
				return "cityVillage";
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_STATE:
				return "stateProvince";
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_COUNTRY:
				return "country";
			case org.hl7.fhir.r4.model.Location.SP_ADDRESS_POSTALCODE:
				return "postalCode";
			default:
				return super.paramToProp(param);
		}
	}
	
	@Override
	public LocationTag getLocationTagByName(String tag) {
		return locationService.getLocationTagByName(tag);
	}
	
	@Override
	public LocationTag saveLocationTag(LocationTag tag) {
		return locationService.saveLocationTag(tag);
	}
	
	@Override
	public LocationAttributeType getLocationAttributeTypeByUuid(String uuid) {
		return locationService.getLocationAttributeTypeByUuid(uuid);
	}
}
