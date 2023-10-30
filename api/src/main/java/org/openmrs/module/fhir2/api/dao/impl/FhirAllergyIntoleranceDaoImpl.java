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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
public class FhirAllergyIntoleranceDaoImpl extends BaseFhirDao<Allergy> implements FhirAllergyIntoleranceDao {
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	private List<Predicate> predicates = new ArrayList<>();
	
	@Override
	public Allergy createOrUpdate(@Nonnull Allergy allergy) {
		Allergy savedAllergy = super.createOrUpdate(allergy);
		
		for (AllergyReaction reaction : allergy.getReactions()) {
			getSessionFactory().getCurrentSession().saveOrUpdate(reaction);
		}
		
		return savedAllergy;
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			EntityManager em = sessionFactory.getCurrentSession();
			CriteriaQuery<Allergy> criteriaQuery = em.getCriteriaBuilder().createQuery(Allergy.class);
			Root<Allergy> root = criteriaQuery.from(Allergy.class);
			
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePatientReference(criteriaBuilder,
					    (ReferenceAndListParam) param.getParam(), "patient"));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleAllergenCategory("allergen.allergenType", (TokenAndListParam) param.getParam())
					            .ifPresent(predicates::add));
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
				case FhirConstants.ALLERGEN_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleAllergen(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.SEVERITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleSeverity(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleManifestation(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleBoolean("voided", convertStringStatusToBoolean((TokenAndListParam) param.getParam()))
					            .ifPresent(predicates::add));
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicates::add);
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	private void handleManifestation(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Allergy> criteriaQuery = criteriaBuilder.createQuery(Allergy.class);
		Root<Allergy> root = criteriaQuery.from(Allergy.class);
		
		if (code != null) {
			root.join("reactions").alias("r");
			root.join("r.reaction").alias("rc");
			
			handleCodeableConcept(criteriaBuilder, code, "rc", "rcm", "rcrt").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleAllergen(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaQuery<Allergy> criteriaQuery = em.getCriteriaBuilder().createQuery(Allergy.class);
		Root<Allergy> root = criteriaQuery.from(Allergy.class);
		
		if (code != null) {
			root.join("allergen.codedAllergen").alias("ac");
			
			handleCodeableConcept(criteriaBuilder, code, "ac", "acm", "acrt").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleSeverity(CriteriaBuilder criteriaBuilder, TokenAndListParam severityParam) {
		if (severityParam == null) {
			return;
		}
		Map<String, String> severityConceptUuids = globalPropertyService.getGlobalProperties(
		    FhirConstants.GLOBAL_PROPERTY_MILD, FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
		
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Allergy> criteriaQuery = criteriaBuilder.createQuery(Allergy.class);
		Root<Allergy> root = criteriaQuery.from(Allergy.class);
		
		root.join("severity").alias("sc");
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(severityParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceSeverity severity = AllergyIntolerance.AllergyIntoleranceSeverity
				        .fromCode(token.getValue());
				switch (severity) {
					case MILD:
						return Optional.of(finalCriteriaBuilder.equal(root.get("sc.uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD)));
					case MODERATE:
						return Optional.of(finalCriteriaBuilder.equal(root.get("sc.uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE)));
					case SEVERE:
						return Optional.of(finalCriteriaBuilder.equal(root.get("sc.uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE)));
					case NULL:
						return Optional.of(finalCriteriaBuilder.equal(root.get("sc.uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_OTHER)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		}).ifPresent(predicates::add);
		criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
	}
	
	private Optional<Predicate> handleAllergenCategory(String propertyName, TokenAndListParam categoryParam) {
		if (categoryParam == null) {
			return Optional.empty();
		}
		
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Allergy> criteriaQuery = criteriaBuilder.createQuery(Allergy.class);
		Root<Allergy> root = criteriaQuery.from(Allergy.class);
		
		return handleAndListParam(categoryParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceCategory category = AllergyIntolerance.AllergyIntoleranceCategory
				        .fromCode(token.getValue());
				switch (category) {
					case FOOD:
						return Optional.of(criteriaBuilder.equal(root.get(propertyName), AllergenType.FOOD));
					case MEDICATION:
						return Optional.of(criteriaBuilder.equal(root.get(propertyName), AllergenType.DRUG));
					case ENVIRONMENT:
						return Optional.of(criteriaBuilder.equal(root.get(propertyName), AllergenType.ENVIRONMENT));
					case NULL:
						return Optional.of(criteriaBuilder.equal(root.get(propertyName), AllergenType.OTHER));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		});
		
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		if (AllergyIntolerance.SP_SEVERITY.equals(param)) {
			return "severity";
		}
		
		return super.paramToProp(param);
	}
}
