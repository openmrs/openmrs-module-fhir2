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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;

@Slf4j
public class FhirUtils {
	
	private FhirUtils() {
	}
	
	public enum OpenmrsEncounterType {
		ENCOUNTER,
		VISIT,
		AMBIGUOUS
	}
	
	public enum OpenmrsConditionType {
		CONDITION,
		DIAGNOSIS
	}
	
	// see https://www.hl7.org/fhir/references.html#literal
	private static final Pattern FHIR_URL = Pattern.compile("(?:(?:http|https)://(?:[A-Za-z0-9\\-.:%$\\\\]*/)+)?"
	        + "(?<type>Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse"
	        + "|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan"
	        + "|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression"
	        + "|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap"
	        + "|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse"
	        + "|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement"
	        + "|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint"
	        + "|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable"
	        + "|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group"
	        + "|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation"
	        + "|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location"
	        + "|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense"
	        + "|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct"
	        + "|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication"
	        + "|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured"
	        + "|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect"
	        + "|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation"
	        + "|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation"
	        + "|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole"
	        + "|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup"
	        + "|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment"
	        + "|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition"
	        + "|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer"
	        + "|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification"
	        + "|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet"
	        + "|VerificationResult|VisionPrescription)"
	        + "/(?<id>[A-Za-z0-9\\-.]{1,64})(?:/_history/(?<version>[A-Za-z0-9\\-.]{1,64}))?");
	
	public static String newUuid() {
		return UUID.randomUUID().toString();
	}
	
	public static Optional<String> getReferenceType(Reference reference) {
		if (reference == null || !(reference.hasType() || reference.hasReference())) {
			return Optional.empty();
		}
		
		if (reference.hasType()) {
			return Optional.of(reference.getType());
		} else {
			return referenceToType(reference.getReference());
		}
	}
	
	public static Optional<String> referenceToType(String reference) {
		if (StringUtils.isBlank(reference)) {
			return Optional.empty();
		}
		
		Matcher m = FHIR_URL.matcher(reference);
		if (m.matches()) {
			return Optional.of(m.group("type"));
		}
		
		return Optional.empty();
	}
	
	public static Optional<String> referenceToId(String reference) {
		if (StringUtils.isBlank(reference)) {
			return Optional.empty();
		}
		
		Matcher m = FHIR_URL.matcher(reference);
		if (m.matches()) {
			return Optional.of(m.group("id"));
		}
		
		return Optional.empty();
	}
	
	public static OperationOutcome createExceptionErrorOperationOutcome(String diagnostics) {
		OperationOutcome outcome = new OperationOutcome();
		OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
		issue.setCode(OperationOutcome.IssueType.BUSINESSRULE);
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setDiagnostics(diagnostics);
		return outcome;
	}
	
	public static Optional<OpenmrsEncounterType> getOpenmrsEncounterType(Encounter encounter) {
		List<OpenmrsEncounterType> openmrsEncounterTypes = encounter.getType().stream().flatMap(
		    it -> it.getCoding().stream().filter(coding -> FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI.equals(coding.getSystem())
		            || FhirConstants.VISIT_TYPE_SYSTEM_URI.equals(coding.getSystem())).map(coding -> {
			            if (FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI.equals(coding.getSystem())) {
				            return OpenmrsEncounterType.ENCOUNTER;
			            } else {
				            return OpenmrsEncounterType.VISIT;
			            }
		            }))
		        .distinct().collect(Collectors.toList());
		
		if (openmrsEncounterTypes.isEmpty()) {
			return Optional.empty();
		} else if (openmrsEncounterTypes.size() == 1) {
			return Optional.of(openmrsEncounterTypes.get(0));
		} else {
			return Optional.of(OpenmrsEncounterType.AMBIGUOUS);
		}
	}
	
	/*
	 * Determines the condition type based on the given category:
	 * 1. "encounter-diagnosis" → Diagnosis
	 * 2. "problem-list-item" → Condition
	 * 3. Any other category → returns empty
	 *
	 * Special case:
	 * - If category is empty, returns Condition.
	 *
	 * @param condition The condition object to evaluate.
	 * @return The condition type: "Diagnosis", "Condition", or empty.
	 */
	public static Optional<OpenmrsConditionType> getOpenmrsConditionType(Condition condition) {
		if (!condition.hasCategory()) {
			return Optional.of(OpenmrsConditionType.CONDITION);
		}
		
		List<Coding> categoryCodings = condition.getCategory().stream().filter(CodeableConcept::hasCoding)
		        .flatMap(category -> category.getCoding().stream()).collect(Collectors.toList());
		
		if (categoryCodings.isEmpty()) {
			return Optional.of(OpenmrsConditionType.CONDITION);
		}
		
		List<Coding> hl7Codings = categoryCodings.stream()
		        .filter(coding -> FhirConstants.CONDITION_CATEGORY_SYSTEM_URI.equals(coding.getSystem()))
		        .collect(Collectors.toList());
		
		if (hl7Codings.isEmpty()) {
			return Optional.empty();
		}
		
		return hl7Codings.stream().map(Coding::getCode).map(code -> {
			if (FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS.equals(code)) {
				return OpenmrsConditionType.DIAGNOSIS;
			} else if (FhirConstants.CONDITION_CATEGORY_CODE_CONDITION.equals(code)) {
				return OpenmrsConditionType.CONDITION;
			}
			
			return null;
		}).filter(Objects::nonNull).findFirst();
	}
	
	/**
	 * Provides implementation-defined localizations for OpenMRS Metadata. This should be used in any
	 * display fields to override the default value.
	 *
	 * @param metadata the piece of OpenMRS Metadata to localize
	 * @return localization for the given metadata, from message source, in the authenticated locale
	 */
	public static String getMetadataTranslation(OpenmrsMetadata metadata) {
		String className = metadata.getClass().getSimpleName();
		String uuid = metadata.getUuid();
		
		String localization = getLocalization(className, uuid);
		
		if (localization == null) {
			return metadata.getName();
		} else {
			return localization;
		}
	}
	
	public static String getMetadataTranslation(Concept concept) {
		String className = concept.getClass().getSimpleName();
		String uuid = concept.getUuid();
		
		String localization = getLocalization(className, uuid);
		
		if (localization == null) {
			return concept.getName().getName();
		} else {
			return localization;
		}
	}
	
	// This code is from the REST module which derived it from the UI framework
	private static String getLocalization(String shortClassName, String uuid) {
		// in case this is a hibernate proxy, strip off anything after an underscore
		// ie: EncounterType_$$_javassist_26 needs to be converted to EncounterType
		int underscoreIndex = shortClassName.indexOf("_$");
		if (underscoreIndex > 0) {
			shortClassName = shortClassName.substring(0, underscoreIndex);
		}
		
		String code = "ui.i18n." + shortClassName + ".name." + uuid;
		String localization = null;
		try {
			localization = Context.getMessageSourceService().getMessage(code, null, Context.getLocale());
		}
		catch (Exception e) {
			log.info("Caught exception while attempting to localize code [{}]", code, e);
		}
		
		if (localization == null || localization.equals(code)) {
			return null;
		} else {
			return localization;
		}
	}
}
