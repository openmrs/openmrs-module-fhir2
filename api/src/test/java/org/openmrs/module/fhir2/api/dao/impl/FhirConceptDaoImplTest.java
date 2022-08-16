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

import static co.unruly.matchers.OptionalMatchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Optional;

import co.unruly.matchers.OptionalMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConceptDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String CONCEPT_DATA_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	private static final String CONCEPT_FHIR_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConceptDaoImplTest_initial_data.xml";
	
	private static final String CONCEPT_UUID = "957eba27-2b38-43e8-91a9-4dfe3956a32d";
	
	private static final String MAPPED_CONCEPT_UUID = "378e63b1-6c75-46ed-95e3-797b48ddc9f8";
	
	private static final String MAPPED_CONCEPT_UUID2 = "bbbf56f8-706e-41ef-95cd-57271567223a";
	
	private static final String BAD_CONCEPT_UUID = "2c9570d4-649c-4395-836f-f2cfa1cd733f";
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService conceptService;
	
	@Autowired
	private FhirConceptDao dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(CONCEPT_DATA_XML);
		executeDataSet(CONCEPT_FHIR_DATA_XML);
	}
	
	@Test
	public void getConceptByUuid_shouldGetConceptByUuid() {
		Concept result = dao.get(CONCEPT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void getConceptByUuid_shouldReturnNullIfConceptUuidDoesNotExist() {
		Concept result = dao.get(BAD_CONCEPT_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldGetConceptBySourceAndMapping() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		Optional<Concept> result = dao.getConceptWithSameAsMappingInSource(loinc, "1000-1");
		
		assertThat(result, not(OptionalMatchers.empty()));
		assertThat(result, contains(hasProperty("uuid", equalTo(MAPPED_CONCEPT_UUID))));
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldReturnNullIfSourceIsNull() {
		Optional<Concept> result = dao.getConceptWithSameAsMappingInSource(null, "1000-1");
		
		assertThat(result, OptionalMatchers.empty());
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldReturnNullIfMappingDoesNotExistInSource() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		Optional<Concept> result = dao.getConceptWithSameAsMappingInSource(loinc, "not-exists");
		
		assertThat(result, OptionalMatchers.empty());
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldReturnNullIfMappingIsNull() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		Optional<Concept> result = dao.getConceptWithSameAsMappingInSource(loinc, null);
		
		assertThat(result, OptionalMatchers.empty());
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldReturnNullIfMappingIsNotSameAs() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		Optional<Concept> result = dao.getConceptWithSameAsMappingInSource(loinc, "not-same-as");
		
		assertThat(result, OptionalMatchers.empty());
	}
	
	@Test
	public void getConceptsWithAnyMappingInSource_shouldGetListOfConceptsBySourceAndMapping() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		List<Concept> results = dao.getConceptsWithAnyMappingInSource(loinc, "1000-1");
		
		assertThat(results, not(empty()));
		assertThat(results, hasSize(2));
		assertThat(results.get(0).getUuid(), equalTo(MAPPED_CONCEPT_UUID));
		assertThat(results.get(1).getUuid(), equalTo(MAPPED_CONCEPT_UUID2));
	}
	
	@Test
	public void getConceptsWithAnyMappingInSource_shouldGetListOfConceptsEvenIfMappingIsNotSameAs() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		List<Concept> results = dao.getConceptsWithAnyMappingInSource(loinc, "not-same-as");
		
		assertThat(results, not(empty()));
		assertThat(results, hasSize(1));
		assertThat(results.get(0).getUuid(), equalTo(MAPPED_CONCEPT_UUID));
	}
	
	@Test
	public void getConceptsWithAnyMappingInSource_shouldReturnEmptyListIfIfSourceIsNull() {
		ConceptSource loinc = conceptService.getConceptSourceByName(null);
		
		List<Concept> results = dao.getConceptsWithAnyMappingInSource(loinc, "1000-1");
		
		assertThat(results, empty());
	}
	
	@Test
	public void getConceptsWithAnyMappingInSource_shouldReturnEmptyListIfMappingDoesNotExist() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		List<Concept> results = dao.getConceptsWithAnyMappingInSource(loinc, "non-existing");
		
		assertThat(results, empty());
	}
	
	@Test
	public void getConceptsWithAnyMappingInSource_shouldReturnEmptyListIfMappingIsNull() {
		ConceptSource loinc = conceptService.getConceptSourceByName("LOINC");
		
		List<Concept> results = dao.getConceptsWithAnyMappingInSource(loinc, null);
		
		assertThat(results, empty());
	}
}
