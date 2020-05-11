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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.sql.JoinType;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirLocationDaoImpl extends BaseDao implements FhirLocationDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public Location getLocationByUuid(String uuid) {
		return (Location) sessionFactory.getCurrentSession().createCriteria(Location.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	public Collection<Location> searchForLocations(StringAndListParam name, StringAndListParam city,
	        StringAndListParam country, StringAndListParam postalCode, StringAndListParam state, TokenAndListParam tag,
	        ReferenceAndListParam parent, SortSpec sort) {
		
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Location.class);
		
		handleBooleanProperty("retired", false).ifPresent(criteria::add);
		handleName(criteria, name);
		handleCity(criteria, city);
		handleCountry(criteria, country);
		handlePostalCode(criteria, postalCode);
		handleState(criteria, state);
		handleTag(criteria, tag);
		handleParentLocation(criteria, parent);
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	@Override
	public List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(Location location,
	        String locationAttributeTypeUuid) {
		return (List<LocationAttribute>) sessionFactory.getCurrentSession().createCriteria(LocationAttribute.class)
		        .createAlias("location", "l", JoinType.INNER_JOIN, eq("l.id", location.getId()))
		        .createAlias("attributeType", "lat").add(eq("lat.uuid", locationAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
	
	private void handleName(Criteria criteria, StringAndListParam namePattern) {
		if (namePattern != null) {
			handleAndListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteria::add);
		}
	}
	
	private void handleCity(Criteria criteria, StringAndListParam cityPattern) {
		if (cityPattern != null) {
			handleAndListParam(cityPattern, (city) -> propertyLike("cityVillage", city)).ifPresent(criteria::add);
		}
	}
	
	private void handleCountry(Criteria criteria, StringAndListParam countryPattern) {
		if (countryPattern != null) {
			handleAndListParam(countryPattern, (country) -> propertyLike("country", country)).ifPresent(criteria::add);
		}
	}
	
	private void handlePostalCode(Criteria criteria, StringAndListParam postalCodePattern) {
		if (postalCodePattern != null) {
			handleAndListParam(postalCodePattern, (postalCode) -> propertyLike("postalCode", postalCode))
			        .ifPresent(criteria::add);
		}
	}
	
	private void handleState(Criteria criteria, StringAndListParam statePattern) {
		if (statePattern != null) {
			handleAndListParam(statePattern, (state) -> propertyLike("stateProvince", state)).ifPresent(criteria::add);
		}
	}
	
	private void handleTag(Criteria criteria, TokenAndListParam tags) {
		if (tags != null) {
			criteria.createAlias("tags", "t");
			handleAndListParam(tags, (tag) -> Optional.of(eq("t.name", tag.getValue()))).ifPresent(criteria::add);
		}
	}
	
	private void handleParentLocation(Criteria criteria, ReferenceAndListParam parent) {
		if (parent != null) {
			handleLocationReference("loc", parent).ifPresent(loc -> criteria.createAlias("parentLocation", "loc").add(loc));
		}
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
