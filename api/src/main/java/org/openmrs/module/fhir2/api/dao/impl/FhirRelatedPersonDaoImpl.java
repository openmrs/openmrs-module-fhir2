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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirRelatedPersonDaoImpl extends BaseFhirDao<Relationship> implements FhirRelatedPersonDao {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		criteria.createAlias("personA", "m"); //inner join with person table
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleNames(criteria, (StringAndListParam) param.getParam(), null, null, "m"));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleGender("m.gender", (TokenAndListParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange("m.birthdate", (DateRangeParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteria, entry);
					break;
			}
		});
	}
	
	@Override
	protected Collection<Order> paramToProps(SortState sortState) {
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		Criteria criteria = sortState.getCriteria();
		if (param.startsWith("address") && !containsAlias(criteria, "pad")) {
			criteria.createAlias("m.addresses", "pad", JoinType.LEFT_OUTER_JOIN);
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			if (!containsAlias(criteria, "pn")) {
				criteria.createAlias("m.names", "pn", JoinType.LEFT_OUTER_JOIN);
			}
			
			criteria.add(and(eq("pn.voided", false), or(
			    and(eq("pn.preferred", true),
			        Subqueries.propertyEq("pn.personNameId",
			            DetachedCriteria.forClass(PersonName.class, "pn1").add(eq("pn1.preferred", true))
			                    .add(sqlRestriction(String.format("pn1_.person_id = %s.person_a", "this_")))
			                    .setProjection(Projections.min("pn1.personNameId")))),
			    and(Subqueries.notExists(DetachedCriteria.forClass(PersonName.class, "pn2").add(eq("pn2.preferred", true))
			            // WARNING this is fragile
			            .add(sqlRestriction(String.format("pn2_.person_id = %s.person_a", "this_")))
			            .setProjection(Projections.id())),
			        Subqueries.propertyEq("pn.personNameId",
			            DetachedCriteria.forClass(PersonName.class, "pn3").add(eq("pn3.preferred", false))
			                    // WARNING this is fragile
			                    .add(sqlRestriction(String.format("pn3_.person_id = %s.person_a", "this_")))
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
	protected String paramToProp(String param) {
		switch (param) {
			case SP_BIRTHDATE:
				return "m.birthdate";
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
	
	private void handleAddresses(Criteria criteria, Map.Entry<String, List<PropParam<?>>> entry) {
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
			criteria.createAlias("m.addresses", "pad");
			criteria.add(c);
		});
	}
}
