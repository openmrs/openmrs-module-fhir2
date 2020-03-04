/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class CustomizableMetadataTranslatorImplTest {
	
	private static final String USER_UUID = "ddc25312-9798-4e6c-b8f8-269f2dd07cfd";
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String LOCATION_DESCRIPTION = "Test description";
	
	private static final String AGENT_TYPE_CODE = "author";
	
	private static final String AGENT_TYPE_DISPLAY = "Author";
	
	private static final String AGENT_ROLE_CODE = "AUT";
	
	private static final String AGENT_ROLE_DISPLAY = "author";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	private CustomizableMetadataTranslatorImpl<LocationAttribute, Location> customizableMetadataTranslator;
	
	private Location location;
	
	private User user;
	
	@Before
	public void setup() {
		customizableMetadataTranslator = new CustomizableMetadataTranslatorImpl<>();
		customizableMetadataTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		
		user = new User();
		user.setUuid(USER_UUID);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setDescription(LOCATION_DESCRIPTION);
		location.setDateCreated(new Date());
		location.setDateChanged(new Date());
		location.setCreator(user);
		location.setChangedBy(user);
	}
	
	@Test
	public void shouldCreateProvenance() {
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldCreateProvenanceWithCorrectActivity() {
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo("CREATE"));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo("create"));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldCreateProvenanceWithCorrectDateChanged() {
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldCreateProvenanceWithCorrectPractitionerReference() {
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getWho(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getWho(), equalTo(practitionerRef));
	}
	
	@Test
	public void shouldCreateProvenanceWithCorrectAgentRole() {
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getDisplay(),
		    equalTo(AGENT_ROLE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
	
	@Test
	public void shouldCreateProvenanceWithCorrectAgentType() {
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getType(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getCode(), equalTo(AGENT_TYPE_CODE));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getDisplay(), equalTo(AGENT_TYPE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE));
	}
	
	@Test
	public void shouldUpdateProvenance() {
		Provenance provenance = customizableMetadataTranslator.getUpdateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldUpdateProvenanceWithCorrectActivity() {
		Provenance provenance = customizableMetadataTranslator.getUpdateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo("UPDATE"));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo("revise"));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldUpdateProvenanceWithCorrectDateChanged() {
		Provenance provenance = customizableMetadataTranslator.getUpdateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldUpdateProvenanceWithCorrectPractitionerReference() {
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		Provenance provenance = customizableMetadataTranslator.getCreateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getWho(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getWho(), equalTo(practitionerRef));
	}
	
	@Test
	public void shouldUpdateProvenanceWithCorrectAgentRole() {
		Provenance provenance = customizableMetadataTranslator.getUpdateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getDisplay(),
		    equalTo(AGENT_ROLE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
	
	@Test
	public void shouldUpdateProvenanceWithCorrectAgentType() {
		Provenance provenance = customizableMetadataTranslator.getUpdateProvenance(location);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getType(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getCode(), equalTo(AGENT_TYPE_CODE));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getDisplay(), equalTo(AGENT_TYPE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE));
	}
	
}
