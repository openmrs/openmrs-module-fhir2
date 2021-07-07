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

import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.or;

import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestDaoImpl extends BaseFhirDao<TestOrder> implements FhirServiceRequestDao<TestOrder> {

	@Autowired
	@Qualifier("fhirR4")
	private FhirContext fhirContext;
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleEncounterReference("e", (ReferenceAndListParam) param.getParam())
					        .ifPresent(c -> criteria.createAlias("encounter", "e").add(c)));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteria,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.HAS_PROPERTY:
					entry.getValue().forEach(hasParameter -> handleHasAndParam(criteria, (HasAndListParam) hasParameter.getParam()));
					break;	
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteria, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(criteria,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange((DateRangeParam) dateRangeParam.getParam())
					        .ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private Optional<Criterion> handleDateRange(DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(Stream.of(
		    Optional.of(or(toCriteriaArray(Stream.of(handleDate("scheduledDate", dateRangeParam.getLowerBound()),
		        handleDate("dateActivated", dateRangeParam.getLowerBound()))))),
		    Optional.of(or(toCriteriaArray(Stream.of(handleDate("dateStopped", dateRangeParam.getUpperBound()),
		        handleDate("autoExpireDate", dateRangeParam.getUpperBound())))))))));
	}

	private void handleHasAndParam(Criteria criteria, HasAndListParam hasAndListParam) {
		if (hasAndListParam == null || hasAndListParam.size() == 0) {
			return;
		}

		DetachedCriteria obsSubquery = DetachedCriteria.forClass(Obs.class, "obs");
		handleAndListParam(hasAndListParam, hasParam -> {
			if (!FhirConstants.OBSERVATION.equals(hasParam.getTargetResourceType())) {
				return Optional.empty();
			}

			if (hasParam.getParameterName() != null) {
				switch (hasParam.getParameterName()) {
					case Observation.SP_ENCOUNTER:
						obsSubquery.createCriteria("encounter")
						        .add(Restrictions.eq("uuid", hasParam.getParameterValue().toString()));
						break;
					case Observation.SP_PATIENT:
						obsSubquery.createCriteria("person")
						        .add(Restrictions.eq("uuid", hasParam.getParameterValue().toString()));
						break;
					case Observation.SP_CATEGORY:
						obsSubquery.createCriteria("concept").createCriteria("conceptClass")
						        .add(Restrictions.eq("name", hasParam.getValueAsQueryToken(fhirContext).toString()));
						break;
					case Observation.SP_CODE:
						obsSubquery.createCriteria("concept")
						        .add(Restrictions.eq("id", Integer.parseInt(hasParam.getParameterValue().toString())));
						break;
				}
			} else {
				return Optional.empty();
			}
			return Optional.empty();
		}).ifPresent(obsSubquery::add);

		obsSubquery.setProjection(property("obs.order"));
		criteria.add(Subqueries.propertyIn("id", obsSubquery));
	}


	
}
