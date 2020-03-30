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

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationTranslatorImpl implements MedicationTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Medication toFhirResource(Drug drug) {
		if (drug == null) {
			return null;
		}
		
		Medication medication = new Medication();
		medication.setId(drug.getUuid());
		medication.setCode(conceptTranslator.toFhirResource(drug.getConcept()));
		medication.setForm(conceptTranslator.toFhirResource(drug.getDosageForm()));
		
		for (DrugIngredient val : drug.getIngredients()) {
			Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
			medication.addIngredient(ingredient.setItem(conceptTranslator.toFhirResource(val.getIngredient())));
		}
		
		medication.getMeta().setLastUpdated(drug.getDateChanged());
		
		if (drug.getRetired()) {
			medication.setStatus(Medication.MedicationStatus.INACTIVE);
		} else {
			medication.setStatus(Medication.MedicationStatus.ACTIVE);
		}
		
		addMedicineExtension(medication, "maximumDailyDose", drug.getMaximumDailyDose().toString());
		addMedicineExtension(medication, "minimumDailyDose", drug.getMinimumDailyDose().toString());
		addMedicineExtension(medication, "strength", drug.getStrength());
		
		return medication;
	}
	
	@Override
	public Drug toOpenmrsType(Drug existingDrug, Medication med) {
		if (med == null) {
			return existingDrug;
		}
		if (med.getId() != null) {
			existingDrug.setUuid(med.getId());
		}
		
		if (med.hasCode()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(med.getCode()));
		}
		
		if (med.hasForm()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(med.getForm()));
		}
		Collection<DrugIngredient> ingredients = new LinkedHashSet();
		if (med.hasIngredient()) {
			for (Medication.MedicationIngredientComponent ingredient : med.getIngredient()) {
				DrugIngredient omrsIngredient = new DrugIngredient();
				omrsIngredient.setDrug(existingDrug);
				omrsIngredient.setIngredient(conceptTranslator.toOpenmrsType(ingredient.getItemCodeableConcept()));
				ingredients.add(omrsIngredient);
			}
			existingDrug.setIngredients(ingredients);
		}
		
		if (med.getStatus() == Medication.MedicationStatus.ACTIVE) {
			existingDrug.setRetired(false);
		} else if (med.getStatus() == Medication.MedicationStatus.INACTIVE) {
			existingDrug.setRetired(true);
		}
		
		getOpenmrsMedicineExtension(med).ifPresent(ext -> ext.getExtension()
		        .forEach(e -> addMedicineComponent(existingDrug, e.getUrl(), ((StringType) e.getValue()).getValue())));
		
		return existingDrug;
	}
	
	public void addMedicineComponent(@NotNull Drug drug, @NotNull String url, @NotNull String value) {
		if (value == null || url == null || !url.startsWith(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#")) {
			return;
		}
		
		String val = url.substring(url.lastIndexOf('#') + 1);
		
		switch (val) {
			case "maximumDailyDose":
				drug.setMaximumDailyDose(Double.valueOf(value));
				break;
			case "minimumDailyDose":
				drug.setMinimumDailyDose(Double.valueOf(value));
				break;
			case "strength":
				drug.setStrength(value);
				break;
		}
	}
	
	private void addMedicineExtension(@NotNull Medication medication, @NotNull java.lang.String extensionProperty,
	        @NotNull String value) {
		if (value == null) {
			return;
		}
		
		getOpenmrsMedicineExtension(medication)
		        .orElseGet(() -> medication.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE))
		        .addExtension(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#" + extensionProperty, new StringType(value));
	}
	
	private Optional<Extension> getOpenmrsMedicineExtension(@NotNull Medication medication) {
		return Optional.ofNullable(medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE));
		
	}
}
