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
import static org.hibernate.criterion.Restrictions.ilike;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collection;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Allergy;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.impl.FhirAllergyIntoleranceServiceImpl;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirAllergyIntoleranceDaoImpl extends BaseDaoImpl implements FhirAllergyIntoleranceDao {
	
	private Log log = LogFactory.getLog(FhirAllergyIntoleranceServiceImpl.class);
	
	@Inject
	@Named("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Inject
	private FhirConceptService conceptService;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public Allergy getAllergyIntoleranceByUuid(String uuid) {
		return (Allergy) sessionFactory.getCurrentSession().createCriteria(Allergy.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	public Collection<Allergy> searchForAllergies(ReferenceParam patientReference, StringOrListParam category,
	        StringOrListParam severity, TokenOrListParam manifestationCode) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Allergy.class);
		handlePatientReference(criteria, patientReference, "patient");
		handleSeverity(criteria, severity).ifPresent(criteria::add);
		handleAllergenCategory(criteria, category);
		return criteria.list();
	}
	
	public Optional<Criterion> handleAllergenCategory(Criteria criteria, StringOrListParam categoryParam) {
		if (categoryParam == null) {
			return Optional.empty();
		}
		criteria.createAlias("allergen", "a");
		
		return handleOrListParam(categoryParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceCategory category = AllergyIntolerance.AllergyIntoleranceCategory
				        .fromCode(token.getValue());
				switch (category) {
					case FOOD:
						return Optional.of(ilike("a.allergenType", "FOOD", MatchMode.EXACT));
					case MEDICATION:
						return Optional.of(ilike("a.allergenType", "DRUG", MatchMode.EXACT));
					case ENVIRONMENT:
						return Optional.of(ilike("a.allergenType", "ENVIRONMENT", MatchMode.EXACT));
					case NULL:
						return Optional.of(ilike("a.allergenType", "OTHER", MatchMode.EXACT));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		});
		
	}
	
	private Optional<Criterion> handleSeverity(Criteria criteria, StringOrListParam severityParam) {
		if (severityParam == null) {
			return Optional.empty();
		}
		criteria.createAlias("severity", "c");
		
		return handleOrListParam(severityParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceSeverity severity = AllergyIntolerance.AllergyIntoleranceSeverity
				        .fromCode(token.getValue());
				switch (severity) {
					case MILD:
						return Optional.of(eq("c.conceptId",
						    getConceptId(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MILD))));
					case MODERATE:
						return Optional.of(eq("c.conceptId",
						    getConceptId(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MODERATE))));
					case SEVERE:
						return Optional.of(eq("c.conceptId",
						    getConceptId(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_SEVERE))));
					case NULL:
						return Optional.of(eq("c.conceptId",
						    getConceptId(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_OTHER))));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		});
		
	}
	
	private Integer getConceptId(String uuid) {
		Concept concept = conceptService.getConceptByUuid(uuid).orElse(null);
		return concept.getId();
	}
	
}
