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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

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
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Drug> criteriaQuery = criteriaBuilder.createQuery(Drug.class);
		Root<Drug> root = criteriaQuery.from(Drug.class);
		
		List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleMedicationCode(finalCriteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DOSAGE_FORM_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleMedicationDosageForm(finalCriteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.INGREDIENT_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleIngredientCode(finalCriteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicates::add);
					criteriaQuery.where(predicates.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	// TODO: look into DetachedCriteria and how to translate it to jpa criteria api
	private void handleIngredientCode(CriteriaBuilder criteriaBuilder, TokenAndListParam ingredientCode) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Drug> criteriaQuery = criteriaBuilder.createQuery(Drug.class);
		Root<Drug> root = criteriaQuery.from(Drug.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (ingredientCode != null) {
			root.join("ingredients").alias("i");
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Concept.class, "ic");
			handleCodeableConcept(criteriaBuilder, ingredientCode, "ic", "icm", "icrt").ifPresent(predicates::add);
			detachedCriteria.setProjection(Projections.property("conceptId"));
			and(Subqueries.propertyIn("i.ingredient", detachedCriteria));
		}
	}
	
	private void handleMedicationCode(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Drug> criteriaQuery = criteriaBuilder.createQuery(Drug.class);
		Root<Drug> root = criteriaQuery.from(Drug.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (code != null) {
			root.join("concept").alias("cc");
			handleCodeableConcept(criteriaBuilder, code, "cc", "ccm", "ccrt").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleMedicationDosageForm(CriteriaBuilder criteriaBuilder, TokenAndListParam dosageForm) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Drug> criteriaQuery = criteriaBuilder.createQuery(Drug.class);
		Root<Drug> root = criteriaQuery.from(Drug.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (dosageForm != null) {
			root.join("dosageForm").alias("dc");
			handleCodeableConcept(criteriaBuilder, dosageForm, "dc", "dcm", "dcrt").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
}
