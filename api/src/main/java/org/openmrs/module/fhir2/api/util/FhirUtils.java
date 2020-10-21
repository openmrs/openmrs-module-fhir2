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

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;

public class FhirUtils {
	
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
		issue.setCode(OperationOutcome.IssueType.EXCEPTION);
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setDiagnostics(diagnostics);
		return outcome;
	}
}
