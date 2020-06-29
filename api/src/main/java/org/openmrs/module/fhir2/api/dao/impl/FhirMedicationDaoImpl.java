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

import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationDaoImpl extends BaseFhirDao<Drug> implements FhirMedicationDao {
	
	@Override
	public Drug createOrUpdate(Drug drug) {
		super.createOrUpdate(drug);
		
		for (DrugIngredient ingredient : drug.getIngredients()) {
			getSessionFactory().getCurrentSession().saveOrUpdate(ingredient);
		}
		
		return drug;
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleMedicationCode(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DOSAGE_FORM_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleMedicationDosageForm(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.INGREDIENT_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleIngredientCode(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleBoolean("retired", convertStringStatusToBoolean((TokenAndListParam) param.getParam()))
					            .ifPresent(criteria::add));
					break;
			}
		});
	}
	
	private void handleIngredientCode(Criteria criteria, TokenAndListParam ingredientCode) {
		if (ingredientCode != null) {
			criteria.createAlias("ingredients", "i");
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Concept.class, "ic");
			handleCodeableConcept(criteria, ingredientCode, "ic", "icm", "icrt").ifPresent(detachedCriteria::add);
			detachedCriteria.setProjection(Projections.property("conceptId"));
			criteria.add(Subqueries.propertyIn("i.ingredient", detachedCriteria));
		}
	}
	
	private void handleMedicationCode(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("concept", "cc");
			handleCodeableConcept(criteria, code, "cc", "ccm", "ccrt").ifPresent(criteria::add);
		}
	}
	
	private void handleMedicationDosageForm(Criteria criteria, TokenAndListParam dosageForm) {
		if (dosageForm != null) {
			criteria.createAlias("dosageForm", "dc");
			handleCodeableConcept(criteria, dosageForm, "dc", "dcm", "dcrt").ifPresent(criteria::add);
		}
	}
}
