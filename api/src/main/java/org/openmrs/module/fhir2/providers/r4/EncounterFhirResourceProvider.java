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

import static lombok.AccessLevel.PACKAGE;

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
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
@Setter(PACKAGE)
public class EncounterFhirResourceProvider implements IResourceProvider {
	
	@Autowired
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
		        subjectReference, encounterType, id, lastUpdated, tag, hasAndListParam, sort, includes, revIncludes));
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
}
