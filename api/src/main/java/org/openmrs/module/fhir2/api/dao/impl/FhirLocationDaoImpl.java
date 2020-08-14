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

import javax.validation.constraints.NotNull;

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
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirLocationDaoImpl extends BaseFhirDao<Location> implements FhirLocationDao {
	
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
					        .forEach(param -> handleParentLocation(criteria, (ReferenceAndListParam) param.getParam()));
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
	public List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(Location location,
	        String locationAttributeTypeUuid) {
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
	
	private void handleParentLocation(Criteria criteria, ReferenceAndListParam parent) {
		handleLocationReference("loc", parent).ifPresent(loc -> criteria.createAlias("parentLocation", "loc").add(loc));
	}
	
	@Override
	protected String paramToProp(@NotNull String paramName) {
		switch (paramName) {
			case "name":
				return "name";
			case "address-city":
				return "cityVillage";
			case "address-state":
				return "stateProvince";
			case "address-country":
				return "country";
			case "address-postalCode":
				return "postalCode";
			default:
				return null;
		}
	}
	
}
