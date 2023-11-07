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

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
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
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(criteriaBuilder, (ReferenceAndListParam) param.getParam(), "patient"));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleAllergenCategory("allergen.allergenType", (TokenAndListParam) param.getParam())
					            .ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.ALLERGEN_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleAllergen(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.SEVERITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleSeverity(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleManifestation(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleBoolean("voided", convertStringStatusToBoolean((TokenAndListParam) param.getParam()))
					            .ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
	}
	
	private void handleManifestation(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		if (code != null) {
			root.join("reactions").alias("r");
			root.join("r.reaction").alias("rc");
			
			handleCodeableConcept(criteriaBuilder, code, "rc", "rcm", "rcrt")
					.ifPresent(criteriaBuilder::and);
		}
	}
	
	private void handleAllergen(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		if (code != null) {
			root.join("allergen.codedAllergen").alias("ac");
			
			handleCodeableConcept(criteriaBuilder, code, "ac", "acm", "acrt")
					.ifPresent(criteriaBuilder::and);
		}
	}
	
	private void handleSeverity(CriteriaBuilder criteriaBuilder, TokenAndListParam severityParam) {
		if (severityParam == null) {
			return;
		}
		Map<String, String> severityConceptUuids = globalPropertyService.getGlobalProperties(
		    FhirConstants.GLOBAL_PROPERTY_MILD, FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
		
		root.join("severity").alias("sc");
		
		handleAndListParam(severityParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceSeverity severity = AllergyIntolerance.AllergyIntoleranceSeverity
				        .fromCode(token.getValue());
				switch (severity) {
					case MILD:
						return Optional.of(
								criteriaBuilder.equal(root.get("sc.uuid"),
										severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD)));
					case MODERATE:
						return Optional.of(
								criteriaBuilder.equal(root.get("sc.uuid"),
										severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE)));
					case SEVERE:
						return Optional.of(
								criteriaBuilder.equal(root.get("sc.uuid"),
										severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE)));
					case NULL:
						return Optional.of(criteriaBuilder.equal(root.get("sc.uuid"), severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_OTHER)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		}).ifPresent(criteriaBuilder::and);
	}
	
	private Optional<Predicate> handleAllergenCategory(String propertyName, TokenAndListParam categoryParam) {
		if (categoryParam == null) {
			return Optional.empty();
		}
		
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
