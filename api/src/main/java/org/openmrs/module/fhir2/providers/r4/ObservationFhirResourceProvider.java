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
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("observationFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
@SuppressWarnings("unused")
public class ObservationFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirObservationService observationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Observation.class;
	}
	
	@Read
	public Observation getObservationById(@IdParam @Nonnull IdType id) {
		Observation observation = observationService.get(id.getIdPart());
		if (observation == null) {
			throw new ResourceNotFoundException("Could not find Observation with Id " + id.getIdPart());
		}
		return observation;
	}
	
	@History
	public List<Resource> getObservationHistoryById(@IdParam @Nonnull IdType id) {
		Observation observation = observationService.get(id.getIdPart());
		if (observation == null) {
			throw new ResourceNotFoundException("Could not find Observation with Id " + id.getIdPart());
		}
		return observation.getContained();
	}
	
	@Create
	public MethodOutcome createObservationResource(@ResourceParam Observation observation) {
		return FhirProviderUtils.buildCreate(observationService.create(observation));
	}
	
	@Delete
	public OperationOutcome deleteObservationResource(@IdParam @Nonnull IdType id) {
		Observation observation = observationService.delete(id.getIdPart());
		if (observation == null) {
			throw new ResourceNotFoundException("Could not find observation to delete with id" + id.getIdPart());
		}
		
		return FhirProviderUtils.buildDelete(observation);
	}
	
	@Search
	public IBundleProvider searchObservations(
	        @OptionalParam(name = Observation.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = Observation.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = Observation.SP_HAS_MEMBER, chainWhitelist = { "",
	                Observation.SP_CODE }, targetTypes = Observation.class) ReferenceAndListParam hasMemberReference,
	        @OptionalParam(name = Observation.SP_VALUE_CONCEPT) TokenAndListParam valueConcept,
	        @OptionalParam(name = Observation.SP_VALUE_DATE) DateRangeParam valueDateParam,
	        @OptionalParam(name = Observation.SP_VALUE_QUANTITY) QuantityAndListParam valueQuantityParam,
	        @OptionalParam(name = Observation.SP_VALUE_STRING) StringAndListParam valueStringParam,
	        @OptionalParam(name = Observation.SP_DATE) DateRangeParam date,
	        @OptionalParam(name = Observation.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = Observation.SP_CATEGORY) TokenAndListParam category,
	        @OptionalParam(name = Observation.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @OptionalParam(name = Observation.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientParam,
	        @IncludeParam(allow = { "Observation:" + Observation.SP_ENCOUNTER, "Observation:" + Observation.SP_PATIENT,
	                "Observation:" + Observation.SP_HAS_MEMBER }) HashSet<Include> includes,
	        @IncludeParam(reverse = true, allow = { "Observation:" + Observation.SP_HAS_MEMBER,
	                "DiagnosticReport:" + DiagnosticReport.SP_RESULT }) HashSet<Include> revIncludes) {
		if (patientParam != null) {
			patientReference = patientParam;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return observationService.searchForObservations(encounterReference, patientReference, hasMemberReference,
		    valueConcept, valueDateParam, valueQuantityParam, valueStringParam, date, code, category, id, lastUpdated, sort,
		    includes, revIncludes);
	}
}
