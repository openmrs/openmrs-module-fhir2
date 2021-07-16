/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static lombok.AccessLevel.PACKAGE;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.convertors.conv30_40.Encounter30_40;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("encounterFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class EncounterFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirEncounterService encounterService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Encounter getEncounterById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.get(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter with Id " + id.getIdPart());
		}
		
		return Encounter30_40.convertEncounter(encounter);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createEncounter(@ResourceParam Encounter encounter) {
		return FhirProviderUtils.buildCreate(
		    Encounter30_40.convertEncounter(encounterService.create(Encounter30_40.convertEncounter(encounter))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateEncounter(@IdParam IdType id, @ResourceParam Encounter encounter) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		encounter.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(Encounter30_40
		        .convertEncounter(encounterService.update(id.getIdPart(), Encounter30_40.convertEncounter(encounter))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteEncounter(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.delete(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(Encounter30_40.convertEncounter(encounter));
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getEncounterHistoryById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.get(id.getIdPart());
		if (encounter == null) {
			throw new ResourceNotFoundException("Could not find encounter with Id " + id.getIdPart());
		}
		return Encounter30_40.convertEncounter(encounter).getContained();
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
	        @OptionalParam(name = Encounter.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(allow = { "Encounter:" + Encounter.SP_LOCATION, "Encounter:" + Encounter.SP_PATIENT,
	                "Encounter:" + Encounter.SP_PARTICIPANT }) HashSet<Include> includes,
	        @IncludeParam(reverse = true, allow = { "Observation:" + Observation.SP_ENCOUNTER,
	                "DiagnosticReport:" + DiagnosticReport.SP_ENCOUNTER, "MedicationRequest:" + MedicationRequest.SP_CONTEXT,
	                "ProcedureRequest:" + ProcedureRequest.SP_ENCOUNTER }) HashSet<Include> revIncludes) {
		if (patientParam != null) {
			subjectReference = patientParam;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return new SearchQueryBundleProviderR3Wrapper(encounterService.searchForEncounters(date, location,
		    participantReference, subjectReference, id, lastUpdated, includes, revIncludes));
	}
	
}
