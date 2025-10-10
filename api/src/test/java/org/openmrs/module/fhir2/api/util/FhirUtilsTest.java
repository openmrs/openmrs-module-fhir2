/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;

public class FhirUtilsTest {
	
	@Test
	public void getOpenmrsConditionType_shouldIdentifyDiagnosis() {
		Condition condition = new Condition();
		CodeableConcept category = new CodeableConcept();
		category.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS, null));
		condition.addCategory(category);
		
		Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.DIAGNOSIS));
	}
	
	@Test
	public void getOpenmrsConditionType_shouldDefaultToCondition() {
		Condition condition = new Condition();
		
		Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.CONDITION));
	}
	
	@Test
	public void getOpenmrsConditionType_shouldIdentifyCondition() {
		Condition condition = new Condition();
		CodeableConcept category = new CodeableConcept();
		category.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION, null));
		condition.addCategory(category);
		
		Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.CONDITION));
	}
	
	@Test
	public void getOpenmrsConditionType_shouldInspectAllCodings() {
		Condition condition = new Condition();
		CodeableConcept category = new CodeableConcept();
		category.addCoding(new Coding("http://example.com/system", "other", null));
		category.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS, null));
		condition.addCategory(category);
		
		Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.DIAGNOSIS));
	}
	
	@Test
	public void getOpenmrsConditionType_shouldReturnEmptyWhenNoHl7CodingPresent() {
		Condition condition = new Condition();
		CodeableConcept category = new CodeableConcept();
		category.addCoding(new Coding("http://example.com/system", "other", null));
		condition.addCategory(category);
		
		Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);
		
		assertThat(result.isPresent(), equalTo(false));
	}
	
	@Test
	public void referenceToType_shouldExtractType() {
		Optional<String> result = FhirUtils.referenceToType("Patient/123");
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo("Patient"));
	}
	
	@Test
	public void referenceToId_shouldExtractId() {
		Optional<String> result = FhirUtils.referenceToId("http://example.com/Condition/abc/_history/1");
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo("abc"));
	}
	
	@Test
	public void getReferenceType_shouldPreferTypeField() {
		org.hl7.fhir.r4.model.Reference reference = new org.hl7.fhir.r4.model.Reference();
		reference.setType("Observation");
		Optional<String> result = FhirUtils.getReferenceType(reference);
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo("Observation"));
	}
	
	@Test
	public void getOpenmrsEncounterType_shouldReturnEncounterWhenEncounterCodingPresent() {
		Encounter encounter = new Encounter();
		CodeableConcept type = new CodeableConcept();
		type.addCoding(new Coding(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI, "uuid", null));
		encounter.addType(type);
		
		Optional<FhirUtils.OpenmrsEncounterType> result = FhirUtils.getOpenmrsEncounterType(encounter);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsEncounterType.ENCOUNTER));
	}
	
	@Test
	public void getOpenmrsEncounterType_shouldReturnAmbiguousWhenBothTypesPresent() {
		Encounter encounter = new Encounter();
		CodeableConcept encounterType = new CodeableConcept();
		encounterType.addCoding(new Coding(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI, "uuid", null));
		CodeableConcept visitType = new CodeableConcept();
		visitType.addCoding(new Coding(FhirConstants.VISIT_TYPE_SYSTEM_URI, "uuid", null));
		encounter.setType(Arrays.asList(encounterType, visitType));
		
		Optional<FhirUtils.OpenmrsEncounterType> result = FhirUtils.getOpenmrsEncounterType(encounter);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo(FhirUtils.OpenmrsEncounterType.AMBIGUOUS));
	}
	
	@Test
	public void getOpenmrsEncounterType_shouldReturnEmptyWhenNoCodingPresent() {
		Encounter encounter = new Encounter();
		
		Optional<FhirUtils.OpenmrsEncounterType> result = FhirUtils.getOpenmrsEncounterType(encounter);
		
		assertThat(result.isPresent(), equalTo(false));
	}
	
	@Test
	public void createExceptionErrorOperationOutcome_shouldSetDiagnostics() {
		OperationOutcome outcome = FhirUtils.createExceptionErrorOperationOutcome("error");
		
		assertThat(outcome.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.ERROR));
		assertThat(outcome.getIssueFirstRep().getDiagnostics(), equalTo("error"));
	}
	
	@Test
	public void newUuid_shouldReturnValidUuid() {
		String uuid = FhirUtils.newUuid();
		
		assertThat(uuid.length(), equalTo(36));
	}
	
	@Test
	public void getReferenceType_shouldUseReferenceWhenTypeMissing() {
		org.hl7.fhir.r4.model.Reference reference = new org.hl7.fhir.r4.model.Reference();
		reference.setReference("Patient/1234");
		
		Optional<String> result = FhirUtils.getReferenceType(reference);
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo("Patient"));
	}
	
	@Test
	public void referenceToId_shouldExtractIdFromRelativeReference() {
		Optional<String> result = FhirUtils.referenceToId("Observation/xyz");
		
		assertThat(result.isPresent(), equalTo(true));
		assertThat(result.get(), equalTo("xyz"));
	}
	
	@Test
	public void referenceToType_shouldReturnEmptyForInvalidReference() {
		Optional<String> result = FhirUtils.referenceToType("not-a-reference");
		
		assertThat(result.isPresent(), equalTo(false));
	}
	
}
