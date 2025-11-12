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

import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaSubquery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirMedicationDaoImpl extends BaseFhirDao<Drug> implements FhirMedicationDao {
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Drug, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleMedicationCode(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DOSAGE_FORM_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleMedicationDosageForm(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.INGREDIENT_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleIngredientCode(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	private <U> void handleIngredientCode(OpenmrsFhirCriteriaContext<Drug, U> criteriaContext,
	        TokenAndListParam ingredientCode) {
		if (ingredientCode != null) {
			Join<?, ?> ingredientsJoin = criteriaContext.addJoin("ingredients", "i");
			Join<?, ?> conceptJoin = criteriaContext.addJoin(ingredientsJoin, "ingredient", "ic");
			
			OpenmrsFhirCriteriaSubquery<Concept, Integer> conceptSubquery = criteriaContext.addSubquery(Concept.class,
			    Integer.class);
			getSearchQueryHelper().handleCodeableConcept(criteriaContext, ingredientCode, conceptJoin, "icm", "icrt")
			        .ifPresent(conceptSubquery::addPredicate);
			conceptSubquery.setProjection(conceptSubquery.getRoot().get("conceptId"));
			
			criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().in(conceptJoin.get("conceptId"))
			        .value(conceptSubquery.finalizeQuery()));
		}
	}
	
	private <U> void handleMedicationCode(OpenmrsFhirCriteriaContext<Drug, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?, ?> conceptJoin = criteriaContext.addJoin("concept", "c");
			getSearchQueryHelper().handleCodeableConcept(criteriaContext, code, conceptJoin, "ccm", "ccrt")
			        .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	private <U> void handleMedicationDosageForm(OpenmrsFhirCriteriaContext<Drug, U> criteriaContext,
	        TokenAndListParam dosageForm) {
		if (dosageForm != null) {
			From<?, ?> dosageFormJoin = criteriaContext.addJoin("dosageForm", "dc");
			getSearchQueryHelper().handleCodeableConcept(criteriaContext, dosageForm, dosageFormJoin, "dcm", "dcrt")
			        .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
}
