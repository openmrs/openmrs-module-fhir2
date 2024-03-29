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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.translators.impl.MedicationTranslatorImpl.DRUG_NAME_EXTENSION;

import java.util.Collections;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationTranslatorImplTest {
	
	private static final String MEDICATION_UUID = "aa47108b-9720-45d4-8be3-ba75e4cea8ac";
	
	private static final String DOSAGE_FORM_CONCEPT_UUID = "162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String DRUG_CONCEPT_UUID = "172553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String INGREDIENT_CONCEPT_UUID = "182553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final Double MAX_DAILY_DOSE = 3.5;
	
	private static final Double MIN_DAILY_DOSE = 2.0;
	
	private static final String DOSE_STRENGTH = "500mg";
	
	private static final String DRUG_NAME = "Example Drug Name";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private MedicationTranslatorImpl medicationTranslator;
	
	private Drug drug;
	
	@Before
	public void setup() {
		drug = new Drug();
		medicationTranslator = new MedicationTranslatorImpl();
		medicationTranslator.setConceptTranslator(conceptTranslator);
		
		Concept drugConcept = new Concept();
		drugConcept.setUuid(DRUG_CONCEPT_UUID);
		drug.setName(DRUG_NAME);
		drug.setConcept(drugConcept);
		drug.setMaximumDailyDose(MAX_DAILY_DOSE);
		drug.setMinimumDailyDose(MIN_DAILY_DOSE);
		drug.setStrength(DOSE_STRENGTH);
		
		Concept dosageConcept = new Concept();
		dosageConcept.setUuid(DOSAGE_FORM_CONCEPT_UUID);
		drug.setDosageForm(dosageConcept);
	}
	
	@Test
	public void toFhirResource_shouldTranslateUuidToId() {
		drug.setUuid(MEDICATION_UUID);
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDosageConceptToMedicationForm() {
		CodeableConcept code = new CodeableConcept().addCoding(new Coding("", DOSAGE_FORM_CONCEPT_UUID, ""));
		
		Concept dosageConcept = new Concept();
		dosageConcept.setUuid(DOSAGE_FORM_CONCEPT_UUID);
		
		when(conceptTranslator.toFhirResource(dosageConcept)).thenReturn(code);
		
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getForm().getCoding().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getForm().getCoding().get(0).getCode(), equalTo(DOSAGE_FORM_CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugIngredientToMedicationIngredient() {
		DrugIngredient ingredient = new DrugIngredient();
		Concept concept = new Concept();
		concept.setUuid(INGREDIENT_CONCEPT_UUID);
		ingredient.setIngredient(concept);
		drug.setIngredients(Collections.singleton((ingredient)));
		
		CodeableConcept codeableConcept = new CodeableConcept().addCoding(new Coding("", INGREDIENT_CONCEPT_UUID, ""));
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getIngredient().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getIngredient().get(0).getItemCodeableConcept().getCoding().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getIngredient().get(0).getItemCodeableConcept().getCoding().get(0).getCode(),
		    equalTo(INGREDIENT_CONCEPT_UUID));
		assertThat(medication.getIngredient().get(0).getItemCodeableConcept().getText(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		drug.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToVersionId() {
		drug.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getMeta().getVersionId(), notNullValue());
	}
	
	@Test
	public void toFhirResource_shouldSetFhirMedicationToActiveIfDrugIsNotRetired() {
		drug.setRetired(false);
		org.hl7.fhir.r4.model.Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getStatus(), equalTo(Medication.MedicationStatus.ACTIVE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIdToUuid() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getUuid(), notNullValue());
		assertThat(drug.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithNullCode() {
		Medication medication = new Medication();
		drug.setConcept(null);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug.getConcept(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationCodeToConcept() {
		Medication medication = new Medication();
		medication.setCode(new CodeableConcept().addCoding(new Coding(null, DRUG_CONCEPT_UUID, "")));
		
		Concept drugConcept = new Concept();
		drugConcept.setUuid(DRUG_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(medication.getCode())).thenReturn(drugConcept);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getConcept(), notNullValue());
		assertThat(drug.getConcept().getUuid(), equalTo(DRUG_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithNullDose() {
		Medication medication = new Medication();
		drug.setDosageForm(null);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug.getDosageForm(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFormToDrugDosageForm() {
		Medication medication = new Medication();
		medication.setForm(new CodeableConcept().addCoding(new Coding(null, DOSAGE_FORM_CONCEPT_UUID, "")));
		
		Concept dosageConcept = new Concept();
		dosageConcept.setUuid(DOSAGE_FORM_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(medication.getForm())).thenReturn(dosageConcept);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getDosageForm(), notNullValue());
		assertThat(drug.getDosageForm().getUuid(), equalTo(DOSAGE_FORM_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationIngredientsToDrugIngredients() {
		CodeableConcept code = new CodeableConcept().addCoding(new Coding("", INGREDIENT_CONCEPT_UUID, ""));
		
		Medication medication = new Medication();
		Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
		medication.addIngredient(ingredient.setItem(code));
		
		Concept ingredientConcept = new Concept();
		ingredientConcept.setUuid(INGREDIENT_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(code)).thenReturn(ingredientConcept);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getIngredients().size(), greaterThanOrEqualTo(1));
		assertThat(drug.getIngredients().iterator().next().getIngredient().getUuid(), equalTo(INGREDIENT_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldNotRetireDrugIfMedicationInActive() {
		Medication medication = new Medication();
		medication.setStatus(Medication.MedicationStatus.ACTIVE);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getRetired(), equalTo(false));
	}
	
	@Test
	public void addMedicineExtension_shouldAddExtensionForMaximumDailyDose() {
		assertThat(
		    medicationTranslator.toFhirResource(drug).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#maximumDailyDose"),
		    hasProperty("value", hasProperty("value", equalTo("3.5"))));
	}
	
	@Test
	public void addMedicineExtension_shouldAddExtensionForMinimumDailyDose() {
		assertThat(
		    medicationTranslator.toFhirResource(drug).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#minimumDailyDose"),
		    hasProperty("value", hasProperty("value", equalTo("2.0"))));
	}
	
	@Test
	public void addMedicineExtension_shouldAddExtensionForStrength() {
		assertThat(
		    medicationTranslator.toFhirResource(drug).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength"),
		    hasProperty("value", hasProperty("value", equalTo(DOSE_STRENGTH))));
	}
	
	@Test
	public void addMedicineExtension_shouldAddExtensionForName() {
		assertThat(
		    medicationTranslator.toFhirResource(drug).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#" + DRUG_NAME_EXTENSION),
		    hasProperty("value", hasProperty("value", equalTo(DRUG_NAME))));
	}
	
	@Test
	public void addMedicineComponent_shouldSetMaximumDailyDoseCorrectly() {
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#maximumDailyDose",
		    "3.5");
		assertThat(drug.getMaximumDailyDose(), notNullValue());
		assertThat(drug.getMaximumDailyDose(), equalTo(MAX_DAILY_DOSE));
	}
	
	@Test
	public void addMedicineComponent_shouldSetMinimumDailyDoseCorrectly() {
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#minimumDailyDose",
		    "2.0");
		assertThat(drug.getMinimumDailyDose(), notNullValue());
		assertThat(drug.getMinimumDailyDose(), equalTo(MIN_DAILY_DOSE));
	}
	
	@Test
	public void addMedicineComponent_shouldSetStrengthCorrectly() {
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength",
		    DOSE_STRENGTH);
		assertThat(drug.getStrength(), notNullValue());
		assertThat(drug.getStrength(), equalTo(DOSE_STRENGTH));
	}
	
	@Test
	public void addMedicineComponent_shouldSetDrugNameCorrectly() {
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#" + DRUG_NAME_EXTENSION,
		    DRUG_NAME);
		assertThat(drug.getName(), notNullValue());
		assertThat(drug.getName(), equalTo(DRUG_NAME));
	}
	
	@Test
	public void addMedicineComponent_shouldReturnNullIfUrlIsNull() {
		drug.setStrength(null);
		drug.setMinimumDailyDose(null);
		drug.setMaximumDailyDose(null);
		
		medicationTranslator.addMedicineComponent(drug, null, DOSE_STRENGTH);
		assertThat(drug.getStrength(), nullValue());
		assertThat(drug.getMinimumDailyDose(), nullValue());
		assertThat(drug.getMaximumDailyDose(), nullValue());
	}
	
	@Test
	public void addMedicineComponent_shouldReturnNullIfValueIsNull() {
		drug.setStrength(null);
		drug.setMinimumDailyDose(null);
		drug.setMaximumDailyDose(null);
		
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength", null);
		assertThat(drug.getStrength(), nullValue());
		assertThat(drug.getMinimumDailyDose(), nullValue());
		assertThat(drug.getMaximumDailyDose(), nullValue());
	}
	
	@Test
	public void addMedicineComponent_shouldReturnNullIUrlDontStartWithNumberSign() {
		drug.setStrength(null);
		drug.setMinimumDailyDose(null);
		drug.setMaximumDailyDose(null);
		
		medicationTranslator.addMedicineComponent(drug, FhirConstants.OPENMRS_FHIR_EXT_MEDICINE, DOSE_STRENGTH);
		assertThat(drug.getStrength(), nullValue());
		assertThat(drug.getMinimumDailyDose(), nullValue());
		assertThat(drug.getMaximumDailyDose(), nullValue());
	}
	
}
