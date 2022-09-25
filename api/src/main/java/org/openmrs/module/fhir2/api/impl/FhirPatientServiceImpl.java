/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirPatientServiceImpl extends BaseFhirService<Patient, org.openmrs.Patient> implements FhirPatientService {
	
	@Autowired
	private PatientTranslator translator;
	
	@Autowired
	private FhirPatientDao dao;
	
	@Autowired
	private SearchQueryInclude<Patient> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Patient, Patient, FhirPatientDao, PatientTranslator, SearchQueryInclude<Patient>> searchQuery;
	
	@Override
	public List<Patient> getPatientsByIds(@Nonnull Collection<Integer> ids) {
		List<org.openmrs.Patient> patients = dao.getPatientsByIds(ids);
		return patients.stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Patient getById(@Nonnull Integer id) {
		return translator.toFhirResource(dao.getPatientById(id));
	}
	
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierTypeByIdentifier(Identifier identifier) {
		if (identifier.getType() == null || StringUtils.isBlank(identifier.getType().getText())) {
			return null;
		}
		
		return dao.getPatientIdentifierTypeByNameOrUuid(identifier.getType().getText(), null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForPatients(PatientSearchParams patientSearchParams) {
		return searchQuery.getQueryResults(patientSearchParams.toSearchParameterMap(), dao, translator, searchQueryInclude);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getPatientEverything(TokenParam patientId) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
		            new TokenAndListParam().addAnd(patientId));
		
		populateEverythingOperationParams(theParams);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getPatientEverything() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "");
		
		populateEverythingOperationParams(theParams);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private void populateEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.ALLERGY_INTOLERANCE + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.PROCEDURE_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
}
