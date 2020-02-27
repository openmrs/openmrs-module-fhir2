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

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.sql.JoinType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPersonDaoImpl extends BaseDaoImpl implements FhirPersonDao {
	
	@Inject
	PersonService personService;
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public Person getPersonByUuid(String uuid) {
		return personService.getPersonByUuid(uuid);
	}
	
	@Override
	public List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(Person person,
	        String personAttributeTypeUuid) {
		return (List<PersonAttribute>) sessionFactory.getCurrentSession().createCriteria(PersonAttribute.class)
		        .createAlias("person", "p", JoinType.INNER_JOIN, eq("p.id", person.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", personAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
	
	@Override
	public Collection<Person> searchForPeople(StringOrListParam name, TokenOrListParam gender, DateRangeParam birthDate,
	        StringOrListParam city, StringOrListParam state, StringOrListParam postalCode, StringOrListParam country,
	        SortSpec sort) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		
		handleNames(criteria, name, null, null);
		handleGender("gender", gender).ifPresent(criteria::add);
		handleDateRange("birthdate", birthDate).ifPresent(criteria::add);
		handlePersonAddress("pad", city, state, postalCode, country).ifPresent(c -> {
			criteria.createAlias("addresses", "pad");
			criteria.add(c);
		});
		if (sort != null) {
			if (sort.getParamName().equals("name") && !containsAlias(criteria, "pn")) {
				criteria.createAlias("names", "pn");
			}
			if (sort.getParamName().startsWith("address") && !containsAlias(criteria, "pad")) {
				criteria.createAlias("addresses", "pad");
			}
		}
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	@Override
	protected String paramToProp(String param) {
		switch (param) {
			case "name":
				return "pn.givenName";
			case "birthdate":
				return "birthdate";
			case "address-city":
				return "pad.cityVillage";
			case "address-state":
				return "pad.stateProvince";
			case "address-postalCode":
				return "pad.postalCode";
			case "address-country":
				return "pad.country";
			default:
				return null;
		}
	}
	
}
