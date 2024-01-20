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

import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationDaoImpl extends BaseFhirDao<Drug> implements FhirMedicationDao {
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Drug,U> criteriaContext, SearchParameterMap theParams) {
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
					criteriaContext.finalizeQuery();
					break;
			}
		});
	}
	
	// TODO: look into DetachedCriteria and how to translate it to jpa criteria api
	private <U> void handleIngredientCode(OpenmrsFhirCriteriaContext<Drug,U> criteriaContext, TokenAndListParam ingredientCode) {
		if (ingredientCode != null) {
			Join<?,?> ingredientsJoin = criteriaContext.addJoin("ingredients", "i");
			From<?,?> conceptJoin = criteriaContext.addJoin(ingredientsJoin,"concept", "ic");
			
			handleCodeableConcept(criteriaContext, ingredientCode, conceptJoin, "icm", "icrt")
			        .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
			criteriaContext.getRoot().get("conceptId");
//			and(Subqueries.propertyIn("i.ingredient", detachedCriteria));
		}
	}
	
	private <U> void handleMedicationCode(OpenmrsFhirCriteriaContext<Drug,U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?,?> conceptJoin = criteriaContext.addJoin("concept", "c");
			handleCodeableConcept(criteriaContext, code, conceptJoin, "ccm", "ccrt").ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	private <U> void handleMedicationDosageForm(OpenmrsFhirCriteriaContext<Drug,U> criteriaContext, TokenAndListParam dosageForm) {
		if (dosageForm != null) {
			criteriaContext.getRoot().join("dosageForm").alias("dc");
			From<?,?> dosageFormJoin = criteriaContext.addJoin("dosageForm", "dc");
			handleCodeableConcept(criteriaContext, dosageForm, dosageFormJoin, "dcm", "dcrt").ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
}
