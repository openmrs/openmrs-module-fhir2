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
import java.util.Optional;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FhirObservationDaoImpl extends BaseDaoImpl implements FhirObservationDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Obs getObsByUuid(String uuid) {
		return (Obs) sessionFactory.getCurrentSession().createCriteria(Obs.class).add(eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public Collection<Obs> searchForObservations(ReferenceAndListParam encounterReference,
	        ReferenceAndListParam patientReference, ReferenceParam hasMemberReference, TokenAndListParam valueConcept,
	        DateRangeParam valueDateParam, QuantityAndListParam valueQuantityParam, StringAndListParam valueStringParam,
	        DateRangeParam date, TokenAndListParam code, SortSpec sort) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		
		handleEncounterReference("e", encounterReference).ifPresent(c -> criteria.createAlias("encounter", "e").add(c));
		handlePatientReference(criteria, patientReference, "person");
		handleHasMemberReference(criteria, hasMemberReference);
		handleValueCodedConcept(criteria, valueConcept);
		handleDateRange("valueDatetime", valueDateParam).ifPresent(criteria::add);
		
		handleValueStringParam("valueText", valueStringParam).ifPresent(criteria::add);
		handleQuantity("valueNumeric", valueQuantityParam).ifPresent(criteria::add);
		handleDateRange("obsDatetime", date).ifPresent(criteria::add);
		handleCodedConcept(criteria, code);
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	protected void handleHasMemberReference(Criteria criteria, ReferenceParam hasMemberReference) {
		if (hasMemberReference != null) {
			criteria.createAlias("groupMembers", "gm");
			
			switch (hasMemberReference.getChain()) {
				case Observation.SP_CODE:
					TokenAndListParam code = new TokenAndListParam()
					        .addAnd(new TokenParam().setValue(hasMemberReference.getValue()));
					criteria.createAlias("gm.concept", "c");
					handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
					break;
				case "":
					criteria.add(eq("gm.uuid", hasMemberReference.getIdPart()));
					break;
			}
		}
	}
	
	protected Optional<Criterion> handleValueStringParam(@NotNull String propertyName, StringAndListParam valueStringParam) {
		if (valueStringParam == null) {
			return Optional.empty();
		}
		return handleAndListParam(valueStringParam, v -> propertyLike(propertyName, v.getValue()));
	}
	
	@Override
	protected String paramToProp(String paramName) {
		if ("date".equals(paramName)) {
			return "obsDatetime";
		}
		
		return null;
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("concept", "c");
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private void handleValueCodedConcept(Criteria criteria, TokenAndListParam valueConcept) {
		if (valueConcept != null) {
			criteria.createAlias("valueCoded", "c");
			handleCodeableConcept(criteria, valueConcept, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
}
