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
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Restrictions.sqlRestriction;
import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Person.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Person.SP_NAME;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.StringAndListParam;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
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
	protected Collection<Order> paramToProps(@Nonnull SortState sortState) {
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		Criteria criteria = sortState.getCriteria();
		if (param.startsWith("address") && lacksAlias(criteria, "pad")) {
			criteria.createAlias(getAssociationPath("addresses"), "pad", JoinType.LEFT_OUTER_JOIN);
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			if (lacksAlias(criteria, "pn")) {
				criteria.createAlias(getAssociationPath("names"), "pn", JoinType.LEFT_OUTER_JOIN);
			}
			
			String sqlAlias = getSqlAlias();
			
			criteria.add(and(eq("pn.voided", false), or(
			    and(eq("pn.preferred", true),
			        Subqueries.propertyEq("pn.personNameId",
			            DetachedCriteria.forClass(PersonName.class, "pn1").add(eq("pn1.preferred", true))
			                    .add(sqlRestriction(String.format("pn1_.person_id = %s.person_id", sqlAlias)))
			                    .setProjection(Projections.min("pn1.personNameId")))),
			    and(Subqueries.notExists(DetachedCriteria.forClass(PersonName.class, "pn2").add(eq("pn2.preferred", true))
			            // WARNING this is fragile
			            .add(sqlRestriction(String.format("pn2_.person_id = %s.person_id", sqlAlias)))
			            .setProjection(Projections.id())),
			        Subqueries.propertyEq("pn.personNameId",
			            DetachedCriteria.forClass(PersonName.class, "pn3").add(eq("pn3.preferred", false))
			                    // WARNING this is fragile
			                    .add(sqlRestriction(String.format("pn3_.person_id = %s.person_id", sqlAlias)))
			                    .setProjection(Projections.min("pn3.personNameId")))),
			    isNull("pn.personNameId"))));
			
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
			
			switch (sortState.getSortOrder()) {
				case ASC:
					return Arrays.stream(properties).map(Order::asc).collect(Collectors.toList());
				case DESC:
					return Arrays.stream(properties).map(Order::desc).collect(Collectors.toList());
			}
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
	
	protected void handleAddresses(Criteria criteria, Map.Entry<String, List<PropParam<?>>> entry) {
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
		
		handlePersonAddress("pad", city, state, postalCode, country).ifPresent(c -> {
			criteria.createAlias(getAssociationPath("addresses"), "pad");
			criteria.add(c);
		});
	}
	
	protected void handleNames(Criteria criteria, List<PropParam<?>> params) {
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
		
		handleNames(criteria, name, given, family, getPersonProperty());
	}
	
	private String getAssociationPath(String property) {
		String personProperty = getPersonProperty();
		return personProperty == null ? property : personProperty + "." + property;
	}
}
