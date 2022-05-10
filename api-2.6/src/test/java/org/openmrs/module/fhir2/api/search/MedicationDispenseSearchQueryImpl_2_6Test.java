/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.MedicationDispense;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class MedicationDispenseSearchQueryImpl_2_6Test extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private FhirMedicationDispenseDao<MedicationDispense> dao;
	
	@Autowired
	private MedicationDispenseTranslator<MedicationDispense> translator;
	
	@Autowired
	private SearchQueryInclude_2_6 searchQueryInclude;
	
	@Autowired
	private SearchQuery<MedicationDispense, org.hl7.fhir.r4.model.MedicationDispense, FhirMedicationDispenseDao<MedicationDispense>, MedicationDispenseTranslator<MedicationDispense>, SearchQueryInclude<org.hl7.fhir.r4.model.MedicationDispense>> searchQuery;
	
	@Autowired
	private LocalDateTimeFactory localDateTimeFactory;
	
	@Autowired
	PatientService patientService;
	
	private Patient patient2;
	
	private Patient patient7;
	
	private SearchParameterMap theParams;
	
	@Before
	public void setup() {
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		updateSearchIndex();
		theParams = new SearchParameterMap();
		patient2 = patientService.getPatient(2);
		patient7 = patientService.getPatient(7);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
	
	private org.hl7.fhir.r4.model.MedicationDispense firstResult(List<IBaseResource> resultList) {
		if (resultList == null || resultList.isEmpty()) {
			return null;
		}
		return (org.hl7.fhir.r4.model.MedicationDispense) resultList.iterator().next();
	}
	
	@Test
	public void shouldReturnMedicationDispensesByPatientUuid() {
		
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addAnd(new ReferenceOrListParam().addOr(new ReferenceParam(patient7.getUuid())));
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, param);
		
		IBundleProvider results = search(theParams);
		List<IBaseResource> resultList = get(results);
		org.hl7.fhir.r4.model.MedicationDispense firstResult = firstResult(resultList);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(firstResult.getSubject().getReference(), endsWith(patient7.getUuid()));
	}
	
	@Test
	public void shouldReturnMedicationDispensesByPatientIdentifier() {
		
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addAnd(new ReferenceOrListParam()
		        .addOr(new ReferenceParam(SP_IDENTIFIER, patient7.getIdentifiers().iterator().next().getIdentifier())));
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, param);
		
		IBundleProvider results = search(theParams);
		List<IBaseResource> resultList = get(results);
		org.hl7.fhir.r4.model.MedicationDispense firstResult = firstResult(resultList);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(firstResult.getSubject().getReference(), endsWith(patient7.getUuid()));
	}
	
	@Test
	public void shouldReturnMedicationDispensesByPatientIdentifierWithOrMatches() {
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addAnd(new ReferenceOrListParam()
		        .addOr(new ReferenceParam(SP_IDENTIFIER, patient2.getIdentifiers().iterator().next().getIdentifier()))
		        .addOr(new ReferenceParam(SP_IDENTIFIER, patient7.getIdentifiers().iterator().next().getIdentifier())));
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, param);
		
		IBundleProvider results = search(theParams);
		List<IBaseResource> resultList = get(results);
		org.hl7.fhir.r4.model.MedicationDispense firstResult = firstResult(resultList);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(3));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(firstResult.getSubject().getReference(),
		    anyOf(endsWith(patient2.getUuid()), endsWith(patient7.getUuid())));
	}
}
