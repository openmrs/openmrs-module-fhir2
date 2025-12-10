/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("encounterFhirR4ResourceProvider")
@R4Provider
public class EncounterFhirResourceProvider implements IResourceProvider {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirEncounterService encounterService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}
	
	@Read
	public Encounter getEncounterByUuid(@IdParam @Nonnull IdType id) {
		Encounter encounter = encounterService.get(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter with Id " + id.getIdPart());
		}
		return encounter;
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createEncounter(@ResourceParam Encounter encounter) {
		return FhirProviderUtils.buildCreate(encounterService.create(encounter));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateEncounter(@IdParam IdType id, @ResourceParam Encounter encounter) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		return FhirProviderUtils.buildUpdate(encounterService.update(id.getIdPart(), encounter));
	}
	
	@Patch
	@SuppressWarnings("unused")
	public MethodOutcome patchEncounter(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update Encounter resource");
		}
		
		Encounter encounter = encounterService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(encounter);
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteEncounter(@IdParam @Nonnull IdType id) {
		encounterService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	public IBundleProvider searchEncounter(@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date,
	        @OptionalParam(name = Encounter.SP_LOCATION, chainWhitelist = { "", Location.SP_ADDRESS_CITY,
	                Location.SP_ADDRESS_STATE, Location.SP_ADDRESS_COUNTRY,
	                Location.SP_ADDRESS_POSTALCODE }, targetTypes = Location.class) ReferenceAndListParam location,
	        @OptionalParam(name = Encounter.SP_PARTICIPANT, chainWhitelist = { "", Practitioner.SP_IDENTIFIER,
	                Practitioner.SP_GIVEN, Practitioner.SP_FAMILY,
	                Practitioner.SP_NAME }, targetTypes = Practitioner.class) ReferenceAndListParam participantReference,
	        @OptionalParam(name = Encounter.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = Encounter.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientParam,
	        @OptionalParam(name = Encounter.SP_TYPE) TokenAndListParam encounterType,
	        @OptionalParam(name = Encounter.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_tag") TokenAndListParam tag,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @OptionalParam(name = FhirConstants.HAS_SEARCH_HANDLER) HasAndListParam hasAndListParam,
	        @IncludeParam(allow = { "Encounter:" + Encounter.SP_LOCATION, "Encounter:" + Encounter.SP_PATIENT,
	                "Encounter:" + Encounter.SP_PARTICIPANT }) HashSet<Include> includes,
	        @IncludeParam(reverse = true, allow = { "Observation:" + Observation.SP_ENCOUNTER,
	                "DiagnosticReport:" + DiagnosticReport.SP_ENCOUNTER,
	                "MedicationRequest:" + MedicationRequest.SP_ENCOUNTER, "ServiceRequest:" + ServiceRequest.SP_ENCOUNTER,
	                "MedicationDispense:" + MedicationDispense.SP_PRESCRIPTION }) HashSet<Include> revIncludes) {
		if (patientParam != null) {
			subjectReference = patientParam;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return encounterService.searchForEncounters(new EncounterSearchParams(date, location, participantReference,
		        subjectReference, encounterType, tag, hasAndListParam, id, lastUpdated, sort, includes, revIncludes));
	}
	
	/**
	 * The $everything operation fetches all the information related the specified encounter
	 *
	 * @param encounterId The id of the encounter
	 * @return a bundle of resources which reference to or are referenced from the encounter
	 */
	@Operation(name = "everything", idempotent = true, type = Encounter.class, bundleType = BundleTypeEnum.SEARCHSET)
	public IBundleProvider getEncounterEverything(@IdParam IdType encounterId) {
		
		if (encounterId == null || encounterId.getIdPart() == null || encounterId.getIdPart().isEmpty()) {
			return null;
		}
		
		TokenParam encounterReference = new TokenParam().setValue(encounterId.getIdPart());
		
		return encounterService.getEncounterEverything(encounterReference);
	}
	
	/**
	 * Custom search endpoint that fetches encounters that include medication requests. NOTE: This query
	 * has been designed to provide the backend query functionality needed by the Dispensing ESM, and so
	 * the exact contract on what is returned may be modified as that ESM evolves. NOTE: requires Core
	 * 2.6.1 or higher (with new "declined" fulfiller status)
	 *
	 * @param date restrict by encounter date
	 * @param status if set to active, when determined encounters to include, exclude encounters that
	 *            *only* have completed, cancelled or declined medication requests
	 * @param patientSearchTerm restrict to encounters for patients whose name or identifier matches the
	 *            search term
	 * @param location restrict to encounters at certain location(s)
	 * @param type restrict to encounters of certain (encounter) type(s)
	 * @return bundle that includes the medication requests and any medication dispenses that reference
	 *         those requests
	 */
	@Search(queryName = "encountersWithMedicationRequests")
	public IBundleProvider getEncountersWithMedicationRequestsSearch(
	        @OptionalParam(name = Encounter.SP_DATE) DateRangeParam date, @OptionalParam(name = "status") TokenParam status,
	        @OptionalParam(name = Encounter.SP_TYPE) TokenAndListParam type,
	        @OptionalParam(name = "patientSearchTerm") TokenParam patientSearchTerm,
	        @OptionalParam(name = "location") ReferenceAndListParam location) {
		
		EncounterSearchParams params = new EncounterSearchParams();
		
		// we always want encounters, not visits
		params.setTag(new TokenAndListParam()
		        .addAnd(new TokenParam().setSystem(OPENMRS_FHIR_EXT_ENCOUNTER_TAG).setValue("encounter")));
		
		if (date != null && !date.isEmpty()) {
			// encounter must be equal to or after the configured (expire) date
			params.setDate(date);
		}
		
		if (status != null && !status.isEmpty() && status.getValue().equalsIgnoreCase("active")) {
			// encounter must have a medication request that is neither completed nor cancelled nor declined (expired is okay, since we are using another definition of expired for dispensing purposes)
			HasOrListParam notCompletedHasParam = new HasOrListParam()
			        .add(new HasParam("MedicationRequest", "encounter", "fulfillerStatus:not", "completed"));
			HasOrListParam notDeclinedHasParam = new HasOrListParam()
			        .add(new HasParam("MedicationRequest", "encounter", "fulfillerStatus:not", "declined"));
			HasOrListParam notCancelledHasParam = new HasOrListParam()
			        .add(new HasParam("MedicationRequest", "encounter", "status:not", "cancelled"));
			params.setHasAndListParam(
			    new HasAndListParam().addAnd(notCancelledHasParam).addAnd(notDeclinedHasParam).addAnd(notCompletedHasParam));
		} else {
			// for "all" query only restriction is that the encounter has at least one medication request
			params.setHasAndListParam(new HasAndListParam()
			        .addAnd(new HasOrListParam().add(new HasParam("MedicationRequest", "encounter", "intent", "order"))));
		}
		
		// sort by date descending, so most recent is first
		SortSpec sortSpec = new SortSpec();
		sortSpec.setParamName("date");
		sortSpec.setOrder(SortOrderEnum.DESC);
		params.setSort(sortSpec);
		
		// search on identifier or patient name if that value has been passed in
		if (patientSearchTerm != null && !patientSearchTerm.isEmpty()) {
			ReferenceOrListParam subjectReference = new ReferenceOrListParam();
			subjectReference.add(new ReferenceParam("name", patientSearchTerm.getValue()));
			subjectReference.add(new ReferenceParam("identifier", patientSearchTerm.getValue()));
			params.setSubject(new ReferenceAndListParam().addAnd(subjectReference));
		}
		
		// search by location if specified
		// all this convoluted testing is a null-safe way to make sure if "location=" is passed in, it is ignored
		if (location != null && !location.getValuesAsQueryTokens().isEmpty()
		        && !location.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().isEmpty()
		        && !location.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue().isEmpty()) {
			params.setLocation(location);
		}
		
		// search by encounter type, if specified
		// all this convoluted testing is a null-safe way to make sure if "type=" is passed in, it is ignored
		if (type != null && !type.getValuesAsQueryTokens().isEmpty()
		        && !type.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().isEmpty()
		        && !type.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue().isEmpty()) {
			params.setEncounterType(type);
		}
		
		// include all medication requests associated with the encounter, and then all dispenses associated with those requests
		HashSet<Include> revIncludes = new HashSet<>();
		Include medicationRequestInclude = new Include("MedicationRequest:encounter", false);
		Include medicationDispenseInclude = new Include("MedicationDispense:prescription", true);
		revIncludes.add(medicationRequestInclude);
		revIncludes.add(medicationDispenseInclude);
		params.setRevIncludes(revIncludes);
		
		return encounterService.searchForEncounters(params);
	}
}
