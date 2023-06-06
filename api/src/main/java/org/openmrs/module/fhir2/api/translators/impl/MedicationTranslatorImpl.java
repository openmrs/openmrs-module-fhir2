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

import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;

import javax.annotation.Nonnull;

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
	
	public static final String DRUG_NAME_EXTENSION = "drugName";
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Medication toFhirResource(@Nonnull Drug drug) {
		if (drug == null) {
			return null;
		}
		
		Medication medication = new Medication();
		medication.setId(drug.getUuid());
		medication.setCode(conceptTranslator.toFhirResource(drug.getConcept()));
		medication.setForm(conceptTranslator.toFhirResource(drug.getDosageForm()));
		
		if (drug.getIngredients() != null) {
			for (DrugIngredient val : drug.getIngredients()) {
				Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
				medication.addIngredient(ingredient.setItem(conceptTranslator.toFhirResource(val.getIngredient())));
			}
		}
		
		medication.getMeta().setLastUpdated(drug.getDateChanged());
		medication.setStatus(Medication.MedicationStatus.ACTIVE);
		
		addMedicineExtension(medication, DRUG_NAME_EXTENSION, drug.getName());
		
		if (drug.getMaximumDailyDose() != null) {
			addMedicineExtension(medication, "maximumDailyDose", drug.getMaximumDailyDose().toString());
		}
		
		if (drug.getMinimumDailyDose() != null) {
			addMedicineExtension(medication, "minimumDailyDose", drug.getMinimumDailyDose().toString());
		}
		
		if (drug.getStrength() != null) {
			addMedicineExtension(medication, "strength", drug.getStrength());
		}
		
		medication.getMeta().setLastUpdated(getLastUpdated(drug));
		
		return medication;
	}
	
	@Override
	public Drug toOpenmrsType(@Nonnull Medication medication) {
		if (medication == null) {
			return null;
		}
		
		return toOpenmrsType(new Drug(), medication);
	}
	
	@Override
	public Drug toOpenmrsType(@Nonnull Drug existingDrug, @Nonnull Medication medication) {
		if (existingDrug == null || medication == null) {
			return null;
		}
		
		if (medication.hasId()) {
			existingDrug.setUuid(medication.getIdElement().getIdPart());
		}
		
		if (medication.hasCode()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(medication.getCode()));
		}
		
		if (medication.hasForm()) {
			existingDrug.setConcept(conceptTranslator.toOpenmrsType(medication.getForm()));
		}
		
		Collection<DrugIngredient> ingredients = new LinkedHashSet<>();
		
		if (medication.hasIngredient()) {
			for (Medication.MedicationIngredientComponent ingredient : medication.getIngredient()) {
				DrugIngredient omrsIngredient = new DrugIngredient();
				omrsIngredient.setDrug(existingDrug);
				omrsIngredient.setIngredient(conceptTranslator.toOpenmrsType(ingredient.getItemCodeableConcept()));
				ingredients.add(omrsIngredient);
			}
			existingDrug.setIngredients(ingredients);
		}
		
		getOpenmrsMedicineExtension(medication).ifPresent(ext -> ext.getExtension()
		        .forEach(e -> addMedicineComponent(existingDrug, e.getUrl(), ((StringType) e.getValue()).getValue())));
		
		return existingDrug;
	}
	
	public void addMedicineComponent(@Nonnull Drug drug, @Nonnull String url, @Nonnull String value) {
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
			case DRUG_NAME_EXTENSION:
				drug.setName(value);
				break;
		}
	}
	
	private void addMedicineExtension(@Nonnull Medication medication, @Nonnull java.lang.String extensionProperty,
	        @Nonnull String value) {
		if (value == null) {
			return;
		}
		
		getOpenmrsMedicineExtension(medication)
		        .orElseGet(() -> medication.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE))
		        .addExtension(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#" + extensionProperty, new StringType(value));
	}
	
	private Optional<Extension> getOpenmrsMedicineExtension(@Nonnull Medication medication) {
		return Optional.ofNullable(medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE));
		
	}
	
}
