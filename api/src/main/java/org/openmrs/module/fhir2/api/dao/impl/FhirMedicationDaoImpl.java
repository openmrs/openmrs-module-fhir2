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

import java.util.Collection;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationDaoImpl extends BaseFhirDaoImpl<Drug> implements FhirMedicationDao {
	
	@Override
	public Drug createOrUpdate(Drug drug) {
		super.createOrUpdate(drug);
		
		for (DrugIngredient ingredient : drug.getIngredients()) {
			getSessionFactory().getCurrentSession().saveOrUpdate(ingredient);
		}
		
		return drug;
	}
	
	@Override
	public Collection<Drug> searchForMedications(TokenAndListParam code, TokenAndListParam dosageForm,
	        TokenOrListParam ingredientCode, TokenOrListParam status) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(Drug.class);
		handleMedicationCode(criteria, code);
		handleMedicationDosageForm(criteria, dosageForm);
		handleBoolean("retired", convertStringStatusToBoolean(status)).ifPresent(criteria::add);
		
		return criteria.list();
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
