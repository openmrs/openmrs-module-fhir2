/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationTranslatorImpl implements MedicationTranslator {
	
	@Inject
	private FhirConceptService conceptService;
	
	@Inject
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Medication toFhirResource(Drug drug) {
		if (drug == null) {
			return null;
		}
		
		Medication medication = new Medication();
		medication.setId(drug.getUuid());
		medication.setCode(conceptTranslator.toFhirResource(drug.getConcept()));
		medication.setForm(new CodeableConcept().addCoding(new Coding(FhirConstants.MEDICATION_FORM_VALUE_SET_URI,
		        drug.getDosageForm().getUuid(), drug.getDosageForm().getDisplayString())));
		
		Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
		for (DrugIngredient val : drug.getIngredients()) {
			ingredient.setItem(conceptTranslator.toFhirResource(val.getIngredient()));
		}
		medication.addIngredient(ingredient);
		
		return medication;
	}
	
	@Override
	public Drug toOpenmrsType(Drug existingDrug, Medication med) {
		if (med == null) {
			return null;
		}
		existingDrug.setUuid(med.getId());
		
		if (med.hasCode()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(med.getCode()));
		}
		
		if (med.hasForm()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(med.getForm()));
		}
		Collection<DrugIngredient> ingredients = new ArrayList<>();
		if (med.hasIngredient()) {
			for (Medication.MedicationIngredientComponent ingredient : med.getIngredient()) {
				DrugIngredient omrsIngredient = new DrugIngredient();
				omrsIngredient.setDrug(existingDrug);
				for (Coding code : ingredient.getItemCodeableConcept().getCoding()) {
					omrsIngredient.setIngredient(conceptService.getConceptByUuid(code.getCode()).orElse(null));
				}
				ingredients.add(omrsIngredient);
			}
			existingDrug.setIngredients(ingredients);
		}
		
		return existingDrug;
	}
}
