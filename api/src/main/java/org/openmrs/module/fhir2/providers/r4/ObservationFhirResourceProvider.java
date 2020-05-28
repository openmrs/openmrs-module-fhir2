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

import javax.validation.constraints.NotNull;

import java.util.List;

import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("observationFhirR4ResourceProvider")
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
@SuppressWarnings("unused")
public class ObservationFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirObservationService observationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Observation.class;
	}
	
	@Read
	public Observation getObservationById(@IdParam @NotNull IdType id) {
		Observation observation = observationService.get(id.getIdPart());
		if (observation == null) {
			throw new ResourceNotFoundException("Could not find Observation with Id " + id.getIdPart());
		}
		return observation;
	}
	
	@History
	public List<Resource> getObservationHistoryById(@IdParam @NotNull IdType id) {
		Observation observation = observationService.get(id.getIdPart());
		if (observation == null) {
			throw new ResourceNotFoundException("Could not find Observation with Id " + id.getIdPart());
		}
		return observation.getContained();
	}
	
	@Search
	public IBundleProvider searchObservations(
	        @OptionalParam(name = Observation.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = Observation.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = Observation.SP_HAS_MEMBER, chainWhitelist = { "",
	                Observation.SP_CODE }, targetTypes = Observation.class) ReferenceParam hasMemberReference,
	        @OptionalParam(name = Observation.SP_VALUE_CONCEPT) TokenAndListParam valueConcept,
	        @OptionalParam(name = Observation.SP_VALUE_DATE) DateRangeParam valueDateParam,
	        @OptionalParam(name = Observation.SP_VALUE_QUANTITY) QuantityAndListParam valueQuantityParam,
	        @OptionalParam(name = Observation.SP_VALUE_STRING) StringAndListParam valueStringParam,
	        @OptionalParam(name = Observation.SP_DATE) DateRangeParam date,
	        @OptionalParam(name = Observation.SP_CODE) TokenAndListParam code,
			@OptionalParam(name = Observation.SP_CATEGORY) TokenAndListParam category, @Sort SortSpec sort,
			@OptionalParam(name = Observation.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientParam) {
						if(patientParam != null){
							patientReference = patientParam;
						}
		return observationService.searchForObservations(encounterReference, patientReference, hasMemberReference,
		    valueConcept, valueDateParam, valueQuantityParam, valueStringParam, date, code, category, sort);
	}
}
