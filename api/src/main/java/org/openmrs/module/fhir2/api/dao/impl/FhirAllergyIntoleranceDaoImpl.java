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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirAllergyIntoleranceDaoImpl extends BaseFhirDao<Allergy> implements FhirAllergyIntoleranceDao {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
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
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Allergy, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "patient"));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER: {
					From<?, ?> allergyJoin = criteriaContext.addJoin("allergen", "allergen");
					entry.getValue().forEach(param -> handleAllergenCategory(criteriaContext, allergyJoin, "allergenType",
					    (TokenAndListParam) param.getParam()).ifPresent(criteriaContext::addPredicate));
					break;
				}
				case FhirConstants.ALLERGEN_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleAllergen(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.SEVERITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleSeverity(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleManifestation(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleBoolean(criteriaContext, "voided",
					            convertStringStatusToBoolean((TokenAndListParam) param.getParam()))
					                    .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	private <U> void handleManifestation(OpenmrsFhirCriteriaContext<Allergy, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			Join<?, ?> reactionsJoin = criteriaContext.addJoin("reactions", "r");
			Join<?, ?> reactionJoin = criteriaContext.addJoin(reactionsJoin, "reaction", "rc");
			
			handleCodeableConcept(criteriaContext, code, reactionJoin, "rcm", "rcrt")
			        .ifPresent(criteriaContext::addPredicate);
		}
	}
	
	private <U> void handleAllergen(OpenmrsFhirCriteriaContext<Allergy, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			Join<?, ?> allergenJoin = criteriaContext.addJoin("allergen", "allergen");
			Join<?, ?> codedAllergenJoin = criteriaContext.addJoin(allergenJoin, "codedAllergen", "ac");
			
			handleCodeableConcept(criteriaContext, code, codedAllergenJoin, "acm", "acrt")
			        .ifPresent(criteriaContext::addPredicate);
		}
	}
	
	private <U> void handleSeverity(OpenmrsFhirCriteriaContext<Allergy, U> criteriaContext,
	        TokenAndListParam severityParam) {
		if (severityParam == null) {
			return;
		}
		Map<String, String> severityConceptUuids = globalPropertyService.getGlobalProperties(
		    FhirConstants.GLOBAL_PROPERTY_MILD, FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
		
		Join<?, ?> severeJoin = criteriaContext.addJoin("severity", "sc");
		
		handleAndListParam(criteriaContext.getCriteriaBuilder(), severityParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceSeverity severity = AllergyIntolerance.AllergyIntoleranceSeverity
				        .fromCode(token.getValue());
				switch (severity) {
					case MILD:
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(severeJoin.get("uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD)));
					case MODERATE:
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(severeJoin.get("uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE)));
					case SEVERE:
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(severeJoin.get("uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE)));
					case NULL:
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(severeJoin.get("uuid"),
						    severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_OTHER)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		}).ifPresent(criteriaContext::addPredicate);
	}
	
	private <T, U> Optional<Predicate> handleAllergenCategory(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        From<?, ?> allergyJoin, String propertyName, TokenAndListParam categoryParam) {
		if (categoryParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), categoryParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceCategory category = AllergyIntolerance.AllergyIntoleranceCategory
				        .fromCode(token.getValue());
				switch (category) {
					case FOOD:
						return Optional.of(
						    criteriaContext.getCriteriaBuilder().equal(allergyJoin.get(propertyName), AllergenType.FOOD));
					case MEDICATION:
						return Optional.of(
						    criteriaContext.getCriteriaBuilder().equal(allergyJoin.get(propertyName), AllergenType.DRUG));
					case ENVIRONMENT:
						return Optional.of(criteriaContext.getCriteriaBuilder().equal(allergyJoin.get(propertyName),
						    AllergenType.ENVIRONMENT));
					case NULL:
						return Optional.of(
						    criteriaContext.getCriteriaBuilder().equal(allergyJoin.get(propertyName), AllergenType.OTHER));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		});
		
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		if (AllergyIntolerance.SP_SEVERITY.equals(param)) {
			return criteriaContext.getRoot().get("severity");
		}
		
		return super.paramToProp(criteriaContext, param);
	}
}
