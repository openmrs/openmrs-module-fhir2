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

import org.springframework.beans.factory.annotation.Value;

public final class FhirConstants {
	
	private FhirConstants() {
	}
	
	public static final String FHIR2_MODULE_ID = "fhir2";
	
	public static final String OPENMRS_FHIR_SERVER_NAME = "OpenMRS FHIR Server";
	
	public static final String HL7_FHIR_CODE_SYSTEM_PREFIX = "http://terminology.hl7.org/CodeSystem";
	
	public static final String HL7_FHIR_CODE_SYSTEM_PREFIX_R3 = "http://hl7.org/fhir";
	
	public static final String FHIR_TERMINOLOGY_DATA_OPERATION = HL7_FHIR_CODE_SYSTEM_PREFIX + "v3-DataOperation";
	
	public static final String FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "provenance-participant-type";
	
	public static final String FHIR_TERMINOLOGY_PARTICIPATION_TYPE = HL7_FHIR_CODE_SYSTEM_PREFIX + "v3-ParticipationType";
	
	public static final String DIAGNOSTIC_REPORT_SERVICE_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/v2-0074";
	
	public static final String INTERPRETATION_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/v3-ObservationInterpretation";
	
	public static final String ALLERGY_INTOLERANCE_CLINICAL_STATUS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "/allergyintolerance-clinical";
	
	public static final String ALLERGY_INTOLERANCE_VERIFICATION_STATUS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "/allergyintolerance-verification";
	
	public static final String CONDITION_CLINICAL_STATUS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/condition-clinical";
	
	public static final String CONDITION_CLINICAL_STATUS_SYSTEM_URI_R3 = HL7_FHIR_CODE_SYSTEM_PREFIX_R3
	        + "/condition-clinical";
	
	public static final String CONDITION_VERIFICATION_STATUS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "/condition-ver-status";
	
	public static final String CONDITION_VERIFICATION_STATUS_SYSTEM_URI_R3 = HL7_FHIR_CODE_SYSTEM_PREFIX_R3
	        + "/condition-ver-status";
	
	public static final String CLINICAL_FINDINGS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/clinical-findings";
	
	public static final String TASK_STATUS_VALUE_SET_URI = "http://hl7.org/fhir/task-status";
	
	public static final String OBSERVATION_REFERENCE_RANGE_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX
	        + "/referencerange-meaning";
	
	public static final String OBSERVATION_REFERENCE_NORMAL = "normal";
	
	public static final String OBSERVATION_REFERENCE_TREATMENT = "treatment";
	
	public static final String OBSERVATION_REFERENCE_ABSOLUTE = "absolute";
	
	public static final String OBSERVATION_CATEGORY_VALUE_SET_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/observation-category";
	
	public static final String ENCOUNTER_CLASS_VALUE_SET_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/v3-ActCode";
	
	@Value("${project.version}")
	public static String OPENMRS_FHIR_SERVER_VERSION;
	
	public static final String OPENMRS_FHIR_PREFIX = "http://fhir.openmrs.org";
	
	public static final String OPENMRS_CODE_SYSTEM_PREFIX = OPENMRS_FHIR_PREFIX + "/code-system";
	
	public static final String ENCOUNTER_TYPE_SYSTEM_URI = OPENMRS_CODE_SYSTEM_PREFIX + "/encounter-type";
	
	public static final String VISIT_TYPE_SYSTEM_URI = OPENMRS_CODE_SYSTEM_PREFIX + "/visit-type";
	
	public static final String OPENMRS_FHIR_EXT_IMMUNIZATION_NEXT_DOSE_DATE = "http://hl7.eu/fhir/StructureDefinition/immunization-nextDoseDate";
	
	public static final String OPENMRS_FHIR_EXT_PREFIX = OPENMRS_FHIR_PREFIX + "/ext";
	
	public static final String OPENMRS_FHIR_EXT_OBS_LOCATION_VALUE = OPENMRS_FHIR_EXT_PREFIX + "/obs-location-value";
	
	public static final String OPENMRS_FHIR_EXT_NAME = OPENMRS_FHIR_EXT_PREFIX + "/name";
	
	public static final String OPENMRS_FHIR_EXT_GROUP_DESCRIPTION = OPENMRS_FHIR_EXT_PREFIX + "/group/description";
	
	public static final String OPENMRS_FHIR_EXT_ADDRESS = OPENMRS_FHIR_EXT_PREFIX + "/address";
	
	public static final String OPENMRS_FHIR_EXT_NON_CODED_CONDITION = OPENMRS_FHIR_EXT_PREFIX + "/non-coded-condition";
	
	public static final String OPENMRS_FHIR_EXT_MEDICINE = OPENMRS_FHIR_EXT_PREFIX + "/medicine";
	
	public static final String OPENMRS_FHIR_EXT_TASK_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/task/identifier";
	
	public static final String OPENMRS_FHIR_EXT_USER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/user/identifier";
	
	public static final String OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE = OPENMRS_FHIR_EXT_PREFIX + "/person-attribute";
	
	public static final String OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE = OPENMRS_FHIR_EXT_PREFIX + "/person-attribute-type";
	
	public static final String OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE = OPENMRS_FHIR_EXT_PREFIX + "/person-attribute-value";
	
	public static final String OPENMRS_FHIR_EXT_PROVIDER_IDENTIFIER = OPENMRS_FHIR_EXT_PREFIX + "/provider/identifier";
	
	public static final String OPENMRS_FHIR_EXT_LOCATION_TAG = OPENMRS_FHIR_EXT_PREFIX + "/location-tag";
	
	public static final String OPENMRS_FHIR_EXT_VS_INTERPRETATION = OPENMRS_FHIR_EXT_PREFIX + "/obs/interpretations";
	
	public static final String OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION = OPENMRS_FHIR_EXT_PREFIX
	        + "/patient/identifier#location";
	
	public static final String OPENMRS_FHIR_EXT_ENCOUNTER_TAG = OPENMRS_FHIR_EXT_PREFIX + "/encounter-tag";
	
	public static final String OPENMRS_FHIR_EXT_OBSERVATION_REFERENCE_RANGE = OPENMRS_FHIR_EXT_PREFIX
	        + "/obs/reference-range";
	
	public static final String OPENMRS_FHIR_EXT_RECORDED = OPENMRS_FHIR_EXT_PREFIX + "/medicationdispense/recorded";
	
	public static final String OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS = OPENMRS_FHIR_EXT_PREFIX
	        + "/medicationrequest/fulfillerstatus";
	
	public static final String OPENMRS_FHIR_DEFAULT_PAGE_SIZE = "fhir2.paging.default";
	
	public static final String OPENMRS_FHIR_MAXIMUM_PAGE_SIZE = "fhir2.paging.maximum";
	
	public static final String ALLERGY_INTOLERANCE = "AllergyIntolerance";
	
	public static final String CONDITION = "Condition";
	
	public static final String DIAGNOSTIC_REPORT = "DiagnosticReport";
	
	public static final String ENCOUNTER = "Encounter";
	
	public static final String IDENTIFIER = "Identifier";
	
	public static final String IMMUNIZATION = "Immunization";
	
	public static final String LOCATION = "Location";
	
	public static final String MEDICATION = "Medication";
	
	public static final String GROUP = "Group";
	
	public static final String MEDICATION_DISPENSE = "MedicationDispense";
	
	public static final String MEDICATION_REQUEST = "MedicationRequest";
	
	public static final String OBSERVATION = "Observation";
	
	public static final String ORGANIZATION = "Organization";
	
	public static final String PATIENT = "Patient";
	
	public static final String PERSON = "Person";
	
	public static final String PRACTITIONER = "Practitioner";
	
	public static final String PROCEDURE_REQUEST = "ProcedureRequest";
	
	public static final String RELATED_PERSON = "RelatedPerson";
	
	public static final String SERVICE_REQUEST = "ServiceRequest";
	
	public static final String TASK = "Task";
	
	public static final String DIAGNOSTIC_REPORT_CATEGORY_LAB = "LAB";
	
	public static final String VALUESET = "ValueSet";
	
	public static final String PERSON_CONTACT_POINT_ATTRIBUTE_TYPE = "fhir2.personContactPointAttributeTypeUuid";
	
	public static final String LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE = "fhir2.locationContactPointAttributeTypeUuid";
	
	public static final String PROVIDER_CONTACT_POINT_ATTRIBUTE_TYPE = "fhir2.providerContactPointAttributeTypeUuid";
	
	public static final String LOCATION_TYPE_ATTRIBUTE_TYPE = "fhir2.locationTypeAttributeTypeUuid";
	
	public static final String IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY = "fhir2.immunizationsEncounterTypeUuid";
	
	public static final String ADMINISTERING_ENCOUNTER_ROLE_PROPERTY = "fhir2.administeringEncounterRoleUuid";
	
	public static final String SUPPORTED_LOCATION_HIERARCHY_SEARCH_DEPTH = "fhir.supportedLocationHierarchySearchDepth";
	
	public static final String GLOBAL_PROPERTY_MILD = "allergy.concept.severity.mild";
	
	public static final String GLOBAL_PROPERTY_SEVERE = "allergy.concept.severity.severe";
	
	public static final String GLOBAL_PROPERTY_MODERATE = "allergy.concept.severity.moderate";
	
	public static final String GLOBAL_PROPERTY_OTHER = "allergy.concept.severity.other";
	
	public static final String GLOBAL_PROPERTY_URI_PREFIX = "fhir2.uriPrefix";
	
	public static final String ENCOUNTER_REFERENCE_SEARCH_HANDLER = "encounter.reference.search.handler";
	
	public static final String PATIENT_REFERENCE_SEARCH_HANDLER = "patient.reference.search.handler";
	
	public static final String MEDICATION_REQUEST_REFERENCE_SEARCH_HANDLER = "medicationRequest.reference.search.handler";
	
	public static final String MEDICATION_REFERENCE_SEARCH_HANDLER = "medication.reference.search.handler";
	
	public static final String CODED_SEARCH_HANDLER = "coded.search.handler";
	
	public static final String CATEGORY_SEARCH_HANDLER = "category.search.handler";
	
	public static final String VALUE_CODED_SEARCH_HANDLER = "value.coded.search.handler";
	
	public static final String DATE_RANGE_SEARCH_HANDLER = "date.range.search.handler";
	
	public static final String HAS_MEMBER_SEARCH_HANDLER = "obs.has.member.search.handler";
	
	public static final String QUANTITY_SEARCH_HANDLER = "quantity.search.handler";
	
	public static final String VALUE_STRING_SEARCH_HANDLER = "value.string.search.handler";
	
	public static final String GENDER_SEARCH_HANDLER = "gender.search.handler";
	
	public static final String IDENTIFIER_SEARCH_HANDLER = "identifier.search.handler";
	
	public static final String BOOLEAN_SEARCH_HANDLER = "boolean.search.handler";
	
	public static final String ADDRESS_SEARCH_HANDLER = "address.search.handler";
	
	public static final String NAME_SEARCH_HANDLER = "name.search.handler";
	
	public static final String QUERY_SEARCH_HANDLER = "query.search.handler";
	
	public static final String NAME_PROPERTY = "name.property";
	
	public static final String GIVEN_PROPERTY = "given.property";
	
	public static final String FAMILY_PROPERTY = "family.property";
	
	public static final String GENDER_PROPERTY = "gender";
	
	public static final String BIRTHDATE_PROPERTY = "birthdate";
	
	public static final String DEATHDATE_PROPERTY = "deathDate";
	
	public static final String DECEASED_PROPERTY = "dead";
	
	public static final String CITY_PROPERTY = "city.property";
	
	public static final String COUNTRY_PROPERTY = "country.property";
	
	public static final String STATE_PROPERTY = "state.property";
	
	public static final String POSTAL_CODE_PROPERTY = "postalCode.property";
	
	public static final String PARTICIPANT_REFERENCE_SEARCH_HANDLER = "participant.reference.search.handler";
	
	public static final String CITY_SEARCH_HANDLER = "city.search.handler";
	
	public static final String STATE_SEARCH_HANDLER = "state.search.handler";
	
	public static final String COUNTRY_SEARCH_HANDLER = "country.search.handler";
	
	public static final String POSTALCODE_SEARCH_HANDLER = "postalcode.search.handler";
	
	public static final String LOCATION_REFERENCE_SEARCH_HANDLER = "location.reference.search.handler";
	
	public static final String DOSAGE_FORM_SEARCH_HANDLER = "dosage.form.search.handler";
	
	public static final String INGREDIENT_SEARCH_HANDLER = "ingredient.search.handler";
	
	public static final String TAG_SEARCH_HANDLER = "tag.search.handler";
	
	public static final String GROUP_MEMBERS_SEARCH_HANDLER = "group.members.search.handler";
	
	public static final String HAPI_NARRATIVES_PROPERTY_FILE = "classpath:ca/uhn/fhir/narrative/narratives.properties";
	
	public static final String OPENMRS_NARRATIVES_PROPERTY_FILE = "classpath:org/openmrs/module/fhir2/narratives.properties";
	
	public static final String NARRATIVES_OVERRIDE_PROPERTY_FILE = "fhir2.narrativesOverridePropertyFile";
	
	public static final String ALLERGEN_SEARCH_HANDLER = "allergen.search.handler";
	
	public static final String SEVERITY_SEARCH_HANDLER = "severity.search.handler";
	
	public static final String CONDITION_CLINICAL_STATUS_HANDLER = "condition.clinical.status.handler";
	
	public static final String STATUS_SEARCH_HANDLER = "status.search.handler";
	
	public static final String TASK_CODE_SEARCH_HANDLER = "task.code.search.handler";
	
	public static final String FULFILLER_STATUS_SEARCH_HANDLER = "fulfillerStatus.search.handler";
	
	public static final String BASED_ON_REFERENCE_SEARCH_HANDLER = "based.on.reference.search.handler";
	
	public static final String OWNER_REFERENCE_SEARCH_HANDLER = "owner.reference.search.handler";
	
	public static final String FOR_REFERENCE_SEARCH_HANDLER = "for.reference.search.handler";
	
	public static final String RESULT_SEARCH_HANDLER = "result.search.handler";
	
	public static final String COMMON_SEARCH_HANDLER = "common.search.handler";
	
	public static final String ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER = "type.reference.search.handler";
	
	public static final String ID_PROPERTY = "_id.property";
	
	public static final String LAST_UPDATED_PROPERTY = "_lastUpdated.property";
	
	public static final String SP_LAST_UPDATED = "_lastUpdated";
	
	public static final String SP_FULFILLER_STATUS = "fulfillerStatus";
	
	public static final String SERVLET_PATH_R4 = "/ms/fhir2Servlet";
	
	public static final String SERVLET_PATH_R3 = "/ms/fhir2R3Servlet";
	
	public static final String INCLUDE_SEARCH_HANDLER = "_include.search.handler";
	
	public static final String INCLUDE_PART_OF_PARAM = "partof";
	
	public static final String INCLUDE_PARTICIPANT_PARAM = "participant";
	
	public static final String INCLUDE_LOCATION_PARAM = "location";
	
	public static final String INCLUDE_PATIENT_PARAM = "patient";
	
	public static final String INCLUDE_ENCOUNTER_PARAM = "encounter";
	
	public static final String INCLUDE_CONTEXT_PARAM = "context";
	
	public static final String INCLUDE_REQUESTER_PARAM = "requester";
	
	public static final String INCLUDE_PERFORMER_PARAM = "performer";
	
	public static final String INCLUDE_PRESCRIPTION_PARAM = "prescription";
	
	public static final String INCLUDE_MEDICATION_PARAM = "medication";
	
	public static final String INCLUDE_HAS_MEMBER_PARAM = "has-member";
	
	public static final String INCLUDE_RELATED_TYPE_PARAM = "related-type";
	
	public static final String INCLUDE_LINK_PARAM = "link";
	
	public static final String INCLUDE_RESULT_PARAM = "result";
	
	public static final String INCLUDE_BASED_0N_PARAM = "based-on";
	
	public static final String INCLUDE_OWNER_PARAM = "owner";
	
	public static final String INCLUDE_PRESCRIPTION_PARAMETER = "prescription";
	
	public static final String REVERSE_INCLUDE_SEARCH_HANDLER = "_revinclude.search.handler";
	
	public static final String MAX_SEARCH_HANDLER = "max.search.handler";
	
	public static final String LASTN_OBSERVATION_SEARCH_HANDLER = "lastn.observation.search.handler";
	
	public static final String LASTN_ENCOUNTERS_SEARCH_HANDLER = "lastn.encounters.search.handler";
	
	public static final String EVERYTHING_SEARCH_HANDLER = "everything.search.handler";
	
	public static final String TITLE_SEARCH_HANDLER = "title.search.handler";
	
	public static final String HAS_SEARCH_HANDLER = "_has";
	
	public static final String CONDITION_OBSERVATION_CONCEPT_UUID = "1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String SNOMED_SYSTEM_URI = "http://snomed.info/sct";
	
	public static final String RX_NORM_SYSTEM_URI = "http://www.nlm.nih.gov/research/umls/rxnorm";
	
	public static final String UCUM_SYSTEM_URI = "http://unitsofmeasure.org";
	
	public static final String EXACT_TOTAL_SEARCH_PARAMETER = "_exactTotal";
	
	public static final String COUNT_QUERY_CACHE = "countQueryCache";
	
	public static final String INCLUDE_MEMBER_PARAM = "member";
	
	public static final String CONDITION_CATEGORY_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/condition-category";
	
	public static final String CONDITION_CATEGORY_CODE_CONDITION = "problem-list-item";
	
	public static final String CONDITION_CATEGORY_CODE_DIAGNOSIS = "encounter-diagnosis";
	
	public static final String DIAGNOSIS_RANK_EXTENSION_URI = "http://fhir.openmrs.org/R4/StructureDefinition/diagnosis-rank";
	
	public static final String CONDITION_CLINICAL_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/condition-clinical";
	
	public static final String CONDITION_VER_STATUS_SYSTEM_URI = HL7_FHIR_CODE_SYSTEM_PREFIX + "/condition-ver-status";
	
	public static final String USER_DATA_KEY_CREATE_IF_NOT_EXISTS = "fhir2_module_create_if_not_exists";
	
	public static final String USER_DATA_KEY_OUTCOME_CREATED = "fhir2_module_outcome_created";
	
}
