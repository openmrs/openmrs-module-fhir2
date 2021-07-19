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
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
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
	public IBundleProvider searchForPatients(StringAndListParam name, StringAndListParam given, StringAndListParam family,
	        TokenAndListParam identifier, TokenAndListParam gender, DateRangeParam birthDate, DateRangeParam deathDate,
	        TokenAndListParam deceased, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	        StringAndListParam country, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	        HashSet<Include> revIncludes) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.NAME_PROPERTY, name)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, given)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, family)
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, identifier)
		        .addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender", gender)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "birthdate", birthDate)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "deathDate", deathDate)
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, deceased)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, city)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, state)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, postalCode)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, country)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes).setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getPatientEverything(TokenAndListParam patientId) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_EVERYTHING_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, patientId);
		
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include("Observation:" + Observation.SP_PATIENT));
		revIncludes.add(new Include("AllergyIntolerance:" + AllergyIntolerance.SP_PATIENT));
		revIncludes.add(new Include("DiagnosticReport:" + DiagnosticReport.SP_PATIENT));
		revIncludes.add(new Include("Encounter:" + Encounter.SP_PATIENT));
		revIncludes.add(new Include("MedicationRequest:" + MedicationRequest.SP_PATIENT));
		revIncludes.add(new Include("ServiceRequest:" + ServiceRequest.SP_PATIENT));
		revIncludes.add(new Include("ProcedureRequest:" + Procedure.SP_PATIENT));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
