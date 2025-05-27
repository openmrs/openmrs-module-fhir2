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

import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.criterion.Restrictions.eq;

import javax.annotation.Nonnull;

import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientIdentifierSystemDao;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirPatientIdentifierSystemDaoImpl implements FhirPatientIdentifierSystemDao {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @__({ @Autowired, @Qualifier("sessionFactory") }))
	private SessionFactory sessionFactory;
	
	@Override
	@Transactional(readOnly = true)
	public String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
		return (String) sessionFactory.getCurrentSession().createCriteria(FhirPatientIdentifierSystem.class)
		        .add(eq("patientIdentifierType.patientIdentifierTypeId", patientIdentifierType.getId()))
		        .setProjection(Projections.property("url")).uniqueResult();
	}
	
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierTypeByUrl(String url) {
		return (PatientIdentifierType) sessionFactory.getCurrentSession().createCriteria(FhirPatientIdentifierSystem.class)
		        .add(eq("url", url)).setProjection(Projections.property("patientIdentifierType")).uniqueResult();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(
	        @Nonnull PatientIdentifierType patientIdentifierType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FhirPatientIdentifierSystem.class);
		criteria.add(eq("patientIdentifierType", patientIdentifierType));
		return Optional.ofNullable((FhirPatientIdentifierSystem) criteria.uniqueResult());
	}
	
	@Override
	@Transactional
	public FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem) {
		sessionFactory.getCurrentSession().saveOrUpdate(fhirPatientIdentifierSystem);
		return fhirPatientIdentifierSystem;
	}
}
