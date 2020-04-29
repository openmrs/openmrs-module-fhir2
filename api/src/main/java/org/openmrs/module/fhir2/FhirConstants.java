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
	
	public static final String TASK_STATUS_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/task-status";
	
	public static final String RESOURCE_TYPES_VALUE_SET_URI = HL7_FHIR_VALUE_SET_PREFIX + "/resource-types";
	
	public static final String OBSERVATION_REFERENCE_NORMAL = "normal";
	
	public static final String OBSERVATION_REFERENCE_TREATMENT = "treatment";
	
	public static final String OBSERVATION_REFERENCE_RANGE_URI = "http://terminology.hl7.org/CodeSystem/referencerange-meaning";
	
	@Value("${project.version}")
	public static String OPENMRS_FHIR_SERVER_VERSION;
	
	public static final String OPENMRS_FHIR_PREFIX = "https://fhir.openmrs.org";
	
	public static final String OPENMRS_FHIR_EXT_PREFIX = OPENMRS_FHIR_PREFIX + "/ext";
	
	public static final String OPENMRS_FHIR_EXT_NAME = OPENMRS_FHIR_EXT_PREFIX + "/name";
	
	public static final String OPENMRS_FHIR_EXT_ADDRESS = OPENMRS_FHIR_EXT_PREFIX + "/address";
	
	public static final String OPENMRS_FHIR_EXT_MEDICINE = OPENMRS_FHIR_EXT_PREFIX + "/medicine";
	
	public static final String RELATED_PERSON = "RelatedPerson";
	
	public static final String PERSON = "Person";
	
	public static final String OPENMRS_FHIR_EXT_USER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/user/identifier";
	
	public static final String OPENMRS_FHIR_EXT_PROVIDER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/provider/identifier";
	
	public static final String OPENMRS_FHIR_EXT_LOCATION_TAG = OPENMRS_FHIR_EXT_PREFIX + "/location-tag";
	
	public static final String OPENMRS_FHIR_EXT_VS_INTERPRETATION = OPENMRS_FHIR_EXT_PREFIX + "/obs/interpretations";
	
	public static final String OPENMRS_FHIR_DEFAULT_PAGE_SIZE = "fhir2.paging.default";
	
	public static final String OPENMRS_FHIR_MAXIMUM_PAGE_SIZE = "fhir2.paging.maximum";
	
	public static final String ENCOUNTER = "Encounter";
	
	public static final String OBSERVATION = "Observation";
	
	public static final String ORGANIZATION = "Organization";
	
	public static final String LOCATION = "Location";
	
	public static final String PATIENT = "Patient";
	
	public static final String PRACTITIONER = "Practitioner";
	
	public static final String IDENTIFIER = "identifier";
	
	public static final String DIAGNOSTIC_REPORT = "DiagnosticReport";
	
	public static final String SERVICE_REQUEST = "ServiceRequest";
	
	public static final String TASK = "Task";
	
	public static final String MEDICATION = "Medication";
	
	public static final String DIAGNOSTIC_REPORT_CATEGORY_LAB = "LAB";
	
	public static String PERSON_ATTRIBUTE_TYPE_PROPERTY = "fhir2.personAttributeTypeUuid";
	
	public static String LOCATION_ATTRIBUTE_TYPE_PROPERTY = "fhir2.locationAttributeTypeUuid";
	
	public static String PROVIDER_ATTRIBUTE_TYPE_PROPERTY = "fhir2.providerAttributeTypeUuid";
	
	public static final String GLOBAL_PROPERTY_MILD = "allergy.concept.severity.mild";
	
	public static final String GLOBAL_PROPERTY_SEVERE = "allergy.concept.severity.severe";
	
	public static final String GLOBAL_PROPERTY_MODERATE = "allergy.concept.severity.moderate";
	
	public static final String GLOBAL_PROPERTY_OTHER = "allergy.concept.severity.other";
	
	public static final String GLOBAL_PROPERTY_URI_PREFIX = "fhir2.uriPrefix";
	
	public static final String AUTHOR = "author";
	
	public static final String AUT = "AUT";
	
	public static final String AND_LIST_PARAMS_SEARCH_HANDLER = "and.list.params.search.handler";
	
	public static final String OR_LIST_PARAMS_SEARCH_HANDLER = "or.list.params.search.handler";
	
	public static final String ENCOUNTER_REFERENCE_SEARCH_HANDLER = "encounter.reference.search.handler";
	
	public static final String PATIENT_REFERENCE_SEARCH_HANDLER = "patient.reference.search.handler";
	
	public static final String CODED_SEARCH_HANDLER = "coded.search.handler";
	
	public static final String VALUE_CODED_SEARCH_HANDLER = "value.coded.search.handler";
	
	public static final String DATE_RANGE_SEARCH_HANDLER = "date.range.search.handler";
	
	public static final String HAS_MEMBER_SEARCH_HANDLER = "obs.has.member.search.handler";
	
	public static final String QUANTITY_SEARCH_HANDLER = "quantity.search.handler";
	
	public static final String VALUE_STRING_SEARCH_HANDLER = "value.string.search.handler";
	
}
