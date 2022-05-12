/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConceptSourceDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String CONCEPT_SOURCE_FHIR_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirConceptSourceDaoImplTest_initial_data.xml";
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ConceptService conceptService;
	
	private FhirConceptSourceDaoImpl fhirConceptSourceDao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(CONCEPT_SOURCE_FHIR_DATA);
		
		fhirConceptSourceDao = new FhirConceptSourceDaoImpl();
		fhirConceptSourceDao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void getFhirConceptSources_shouldReturnAllSources() {
		Collection<FhirConceptSource> result = fhirConceptSourceDao.getFhirConceptSources();
		
		assertThat(result, notNullValue());
		assertThat(result, not(empty()));
		assertThat(result, hasItem(hasProperty("name", equalTo("LOINC"))));
		assertThat(result, hasItem(hasProperty("name", equalTo("CIEL"))));
	}
	
	@Test
	public void getFhirConceptSourceByUrl_shouldReturnSourceForUrl() {
		Optional<FhirConceptSource> result = fhirConceptSourceDao
		        .getFhirConceptSourceByUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		
		assertThat(result.isPresent(), is(true));
		assertThat(result.get().getUrl(), equalTo(FhirTestConstants.LOINC_SYSTEM_URL));
	}
	
	@Test
	public void getFhirConceptSourceByUrl_shouldReturnEmptyOptionalForMissingUrl() {
		Optional<FhirConceptSource> result = fhirConceptSourceDao.getFhirConceptSourceByUrl("https://www.example.com");
		
		assertThat(result.isPresent(), is(false));
	}
	
	@Test
	public void getFhirConceptSourceByConceptSource_shouldReturnSourceWherePresent() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("LOINC");
		assertThat(conceptSource, notNullValue());
		assertThat(conceptSource.getUuid(), equalTo("2b3c1ff8-768a-102f-83f4-12313b04a615"));
		Optional<FhirConceptSource> result = fhirConceptSourceDao.getFhirConceptSourceByConceptSource(conceptSource);
		assertThat(result.isPresent(), is(true));
		assertThat(result.get().getUrl(), equalTo(FhirTestConstants.LOINC_SYSTEM_URL));
	}
	
	@Test
	public void getFhirConceptSourceByConceptSource_shouldReturnEmptyOptionalWhereNoFhirConceptSourceExists() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("SNOMED CT");
		assertThat(conceptSource, notNullValue());
		Optional<FhirConceptSource> result = fhirConceptSourceDao.getFhirConceptSourceByConceptSource(conceptSource);
		assertThat(result.isPresent(), is(false));
	}
	
	@Test
	public void getConceptSourceByHl7Code_shouldReturnSourceForHl7Code() {
		ConceptSource result = fhirConceptSourceDao.getConceptSourceByHl7Code("SCT");
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("SNOMED CT"));
	}
	
	@Test
	public void getConceptSourceByHl7Code_shouldReturnNonRetiredOverRetiredSourceForHl7Code() {
		ConceptSource result = fhirConceptSourceDao.getConceptSourceByHl7Code("I10");
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("ICD-10"));
		assertThat(result.getUuid(), equalTo("75f5b378-5065-11de-80cb-001e378eb67e"));
		assertThat(result.getRetired(), equalTo(false));
	}
	
	@Test
	public void getFhirConceptSourceByHl7Code_shouldReturnNullForMissingSourceName() {
		ConceptSource result = fhirConceptSourceDao.getConceptSourceByHl7Code("SNOMED CT");
		assertThat(result, nullValue());
	}
}
