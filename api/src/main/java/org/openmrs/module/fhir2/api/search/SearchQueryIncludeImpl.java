/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.api.search.param.DiagnosticReportSearchParams;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.FhirAllergyIntoleranceSearchParams;
import org.openmrs.module.fhir2.api.search.param.LocationSearchParams;
import org.openmrs.module.fhir2.api.search.param.MedicationRequestSearchParams;
import org.openmrs.module.fhir2.api.search.param.ObservationSearchParams;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@OpenmrsProfile(openmrsPlatformVersion = "2.* - 2.5.*")
public class SearchQueryIncludeImpl<U extends IBaseResource> implements SearchQueryInclude<U> {
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirLocationService locationService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirObservationService observationService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirEncounterService encounterService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirPatientService patientService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirPractitionerService practitionerService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirMedicationService medicationService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirDiagnosticReportService diagnosticReportService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirMedicationRequestService medicationRequestService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirServiceRequestService serviceRequestService;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirAllergyIntoleranceService allergyIntoleranceService;
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<IBaseResource> getIncludedResources(List<U> resourceList, SearchParameterMap theParams) {
		List<PropParam<Object>> includeParamList = theParams.getParameters(FhirConstants.INCLUDE_SEARCH_HANDLER);
		List<PropParam<Object>> revIncludeParamList = theParams.getParameters(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER);
		
		Set<Include> includeSet = new LinkedHashSet<>();
		Set<Include> revIncludeSet = new LinkedHashSet<>();
		
		if (CollectionUtils.isNotEmpty(includeParamList)) {
			includeSet = (Set<Include>) includeParamList.get(0).getParam();
		}
		
		if (CollectionUtils.isNotEmpty(revIncludeParamList)) {
			revIncludeSet = (Set<Include>) revIncludeParamList.get(0).getParam();
		}
		
		Set<? extends IBaseResource> _includeResources = handleInclude(resourceList, includeSet);
		Set<? extends IBaseResource> _revIncludeResources = handleRevInclude(resourceList, includeSet, revIncludeSet);
		
		Set<IBaseResource> resourcesToBeReturned = new LinkedHashSet<>();
		resourcesToBeReturned.addAll(_includeResources);
		resourcesToBeReturned.addAll(_revIncludeResources);
		
		return resourcesToBeReturned;
	}
	
	protected Set<? extends IBaseResource> handleInclude(List<U> resourceList, Set<Include> includeSet) {
		Set<IBaseResource> includedResourcesSet = new LinkedHashSet<>();
		
		if (CollectionUtils.isEmpty(includeSet)) {
			return includedResourcesSet;
		}
		
		includeSet.forEach((includeParam) -> {
			Optional.ofNullable(handleIncludeParam(resourceList, includeParam)).ifPresent(includedResourcesSet::addAll);
		});
		
		return includedResourcesSet;
	}
	
	protected Set<? extends IBaseResource> handleIncludeParam(List<U> resourceList, Include includeParam) {
		switch (includeParam.getParamName()) {
			case FhirConstants.INCLUDE_PART_OF_PARAM:
				return handleParentLocationInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_CONTEXT_PARAM:
			case FhirConstants.INCLUDE_ENCOUNTER_PARAM:
				return handleEncounterInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_PATIENT_PARAM:
				return handlePatientInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_HAS_MEMBER_PARAM:
			case FhirConstants.INCLUDE_RESULT_PARAM:
			case FhirConstants.INCLUDE_RELATED_TYPE_PARAM:
				return handleObsGroupInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_REQUESTER_PARAM:
			case FhirConstants.INCLUDE_PERFORMER_PARAM:
			case FhirConstants.INCLUDE_PARTICIPANT_PARAM:
				return handleParticipantInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_LOCATION_PARAM:
				return handleLocationInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_MEDICATION_PARAM:
				return handleMedicationInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_LINK_PARAM:
				return handlePersonLinkInclude(resourceList, includeParam.getParamTargetType());
			case FhirConstants.INCLUDE_BASED_0N_PARAM:
				return handleServiceRequestInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_OWNER_PARAM:
				return handlePractitionerInclude(resourceList, includeParam.getParamType());
			case FhirConstants.INCLUDE_PRESCRIPTION_PARAM:
				return handleMedicationRequestInclude(resourceList, includeParam.getParamType());
		}
		
		return null;
	}
	
	protected Set<IBaseResource> handleRevInclude(List<U> resourceList, Set<Include> includeSet,
	        Set<Include> revIncludeSet) {
		Set<IBaseResource> revIncludedResourcesSet = new LinkedHashSet<>();
		
		if (CollectionUtils.isEmpty(revIncludeSet)) {
			return revIncludedResourcesSet;
		}
		
		ReferenceAndListParam referenceParams = new ReferenceAndListParam();
		ReferenceOrListParam params = new ReferenceOrListParam();
		resourceList.forEach(resource -> params.addOr(new ReferenceParam(resource.getIdElement().getIdPart())));
		referenceParams.addAnd(params);
		
		revIncludeSet.forEach(revIncludeParam -> {
			IBundleProvider bundleProvider = handleRevIncludeParam(includeSet, revIncludeSet, referenceParams,
			    revIncludeParam);
			
			if (bundleProvider != null && !bundleProvider.isEmpty()) {
				revIncludedResourcesSet.addAll(bundleProvider.getResources(0, -1));
			}
		});
		
		return revIncludedResourcesSet;
	}
	
	protected IBundleProvider handleRevIncludeParam(Set<Include> includeSet, Set<Include> revIncludeSet,
	        ReferenceAndListParam referenceParams, Include revIncludeParam) {
		switch (revIncludeParam.getParamName()) {
			case FhirConstants.INCLUDE_PART_OF_PARAM:
			case FhirConstants.INCLUDE_LOCATION_PARAM:
				return handleLocationReverseInclude(referenceParams, revIncludeParam.getParamType());
			case FhirConstants.INCLUDE_CONTEXT_PARAM:
			case FhirConstants.INCLUDE_ENCOUNTER_PARAM:
				return handleEncounterReverseInclude(referenceParams, revIncludeParam.getParamType(),
				    getRecursiveIncludes(includeSet), getRecursiveIncludes(revIncludeSet));
			case FhirConstants.INCLUDE_MEDICATION_PARAM:
				return handleMedicationReverseInclude(referenceParams, revIncludeParam.getParamType(),
				    getRecursiveIncludes(includeSet), getRecursiveIncludes(revIncludeSet));
			case FhirConstants.INCLUDE_PATIENT_PARAM:
				return handlePatientReverseInclude(referenceParams, revIncludeParam.getParamType(),
				    getRecursiveIncludes(includeSet), getRecursiveIncludes(revIncludeSet));
			case FhirConstants.INCLUDE_HAS_MEMBER_PARAM:
			case FhirConstants.INCLUDE_RESULT_PARAM:
			case FhirConstants.INCLUDE_RELATED_TYPE_PARAM:
				return handleObservationReverseInclude(referenceParams, revIncludeParam.getParamType());
			case FhirConstants.INCLUDE_REQUESTER_PARAM:
			case FhirConstants.INCLUDE_PARTICIPANT_PARAM:
				return handlePractitionerReverseInclude(referenceParams, revIncludeParam.getParamType(),
				    getRecursiveIncludes(includeSet), getRecursiveIncludes(revIncludeSet));
		}
		
		return null;
	}
	
	protected Set<IBaseResource> handleEncounterInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueEncounterUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((Observation) resource).getEncounter())));
				break;
			case FhirConstants.DIAGNOSTIC_REPORT:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((DiagnosticReport) resource).getEncounter())));
				break;
			case FhirConstants.MEDICATION_REQUEST:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((MedicationRequest) resource).getEncounter())));
				break;
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((MedicationDispense) resource).getContext())));
				break;
			case FhirConstants.PROCEDURE_REQUEST:
			case FhirConstants.SERVICE_REQUEST:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((ServiceRequest) resource).getEncounter())));
				break;
			case FhirConstants.TASK:
				resourceList
				        .forEach(resource -> uniqueEncounterUUIDs.add(getIdFromReference(((Task) resource).getEncounter())));
				break;
		}
		
		uniqueEncounterUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(encounterService.get(uniqueEncounterUUIDs));
	}
	
	protected Set<IBaseResource> handleLocationInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueLocationUUIDs = new HashSet<>();
		
		switch (paramType) {
			case FhirConstants.ENCOUNTER:
				resourceList.forEach(resource -> {
					uniqueLocationUUIDs.addAll(getIdsFromReferenceList(((Encounter) resource).getLocation().stream()
					        .map(Encounter.EncounterLocationComponent::getLocation).collect(Collectors.toList())));
				});
				break;
		}
		
		uniqueLocationUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(locationService.get(uniqueLocationUUIDs));
	}
	
	protected Set<IBaseResource> handleMedicationInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueMedicationUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.MEDICATION_REQUEST:
				resourceList.stream()
				        .map(resource -> getIdFromReference(((MedicationRequest) resource).getMedicationReference()))
				        .forEach(uniqueMedicationUUIDs::add);
				break;
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.stream()
				        .map(resource -> getIdFromReference(((MedicationDispense) resource).getMedicationReference()))
				        .forEach(uniqueMedicationUUIDs::add);
				break;
		}
		
		uniqueMedicationUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(medicationService.get(uniqueMedicationUUIDs));
	}
	
	protected Set<IBaseResource> handleMedicationRequestInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueUuids = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.forEach(resource -> uniqueUuids
				        .addAll(getIdsFromReferenceList((((MedicationDispense) resource).getAuthorizingPrescription()))));
				break;
		}
		
		uniqueUuids.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(medicationRequestService.get(uniqueUuids));
	}
	
	protected Set<IBaseResource> handleObsGroupInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueObservationUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(resource -> uniqueObservationUUIDs
				        .addAll(getIdsFromReferenceList(((Observation) resource).getHasMember())));
				break;
			case FhirConstants.DIAGNOSTIC_REPORT:
				resourceList.forEach(resource -> uniqueObservationUUIDs
				        .addAll(getIdsFromReferenceList(((DiagnosticReport) resource).getResult())));
				break;
		}
		
		uniqueObservationUUIDs.removeIf(Objects::isNull);
		
		if (uniqueObservationUUIDs.isEmpty()) {
			return null;
		}
		
		return new LinkedHashSet<>(observationService.get(uniqueObservationUUIDs));
	}
	
	protected Set<IBaseResource> handlePatientInclude(List<U> resourceList, String paramType) {
		Set<String> uniquePatientUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((Observation) resource).getSubject())));
				break;
			case FhirConstants.ALLERGY_INTOLERANCE:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((AllergyIntolerance) resource).getPatient())));
				break;
			case FhirConstants.DIAGNOSTIC_REPORT:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((DiagnosticReport) resource).getSubject())));
				break;
			case FhirConstants.ENCOUNTER:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((Encounter) resource).getSubject())));
				break;
			case FhirConstants.MEDICATION_REQUEST:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((MedicationRequest) resource).getSubject())));
				break;
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((MedicationDispense) resource).getSubject())));
				break;
			case FhirConstants.PERSON:
				resourceList.forEach(resource -> {
					List<Reference> patientReferenceList = new ArrayList<>();
					((Person) resource).getLink().forEach(patient -> patientReferenceList.add(patient.getTarget()));
					uniquePatientUUIDs.addAll(getIdsFromReferenceList(patientReferenceList));
				});
				break;
			case FhirConstants.RELATED_PERSON:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((RelatedPerson) resource).getPatient())));
				break;
			case FhirConstants.PROCEDURE_REQUEST:
			case FhirConstants.SERVICE_REQUEST:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((ServiceRequest) resource).getSubject())));
				break;
			case FhirConstants.CONDITION:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((Condition) resource).getSubject())));
				break;
			case FhirConstants.TASK:
				resourceList.forEach(resource -> uniquePatientUUIDs.add(getIdFromReference(((Task) resource).getFor())));
				break;
		}
		
		uniquePatientUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(patientService.get(uniquePatientUUIDs));
	}
	
	protected Set<Location> handleParentLocationInclude(List<U> resourceList, String targetType) {
		if (targetType.equals(FhirConstants.LOCATION)) {
			Set<String> uniqueParentLocationUUIDs = resourceList.stream().filter(it -> it instanceof Location)
			        .map(it -> (Location) it).map(Location::getPartOf).map(this::getIdFromReference).filter(Objects::nonNull)
			        .collect(Collectors.toSet());
			return new LinkedHashSet<>(locationService.get(uniqueParentLocationUUIDs));
		}
		
		return Collections.emptySet();
	}
	
	protected Set<IBaseResource> handleParticipantInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueParticipantUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.ENCOUNTER:
				resourceList.forEach(resource -> {
					List<Reference> participantReferenceList = new ArrayList<>();
					((Encounter) resource).getParticipant()
					        .forEach(participant -> participantReferenceList.add(participant.getIndividual()));
					uniqueParticipantUUIDs.addAll(getIdsFromReferenceList(participantReferenceList));
				});
				break;
			case FhirConstants.MEDICATION_REQUEST:
				resourceList.forEach(resource -> uniqueParticipantUUIDs
				        .add(getIdFromReference(((MedicationRequest) resource).getRequester())));
				break;
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.forEach(resource -> {
					List<Reference> performerReferenceList = new ArrayList<>();
					((MedicationDispense) resource).getPerformer()
					        .forEach(performer -> performerReferenceList.add(performer.getActor()));
					uniqueParticipantUUIDs.addAll(getIdsFromReferenceList(performerReferenceList));
				});
				break;
			case FhirConstants.PROCEDURE_REQUEST:
			case FhirConstants.SERVICE_REQUEST:
				resourceList.forEach(
				    resource -> uniqueParticipantUUIDs.add(getIdFromReference(((ServiceRequest) resource).getRequester())));
				break;
		}
		
		uniqueParticipantUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(practitionerService.get(uniqueParticipantUUIDs));
	}
	
	protected Set<IBaseResource> handlePersonLinkInclude(List<U> resourceList, String targetType) {
		Set<IBaseResource> includedResources = new LinkedHashSet<>();
		
		switch (targetType) {
			case FhirConstants.PATIENT:
				resourceList.forEach(resource -> {
					List<Reference> patientReferenceList = new ArrayList<>();
					((Person) resource).getLink().stream()
					        .filter(res -> res.getTarget().getType().equals(FhirConstants.PATIENT))
					        .forEach(patient -> patientReferenceList.add(patient.getTarget()));
					
					includedResources
					        .addAll(patientService.get(new LinkedHashSet<>(getIdsFromReferenceList(patientReferenceList))));
				});
				break;
		}
		
		return includedResources;
	}
	
	protected Set<IBaseResource> handlePractitionerInclude(List<U> resourceList, String paramType) {
		Set<String> uniquePractitionerUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.TASK:
				resourceList
				        .forEach(resource -> uniquePractitionerUUIDs.add(getIdFromReference(((Task) resource).getOwner())));
				break;
		}
		
		uniquePractitionerUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(practitionerService.get(uniquePractitionerUUIDs));
	}
	
	protected Set<IBaseResource> handleServiceRequestInclude(List<U> resourceList, String paramType) {
		Set<String> uniqueServiceRequestUUIDs = new LinkedHashSet<>();
		
		switch (paramType) {
			case FhirConstants.TASK:
				resourceList.forEach(resource -> {
					uniqueServiceRequestUUIDs.addAll(getIdsFromReferenceList(((Task) resource).getBasedOn()));
				});
				break;
		}
		
		uniqueServiceRequestUUIDs.removeIf(Objects::isNull);
		
		return new LinkedHashSet<>(serviceRequestService.get(uniqueServiceRequestUUIDs));
	}
	
	protected IBundleProvider handleEncounterReverseInclude(ReferenceAndListParam params, String targetType,
	        Set<Include> recursiveIncludes, Set<Include> recursiveRevIncludes) {
		switch (targetType) {
			case FhirConstants.OBSERVATION:
				return observationService
				        .searchForObservations(ObservationSearchParams.builder().encounterReference(params).build());
			case FhirConstants.DIAGNOSTIC_REPORT:
				return diagnosticReportService.searchForDiagnosticReports(
				    DiagnosticReportSearchParams.builder().encounterReference(params).build());
			case FhirConstants.MEDICATION_REQUEST:
				return medicationRequestService.searchForMedicationRequests(MedicationRequestSearchParams.builder()
				        .encounterReference(params).includes(recursiveIncludes).revIncludes(recursiveRevIncludes).build());
			case FhirConstants.PROCEDURE_REQUEST:
			case FhirConstants.SERVICE_REQUEST:
				return serviceRequestService.searchForServiceRequests(null, null, params, null, null, null, null, null);
		}
		
		return null;
	}
	
	protected IBundleProvider handleLocationReverseInclude(ReferenceAndListParam params, String targetType) {
		switch (targetType) {
			case FhirConstants.LOCATION:
				LocationSearchParams locationSearchParams = new LocationSearchParams();
				locationSearchParams.setParent(params);
				return locationService.searchForLocations(locationSearchParams);
			case FhirConstants.ENCOUNTER:
				EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
				encounterSearchParams.setLocation(params);
				return encounterService.searchForEncounters(encounterSearchParams);
		}
		
		return null;
	}
	
	protected IBundleProvider handleMedicationReverseInclude(ReferenceAndListParam params, String targetType,
	        Set<Include> recursiveIncludes, Set<Include> recursiveRevIncludes) {
		switch (targetType) {
			case FhirConstants.MEDICATION_REQUEST:
				return medicationRequestService.searchForMedicationRequests(new MedicationRequestSearchParams(null, null,
				        null, null, params, null, null, null, null, recursiveIncludes, recursiveRevIncludes));
		}
		
		return null;
	}
	
	protected IBundleProvider handleObservationReverseInclude(ReferenceAndListParam params, String targetType) {
		switch (targetType) {
			case FhirConstants.OBSERVATION:
				ObservationSearchParams observationSearchParams = new ObservationSearchParams();
				observationSearchParams.setHasMember(params);
				return observationService.searchForObservations(observationSearchParams);
			case FhirConstants.DIAGNOSTIC_REPORT:
				return diagnosticReportService.searchForDiagnosticReports(
				    new DiagnosticReportSearchParams(null, null, null, null, params, null, null, null, null));
		}
		
		return null;
	}
	
	protected IBundleProvider handlePatientReverseInclude(ReferenceAndListParam params, String targetType,
	        Set<Include> recursiveIncludes, Set<Include> recursiveRevIncludes) {
		switch (targetType) {
			case FhirConstants.OBSERVATION:
				ObservationSearchParams observationSearchParams = new ObservationSearchParams();
				observationSearchParams.setPatient(params);
				return observationService.searchForObservations(observationSearchParams);
			case FhirConstants.DIAGNOSTIC_REPORT:
				return diagnosticReportService.searchForDiagnosticReports(
				    new DiagnosticReportSearchParams(null, params, null, null, null, null, null, null, null));
			case FhirConstants.ALLERGY_INTOLERANCE:
				return allergyIntoleranceService.searchForAllergies(
				    new FhirAllergyIntoleranceSearchParams(params, null, null, null, null, null, null, null, null, null));
			case FhirConstants.ENCOUNTER:
				EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
				encounterSearchParams.setSubject(params);
				return encounterService.searchForEncounters(encounterSearchParams);
			case FhirConstants.MEDICATION_REQUEST:
				return medicationRequestService.searchForMedicationRequests(new MedicationRequestSearchParams(params, null,
				        null, null, null, null, null, null, null, recursiveIncludes, recursiveRevIncludes));
			case FhirConstants.SERVICE_REQUEST:
			case FhirConstants.PROCEDURE_REQUEST:
				return serviceRequestService.searchForServiceRequests(params, null, null, null, null, null, null, null);
		}
		
		return null;
	}
	
	protected IBundleProvider handlePractitionerReverseInclude(ReferenceAndListParam params, String targetType,
	        Set<Include> recursiveIncludes, Set<Include> recursiveRevIncludes) {
		switch (targetType) {
			case FhirConstants.ENCOUNTER:
				EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
				encounterSearchParams.setParticipant(params);
				return encounterService.searchForEncounters(encounterSearchParams);
			case FhirConstants.MEDICATION_REQUEST:
				return medicationRequestService.searchForMedicationRequests(new MedicationRequestSearchParams(null, null,
				        null, params, null, null, null, null, null, recursiveIncludes, recursiveRevIncludes));
			case FhirConstants.PROCEDURE_REQUEST:
			case FhirConstants.SERVICE_REQUEST:
				return serviceRequestService.searchForServiceRequests(null, null, null, params, null, null, null, null);
		}
		
		return null;
	}
	
	protected Set<Include> getRecursiveIncludes(Set<Include> includeSet) {
		return includeSet.stream().filter(Include::isRecurse).collect(Collectors.toSet());
	}
	
	protected Set<String> getIdsFromReferenceList(List<Reference> referenceList) {
		Set<String> idList = new LinkedHashSet<>();
		
		if (referenceList != null) {
			referenceList.forEach(reference -> idList.add(getIdFromReference(reference)));
		}
		
		return idList;
	}
	
	protected String getIdFromReference(Reference reference) {
		return reference != null ? reference.getReferenceElement().getIdPart() : null;
	}
}
