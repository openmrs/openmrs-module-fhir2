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
import org.hl7.fhir.convertors.conv30_40.AllergyIntolerance30_40;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("allergyIntoleranceFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class AllergyIntoleranceFhirResourceProvider implements IResourceProvider {
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AllergyIntolerance.class;
	}
	
	@Autowired
	private FhirAllergyIntoleranceService allergyIntoleranceService;
	
	@Read
	@SuppressWarnings("unused")
	public AllergyIntolerance getAllergyIntoleranceById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.AllergyIntolerance allergyIntolerance = allergyIntoleranceService.get(id.getIdPart());
		if (allergyIntolerance == null) {
			throw new ResourceNotFoundException("Could not find allergyIntolerance with Id " + id.getIdPart());
		}
		
		return AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome creatAllergyIntolerance(@ResourceParam AllergyIntolerance allergyIntolerance) {
		return FhirProviderUtils.buildCreate(AllergyIntolerance30_40.convertAllergyIntolerance(
		    allergyIntoleranceService.create(AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateAllergyIntolerance(@IdParam IdType id, @ResourceParam AllergyIntolerance allergyIntolerance) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		allergyIntolerance.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntoleranceService
		        .update(id.getIdPart(), AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteAllergyIntolerance(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.AllergyIntolerance allergyIntolerance = allergyIntoleranceService.delete(id.getIdPart());
		if (allergyIntolerance == null) {
			throw new ResourceNotFoundException("Could not find allergyIntolerance to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance));
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
		
		return new SearchQueryBundleProviderR3Wrapper(allergyIntoleranceService.searchForAllergies(patientReference,
		    category, allergen, severity, manifestationCode, clinicalStatus, id, lastUpdated, sort, includes));
	}
}
