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
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
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
	public Collection<Location> searchForLocations(StringOrListParam name, StringOrListParam city, StringOrListParam country,
	        StringOrListParam postalCode, StringOrListParam state, TokenOrListParam tag, ReferenceOrListParam parent,
	        SortSpec sort) {
		
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
	
	private void handleName(Criteria criteria, StringOrListParam namePattern) {
		if (namePattern != null) {
			handleOrListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteria::add);
		}
	}
	
	private void handleCity(Criteria criteria, StringOrListParam cityPattern) {
		if (cityPattern != null) {
			handleOrListParam(cityPattern, (city) -> propertyLike("cityVillage", city)).ifPresent(criteria::add);
		}
	}
	
	private void handleCountry(Criteria criteria, StringOrListParam countryPattern) {
		if (countryPattern != null) {
			handleOrListParam(countryPattern, (country) -> propertyLike("country", country)).ifPresent(criteria::add);
		}
	}
	
	private void handlePostalCode(Criteria criteria, StringOrListParam postalCodePattern) {
		if (postalCodePattern != null) {
			handleOrListParam(postalCodePattern, (postalCode) -> propertyLike("postalCode", postalCode))
			        .ifPresent(criteria::add);
		}
	}
	
	private void handleState(Criteria criteria, StringOrListParam statePattern) {
		if (statePattern != null) {
			handleOrListParam(statePattern, (state) -> propertyLike("stateProvince", state)).ifPresent(criteria::add);
		}
	}
	
	private void handleTag(Criteria criteria, TokenOrListParam tags) {
		if (tags != null) {
			criteria.createAlias("tags", "t");
			handleOrListParam(tags, (tag) -> Optional.of(eq("t.name", tag.getValue()))).ifPresent(criteria::add);
		}
	}
	
	private void handleParentLocation(Criteria criteria, ReferenceOrListParam parent) {
		if (parent != null) {
			DetachedCriteria criteriaForParent = DetachedCriteria.forClass(Location.class);
			handleOrListParam(parent, this::handleParentReference).ifPresent(criteriaForParent::add);
			criteriaForParent.setProjection(Projections.property("locationId"));
			criteria.add(Subqueries.propertyIn("parentLocation.locationId", criteriaForParent));
		}
	}
	
	private Optional<Criterion> handleParentReference(ReferenceParam parentReference) {
		if (parentReference != null) {
			if (parentReference.getChain() != null) {
				switch (parentReference.getChain()) {
					case "name":
						return propertyLike("name", parentReference.getValue());
					case "address-city":
						return propertyLike("cityVillage", parentReference.getValue());
					case "address-state":
						return propertyLike("stateProvince", parentReference.getValue());
					case "address-country":
						return propertyLike("country", parentReference.getValue());
					case "address-postalcode":
						return propertyLike("postalCode", parentReference.getValue());
					case "":
						return Optional.of(eq("uuid", parentReference.getValue()));
				}
			}
		}
		
		return Optional.empty();
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
