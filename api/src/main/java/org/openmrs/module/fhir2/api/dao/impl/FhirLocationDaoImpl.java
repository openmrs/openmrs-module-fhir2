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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
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
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<Location> criteriaContext, SearchParameterMap theParams) {
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
					entry.getValue().forEach(
					    param -> handleParentLocation(criteriaContext, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.TAG_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTag(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaContext::addPredicate);
					criteriaContext.finalizeQuery();
					break;
			}
		});
	}
	
	@Override
	public List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(@Nonnull Location location,
	        @Nonnull String locationAttributeTypeUuid) {
		OpenmrsFhirCriteriaContext<LocationAttribute> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder()
				.equal(criteriaContext.getRoot().join("location").get("id"), location.getId()));
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder()
				.equal(criteriaContext.getRoot().join("attributeType").get("uuid"), locationAttributeTypeUuid));
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder()
				.equal(criteriaContext.getRoot().get("voided"), false));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
	}
	
	private void handleName(OpenmrsFhirCriteriaContext<Location> criteriaContext, StringAndListParam namePattern) {
		handleAndListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	private void handleCity(OpenmrsFhirCriteriaContext<Location> criteriaContext, StringAndListParam cityPattern) {
		handleAndListParam(cityPattern, (city) -> propertyLike("cityVillage", city)).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	private void handleCountry(OpenmrsFhirCriteriaContext<Location> criteriaContext, StringAndListParam countryPattern) {
		handleAndListParam(countryPattern, (country) -> propertyLike("country", country)).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	private void handlePostalCode(OpenmrsFhirCriteriaContext<Location> criteriaContext, StringAndListParam postalCodePattern) {
		handleAndListParam(postalCodePattern, (postalCode) -> propertyLike("postalCode", postalCode))
		        .ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	private void handleState(OpenmrsFhirCriteriaContext<Location> criteriaContext, StringAndListParam statePattern) {
		handleAndListParam(statePattern, (state) -> propertyLike("stateProvince", state)).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	private void handleTag(OpenmrsFhirCriteriaContext<Location> criteriaContext, TokenAndListParam tags) {
		if (tags != null) {
			criteriaContext.getRoot().join("tags").alias("t");
			handleAndListParam(tags, (tag) -> Optional.of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("t.name"), tag.getValue())))
			        .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	private void handleParentLocation(OpenmrsFhirCriteriaContext<Location> criteriaContext, ReferenceAndListParam parent) {
		handleLocationReference("loc", parent).ifPresent(loc -> {
			criteriaContext.getRoot().join("parentLocation").alias("loc");
			criteriaContext.addPredicate(loc);
			criteriaContext.finalizeQuery();
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
	
	protected OpenmrsFhirCriteriaContext<LocationAttribute> openmrsFhirCriteriaContext() {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<LocationAttribute> cq = cb.createQuery(LocationAttribute.class);
		Root<LocationAttribute> root = cq.from(LocationAttribute.class);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
}
