/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
public class FhirConstants {
	
	public static final String OPENMRS_FHIR_SERVER_NAME = "OpenMRS FHIR Server";
	
	public static final String OPENMRS_URI = "http://openmrs.org";
	
	public static final String HL7_FHIR_VALUE_SET_PREFIX = "http://hl7.org/fhir/ValueSet";
	
	public static final String HL7_FHIR_CODE_SYSTEM_PREFIX = "http://terminology.hl7.org/CodeSystem";
	
	public static final String FHIR_TERMINOLOGY_DATA_OPERATION = HL7_FHIR_CODE_SYSTEM_PREFIX + "v3-DataOperation";
	
	public static final String FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "provenance-participant-type";
	
	public static final String FHIR_TERMINOLOGY_PARTICIPATION_TYPE = HL7_FHIR_CODE_SYSTEM_PREFIX + "v3-ParticipationType";
	
	public static final String DIAGNOSTIC_SERVICE_SECTIONS_VALUESET_URI = HL7_FHIR_VALUE_SET_PREFIX
	        + "/diagnostic-service-sections";
	
	public static final String INTERPRETATION_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/observation-interpretation";
	
	public static final String CONDITION_VERIFICATION_STATUS_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX
	        + "/condition-ver-status";
	
	public static final String ALLERGY_SUBSTANCE_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/substance-code";
	
	public static final String CLINICAL_FINDINGS_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/clinical-findings";
	
	public static final String MEDICATION_CODES_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/medication-codes";
	
	public static final String MEDICATION_FORM_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/medication-form-codes";
	
	public static final String MEDICATION_INGREDIENT_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX
	        + "/medication-ingredient-codes";
	
	public static final String ALLERGY_INTOLERANCE_CLINICAL_STATUS_VALUE_SET = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "/allergyintolerance-clinical";
	
	public static final String OBSERVATION_REFERENCE_NORMAL = "normal";
	
	public static final String OBSERVATION_REFERENCE_TREATMENT = "treatment";
	
	public static final String OBSERVATION_REFERENCE_RANGE_URI = "http://terminology.hl7.org/CodeSystem/referencerange-meaning";
	
	@Value("${project.version}")
	public static String OPENMRS_FHIR_SERVER_VERSION;
	
	public static final String OPENMRS_FHIR_PREFIX = "https://fhir.openmrs.org";
	
	public static final String OPENMRS_FHIR_EXT_PREFIX = OPENMRS_FHIR_PREFIX + "/ext";
	
	public static final String OPENMRS_FHIR_EXT_NAME = OPENMRS_FHIR_EXT_PREFIX + "/name";
	
	public static final String OPENMRS_FHIR_EXT_ADDRESS = OPENMRS_FHIR_EXT_PREFIX + "/address";
	
	public static final String OPENMRS_FHIR_EXT_USER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/user/identifier";
	
	public static final String OPENMRS_FHIR_EXT_PROVIDER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/provider/identifier";
	
	public static final String OPENMRS_FHIR_EXT_LOCATION_TAG = OPENMRS_FHIR_EXT_PREFIX + "/location-tag";
	
	public static final String OPENMRS_FHIR_EXT_VS_INTERPRETATION = OPENMRS_FHIR_EXT_PREFIX + "/obs/interpretations";
	
	public static final String OPENMRS_FHIR_DEFAULT_PAGE_SIZE = "fhir2.paging.default";
	
	public static final String OPENMRS_FHIR_MAXIMUM_PAGE_SIZE = "fhir2.paging.maximum";
	
	public static final String ENCOUNTER = "Encounter";
	
	public static final String OBSERVATION = "Observation";
	
	public static final String LOCATION = "Location";
	
	public static final String PATIENT = "Patient";
	
	public static final String PRACTITIONER = "Practitioner";
	
	public static final String IDENTIFIER = "identifier";
	
	public static final String DIAGNOSTIC_REPORT = "DiagnosticReport";
	
	public static final String DIAGNOSTIC_REPORT_CATEGORY_LAB = "LAB";
	
	@Value("${project.parent.artifactId}.personAttributeTypeUuid")
	public static String PERSON_ATTRIBUTE_TYPE_PROPERTY;
	
	@Value("${project.parent.artifactId}.locationAttributeTypeUuid")
	public static String LOCATION_ATTRIBUTE_TYPE_PROPERTY;
	
	@Value("${project.parent.artifactId}.providerAttributeTypeUuid")
	public static String PROVIDER_ATTRIBUTE_TYPE_PROPERTY;
	
	public static final String GLOBAL_PROPERTY_MILD = "allergy.concept.severity.mild";
	
	public static final String GLOBAL_PROPERTY_SEVERE = "allergy.concept.severity.severe";
	
	public static final String GLOBAL_PROPERTY_MODERATE = "allergy.concept.severity.moderate";
	
	public static final String GLOBAL_PROPERTY_OTHER = "allergy.concept.severity.other";
	
	public static final String AUTHOR = "author";
	
	public static final String AUT = "AUT";
	
}
