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
import static org.hibernate.criterion.Restrictions.or;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPatientDaoImpl extends BaseDaoImpl implements FhirPatientDao {
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public Patient getPatientByUuid(String uuid) {
		return (Patient) sessionFactory.getCurrentSession().createCriteria(Patient.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid) {
		List<PatientIdentifierType> identifierTypes = (List<PatientIdentifierType>) sessionFactory.getCurrentSession()
		        .createCriteria(PatientIdentifierType.class)
		        .add(or(and(eq("name", name), eq("retired", false)), eq("uuid", uuid))).list();
		
		if (identifierTypes.isEmpty()) {
			return null;
		} else {
			// favour uuid if one was supplied
			if (uuid != null) {
				try {
					return identifierTypes.stream().filter((idType) -> uuid.equals(idType.getUuid())).findFirst().get();
				}
				catch (NoSuchElementException ignored) {}
			}
			
			return identifierTypes.get(0);
		}
	}
	
	@Override
	public Collection<Patient> searchForPatients(StringOrListParam name, StringOrListParam given, StringOrListParam family,
	        TokenOrListParam identifier, TokenOrListParam gender, DateRangeParam birthDate, DateRangeParam deathDate,
	        TokenOrListParam deceased, StringOrListParam city, StringOrListParam state, StringOrListParam postalCode,
	        SortSpec sort) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		handleNames(criteria, name, given, family);
		handleIdentifier(criteria, identifier);
		handleGender("gender", gender).ifPresent(criteria::add);
		handleDateRange("birthdate", birthDate).ifPresent(criteria::add);
		handleDateRange("deathdate", deathDate).ifPresent(criteria::add);
		handleBoolean("dead", deceased).ifPresent(criteria::add);
		handlePersonAddress("pad", city, state, postalCode, null).ifPresent(c -> {
			criteria.createAlias("addresses", "pad");
			criteria.add(c);
		});
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
}
