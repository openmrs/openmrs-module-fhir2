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
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
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
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("allergyIntoleranceFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class AllergyIntoleranceFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirAllergyIntoleranceService fhirAllergyIntoleranceService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AllergyIntolerance.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public AllergyIntolerance getAllergyIntoleranceByUuid(@IdParam @Nonnull IdType id) {
		AllergyIntolerance allergy = fhirAllergyIntoleranceService.get(id.getIdPart());
		if (allergy == null) {
			throw new ResourceNotFoundException("Could not find allergy with Id " + id.getIdPart());
		}
		return allergy;
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
	        @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenAndListParam clinicalStatus,
	        @OptionalParam(name = AllergyIntolerance.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @IncludeParam(allow = { "AllergyIntolerance:" + AllergyIntolerance.SP_PATIENT }) HashSet<Include> includes) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return fhirAllergyIntoleranceService.searchForAllergies(patientReference, category, allergen, severity,
		    manifestationCode, clinicalStatus, id, lastUpdated, sort, includes);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createAllergy(@ResourceParam AllergyIntolerance allergy) {
		return FhirProviderUtils.buildCreate(fhirAllergyIntoleranceService.create(allergy));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateAllergy(@IdParam IdType id, @ResourceParam AllergyIntolerance allergy) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		allergy.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(fhirAllergyIntoleranceService.update(id.getIdPart(), allergy));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteAllergy(@IdParam @Nonnull IdType id) {
		AllergyIntolerance allergyIntolerance = fhirAllergyIntoleranceService.delete(id.getIdPart());
		if (allergyIntolerance == null) {
			throw new ResourceNotFoundException("Could not find allergy to delete with id " + id.getIdPart());
		}
		
		return FhirProviderUtils.buildDelete(allergyIntolerance);
	}
}
