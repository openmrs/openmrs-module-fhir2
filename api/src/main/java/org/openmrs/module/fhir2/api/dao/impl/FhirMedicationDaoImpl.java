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

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationDaoImpl extends BaseFhirDao<Drug> implements FhirMedicationDao {
	
	@Override
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<Drug> criteriaContext, SearchParameterMap theParams) {
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
	private void handleIngredientCode(OpenmrsFhirCriteriaContext<Drug> criteriaContext, TokenAndListParam ingredientCode) {
		if (ingredientCode != null) {
			criteriaContext.getRoot().join("ingredients").alias("i");
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Concept.class, "ic");
			handleCodeableConcept(criteriaContext, ingredientCode, "ic", "icm", "icrt")
			        .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
			detachedCriteria.setProjection(Projections.property("conceptId"));
			and(Subqueries.propertyIn("i.ingredient", detachedCriteria));
		}
	}
	
	private void handleMedicationCode(OpenmrsFhirCriteriaContext<Drug> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			criteriaContext.getRoot().join("concept").alias("cc");
			handleCodeableConcept(criteriaContext, code, "cc", "ccm", "ccrt").ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	private void handleMedicationDosageForm(OpenmrsFhirCriteriaContext<Drug> criteriaContext, TokenAndListParam dosageForm) {
		if (dosageForm != null) {
			criteriaContext.getRoot().join("dosageForm").alias("dc");
			handleCodeableConcept(criteriaContext, dosageForm, "dc", "dcm", "dcrt").ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
}
