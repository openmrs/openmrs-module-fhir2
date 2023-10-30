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

import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Person.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Person.SP_NAME;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.param.StringAndListParam;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PropParam;

/**
 * Base class for Person-related DAO objects. This helps standardise the logic used to search for
 * and sort objects that represent a Person, including {@link Person}, {@link Patient}, but also
 * classes like {@link org.openmrs.Provider} and {@link org.openmrs.User}.
 *
 * @param <T> The OpenMRS object that represents a person.
 */
public abstract class BasePersonDao<T extends OpenmrsObject & Auditable> extends BaseFhirDao<T> {
	
	/**
	 * Returns the sqlAlias of the Person class for queries from this class
	 *
	 * @return the sqlAlias for the Person class for queries from this class
	 */
	protected abstract String getSqlAlias();
	
	/**
	 * This is intended to be overridden by subclasses to provide the property that defines the Person
	 * for this object
	 *
	 * @return the property that points to the person for this object
	 */
	@SuppressWarnings("UnstableApiUsage")
	protected String getPersonProperty() {
		Class<? super T> rawType = typeToken.getRawType();
		if (rawType.equals(Person.class) || rawType.equals(Patient.class)) {
			return null;
		}
		
		return "person";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Collection<Order> paramToProps(@Nonnull SortState sortState) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		Root<T> root = (Root<T>) criteriaQuery.from(typeToken.getRawType());
		
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		CriteriaBuilder cb = sortState.getCriteriaBuilder();
		if (param.startsWith("address") && lacksAlias(cb, "pad")) {
			root.join(getAssociationPath("addresses"), JoinType.LEFT).alias("pad");
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			if (lacksAlias(cb, "pn")) {
				root.join(getAssociationPath("names"), JoinType.LEFT).alias("pn");
			}
			
			Root<PersonName> subRoot = criteriaQuery.subquery(Integer.class).from(PersonName.class);
			
			Predicate predicate = criteriaBuilder
			        .and(criteriaBuilder.equal(root.get("pn").get("voided"), false),
			            criteriaBuilder.or(
			                criteriaBuilder.and(criteriaBuilder.equal(root.get("pn").get("preferred"), true),
			                    criteriaBuilder.equal(root.get("pn").get("personNameId"),
			                        criteriaQuery.subquery(Integer.class)
			                                .select(criteriaBuilder.min(root.get("pn1").get("personNameId")))
			                                .where(criteriaBuilder.and(
			                                    criteriaBuilder.equal(subRoot.get("preferred"), true),
			                                    criteriaBuilder.equal(subRoot.get("person_id"), root.get("person_id")))))),
			                criteriaBuilder.and(
			                    criteriaBuilder.not(criteriaBuilder.exists(
			                        criteriaQuery.subquery(Integer.class).select(root.get("pn2").get("personNameId")).where(
			                            criteriaBuilder.and(criteriaBuilder.equal(subRoot.get("pn2").get("preferred"), true),
			                                criteriaBuilder.equal(subRoot.get("pn2").get("person").get("personId"),
			                                    root.get("personId")))))),
			                    criteriaBuilder.equal(root.get("pn").get("personNameId"),
			                        criteriaQuery.subquery(Integer.class)
			                                .select(criteriaBuilder.min(root.get("pn3").get("personNameId")))
			                                .where(criteriaBuilder.and(
			                                    criteriaBuilder.equal(subRoot.get("pn3").get("preferred"), false),
			                                    criteriaBuilder.equal(subRoot.get("pn3").get("person").get("personId"),
			                                        root.get("personId")))))),
			                criteriaBuilder.isNull(root.get("pn").get("personNameId"))));
			criteriaQuery.where(predicate);
			
			String[] properties = null;
			switch (param) {
				case SP_NAME:
					properties = new String[] { "pn.familyName", "pn.familyName2", "pn.givenName", "pn.middleName",
					        "pn.familyNamePrefix", "pn.familyNameSuffix" };
					break;
				case SP_GIVEN:
					properties = new String[] { "pn.givenName" };
					break;
				case SP_FAMILY:
					properties = new String[] { "pn.familyName" };
					break;
			}
			
			List<Order> sortStateOrders = new ArrayList<>();
			switch (sortState.getSortOrder()) {
				case ASC:
					for (String property : properties) {
						sortStateOrders.add(cb.asc(root.get(property)));
					}
					break;
				case DESC:
					for (String property : properties) {
						sortStateOrders.add(cb.desc(root.get(property)));
					}
					break;
			}
			
			criteriaQuery.orderBy(sortStateOrders);
			return sortStateOrders;
			
		}
		
		return super.paramToProps(sortState);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case SP_BIRTHDATE:
				return "birthdate";
			case SP_ADDRESS_CITY:
				return "pad.cityVillage";
			case SP_ADDRESS_STATE:
				return "pad.stateProvince";
			case SP_ADDRESS_POSTALCODE:
				return "pad.postalCode";
			case SP_ADDRESS_COUNTRY:
				return "pad.country";
			default:
				return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void handleAddresses(CriteriaBuilder criteriaBuilder, Map.Entry<String, List<PropParam<?>>> entry) {
		StringAndListParam city = null;
		StringAndListParam country = null;
		StringAndListParam postalCode = null;
		StringAndListParam state = null;
		for (PropParam<?> param : entry.getValue()) {
			switch (param.getPropertyName()) {
				case FhirConstants.CITY_PROPERTY:
					city = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.STATE_PROPERTY:
					state = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.POSTAL_CODE_PROPERTY:
					postalCode = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.COUNTRY_PROPERTY:
					country = ((StringAndListParam) param.getParam());
					break;
			}
		}
		
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = (CriteriaQuery<T>) em.getCriteriaBuilder().createQuery(typeToken.getRawType());
		Root<T> root = (Root<T>) criteriaQuery.from(typeToken.getRawType());
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handlePersonAddress("pad", city, state, postalCode, country).ifPresent(c -> {
			root.join(getAssociationPath("addresses")).alias("pad");
			finalCriteriaBuilder.and(c);
		});
	}
	
	protected void handleNames(CriteriaBuilder criteriaBuilder, List<PropParam<?>> params) {
		StringAndListParam name = null;
		StringAndListParam given = null;
		StringAndListParam family = null;
		
		for (PropParam<?> param : params) {
			switch (param.getPropertyName()) {
				case FhirConstants.NAME_PROPERTY:
					name = (StringAndListParam) param.getParam();
					break;
				case FhirConstants.GIVEN_PROPERTY:
					given = (StringAndListParam) param.getParam();
					break;
				case FhirConstants.FAMILY_PROPERTY:
					family = (StringAndListParam) param.getParam();
					break;
			}
		}
		
		handleNames(criteriaBuilder, name, given, family, getPersonProperty());
	}
	
	private String getAssociationPath(String property) {
		String personProperty = getPersonProperty();
		return personProperty == null ? property : personProperty + "." + property;
	}
}
