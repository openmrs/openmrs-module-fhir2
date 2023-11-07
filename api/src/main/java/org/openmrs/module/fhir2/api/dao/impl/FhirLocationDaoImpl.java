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
import javax.persistence.criteria.CriteriaBuilder;

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
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
	
	@Autowired
	LocationService locationService;
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleName(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.CITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCity(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.STATE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleState(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.COUNTRY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCountry(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.POSTALCODE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePostalCode(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleParentLocation(criteriaBuilder, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.TAG_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTag(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
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
	
	private void handleName(CriteriaBuilder criteriaBuilder, StringAndListParam namePattern) {
		handleAndListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteriaBuilder::and);
	}
	
	private void handleCity(CriteriaBuilder criteriaBuilder, StringAndListParam cityPattern) {
		handleAndListParam(cityPattern, (city) -> propertyLike("cityVillage", city)).ifPresent(criteriaBuilder::and);
	}
	
	private void handleCountry(CriteriaBuilder criteriaBuilder, StringAndListParam countryPattern) {
		handleAndListParam(countryPattern, (country) -> propertyLike("country", country)).ifPresent(criteriaBuilder::and);
	}
	
	private void handlePostalCode(CriteriaBuilder criteriaBuilder, StringAndListParam postalCodePattern) {
		handleAndListParam(postalCodePattern, (postalCode) -> propertyLike("postalCode", postalCode))
		        .ifPresent(criteriaBuilder::and);
	}
	
	private void handleState(CriteriaBuilder criteriaBuilder, StringAndListParam statePattern) {
		handleAndListParam(statePattern, (state) -> propertyLike("stateProvince", state)).ifPresent(criteriaBuilder::and);
	}
	
	private void handleTag(CriteriaBuilder criteriaBuilder, TokenAndListParam tags) {
		if (tags != null) {
			root.join("tags").alias("t");
			handleAndListParam(tags, (tag) -> Optional.of(criteriaBuilder.equal(root.get("t.name"), tag.getValue()))).ifPresent(criteriaBuilder::and);
		}
	}
	
	private void handleParentLocation(CriteriaBuilder criteriaBuilder, ReferenceAndListParam parent) {
		handleLocationReference("loc", parent).ifPresent(loc -> {
			root.join("parentLocation").alias("loc");
			criteriaBuilder.and(loc);
		});
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
