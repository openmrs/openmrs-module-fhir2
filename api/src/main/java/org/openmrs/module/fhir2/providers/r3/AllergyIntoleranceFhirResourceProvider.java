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

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.AllergyIntolerance30_40;
import org.hl7.fhir.convertors.conv30_40.Provenance30_40;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Provenance;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("allergyIntoleranceFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class AllergyIntoleranceFhirResourceProvider implements IResourceProvider {
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AllergyIntolerance.class;
	}
	
	@Autowired
	private FhirAllergyIntoleranceService allergyIntoleranceService;
	
	@Read
	@SuppressWarnings("unused")
	public AllergyIntolerance getAllergyIntoleranceById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.AllergyIntolerance allergyIntolerance = allergyIntoleranceService.get(id.getIdPart());
		if (allergyIntolerance == null) {
			throw new ResourceNotFoundException("Could not find allergyIntolerance with Id " + id.getIdPart());
		}
		
		return AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance);
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getAllergyIntoleranceHistoryById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.AllergyIntolerance allergyIntolerance = allergyIntoleranceService.get(id.getIdPart());
		if (allergyIntolerance == null) {
			throw new ResourceNotFoundException("Could not find allergy with Id " + id.getIdPart());
		}
		
		return allergyIntolerance.getContained().stream().filter(r -> r instanceof Provenance).map(r -> (Provenance) r)
		        .map(Provenance30_40::convertProvenance).collect(Collectors.toList());
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForAllergies(
	        @OptionalParam(name = AllergyIntolerance.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = Observation.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = AllergyIntolerance.SP_CATEGORY) TokenAndListParam category,
	        @OptionalParam(name = AllergyIntolerance.SP_CODE) TokenAndListParam allergen,
	        @OptionalParam(name = AllergyIntolerance.SP_SEVERITY) TokenAndListParam severity,
	        @OptionalParam(name = AllergyIntolerance.SP_MANIFESTATION) TokenAndListParam manifestationCode,
	        @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenAndListParam clinicalStatus, @Sort SortSpec sort) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		return allergyIntoleranceService.searchForAllergies(patientReference, category, allergen, severity,
		    manifestationCode, clinicalStatus, sort);
	}
}
