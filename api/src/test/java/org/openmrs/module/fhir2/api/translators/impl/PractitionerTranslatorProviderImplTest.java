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

import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerTranslatorProviderImplTest {
	
	private static final String PROVIDER_UUID = "328934-34ni23-23j34-23923";
	
	private static final String GENDER = "M";
	
	private static final String GIVEN_NAME = "kipchumba";
	
	private static final String FAMILY_NAME = "vannessa";
	
	private static final String CITY_VILLAGE = "chemasta";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String PRACTITIONER_UUID = "934j934-34923n-23923n-2321";
	
	private static final String PRACTITIONER_IDENTIFIER = "practitioner-identifier";
	
	private static final String PERSON_ATTRIBUTE_UUID = "TY78UI-HJ89XX67-XX56XX-KL78S67D";
	
	private static final String PERSON_ATTRIBUTE_VALUE = "+254723723456";
	
	private static final String PERSON_ATTRIBUTE_TYPE_NAME = "PHONE";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "FF89DD99-OOX78-KKG89D-XX89CC8";
	
	private static final String ADDRESS_UUID = "135791-xxxxxx-135791";
	
	private static final String ADDRESS_CITY = "Eldoret";
	
	@Mock
	private GenderTranslator genderTranslator;
	
	@Mock
	private PersonNameTranslator nameTranslator;
	
	@Mock
	private PersonAddressTranslator addressTranslator;
	
	@Mock
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Mock
	private FhirPractitionerDao fhirPractitionerDao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private ProvenanceTranslator<Provider> provenanceTranslator;
	
	private BirthDateTranslator birthDateTranslator = new BirthDateTranslatorImpl();
	
	private PractitionerTranslatorProviderImpl practitionerTranslator;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		practitionerTranslator = new PractitionerTranslatorProviderImpl();
		practitionerTranslator.setAddressTranslator(addressTranslator);
		practitionerTranslator.setGenderTranslator(genderTranslator);
		practitionerTranslator.setNameTranslator(nameTranslator);
		practitionerTranslator.setTelecomTranslator(telecomTranslator);
		practitionerTranslator.setFhirPractitionerDao(fhirPractitionerDao);
		practitionerTranslator.setGlobalPropertyService(globalPropertyService);
		practitionerTranslator.setProvenanceTranslator(provenanceTranslator);
		practitionerTranslator.setBirthDateTranslator(birthDateTranslator);
		
		Person person = new Person();
		person.setGender(GENDER);
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		provider.setPerson(person);
		
		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		Identifier identifier = new Identifier();
		identifier.setValue(PRACTITIONER_IDENTIFIER);
		practitioner.addIdentifier();
	}
	
	@Test
	public void shouldTranslateOpenMrsProviderToFhirPractitioner() {
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
	}
	
	@Test
	public void shouldReturnNullWhenPractitionerIsNull() {
		Provider omrsProvider = practitionerTranslator.toOpenmrsType(provider, null);
		assertThat(omrsProvider, nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenProviderIsNull() {
		Practitioner practitioner = practitionerTranslator.toFhirResource(null);
		assertThat(practitioner, nullValue());
	}
	
	@Test
	public void shouldTranslateProviderUuidToFhirIdType() {
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getId(), notNullValue());
		assertThat(practitioner.getId(), equalTo(PROVIDER_UUID));
	}
	
	@Test
	public void shouldTranslateProviderGenderToFhirPractitionerType() {
		Person person = new Person();
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		person.addName(name);
		provider.setPerson(person);
		HumanName humanName = new HumanName();
		humanName.setFamily(FAMILY_NAME);
		humanName.addGiven(GIVEN_NAME);
		when(nameTranslator.toFhirResource(name)).thenReturn(humanName);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getName(), notNullValue());
		assertThat(practitioner.getName(), not(empty()));
		assertThat(practitioner.getName().get(0).getGiven().get(0).getValue(), equalTo(GIVEN_NAME));
		assertThat(practitioner.getName().get(0).getFamily(), equalTo(FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateProviderAddressToFhirPractitionerAddressType() {
		Person person = new Person();
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCityVillage(CITY_VILLAGE);
		personAddress.setCountry(COUNTRY);
		person.addAddress(personAddress);
		provider.setPerson(person);
		Address fhirAddress = new Address();
		fhirAddress.setCity(CITY_VILLAGE);
		fhirAddress.setCountry(COUNTRY);
		when(addressTranslator.toFhirResource(personAddress)).thenReturn(fhirAddress);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getAddress(), not(empty()));
		assertThat(practitioner.getAddress().get(0).getCountry(), equalTo(COUNTRY));
		assertThat(practitioner.getAddress().get(0).getCity(), equalTo(CITY_VILLAGE));
	}
	
	@Test
	public void shouldTranslateProviderGenderToFhirPractitionerGender() {
		when(genderTranslator.toFhirResource(GENDER)).thenReturn(Enumerations.AdministrativeGender.MALE);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getGender(), notNullValue());
		assertThat(practitioner.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldTranslateToOpenMrsType() {
		Provider provider = practitionerTranslator.toOpenmrsType(practitioner);
		assertThat(provider, notNullValue());
		assertThat(provider.getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldReturnUpdatedProvider() {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PROVIDER_UUID);
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		
		Provider result = practitionerTranslator.toOpenmrsType(provider, practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getIdentifier(), equalTo("349023n23b-t"));
	}
	
	@Test
	public void shouldTranslateFhirContactPointToPersonAttribute() {
		ProviderAttributeType attributeType = new ProviderAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		providerAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		providerAttribute.setAttributeType(attributeType);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(PERSON_ATTRIBUTE_UUID);
		contactPoint.setValue(PERSON_ATTRIBUTE_VALUE);
		practitioner.addTelecom(contactPoint);
		
		when(telecomTranslator.toOpenmrsType(any(), any())).thenReturn(providerAttribute);
		
		Provider provider = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(provider, notNullValue());
		assertThat(provider.getAttributes(), notNullValue());
		assertThat(provider.getAttributes().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		Provider provider = new Provider();
		provider.setDateChanged(new Date());
		
		Practitioner result = practitionerTranslator.toFhirResource(provider);
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateFhirPractitionerToOpenmrsProvider() {
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		
		Provider result = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateFhirPractitionerIdToUuid() {
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		
		Provider result = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldTranslateFhirGenderToOpenmrsGender() {
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		practitioner.setGender(Enumerations.AdministrativeGender.MALE);
		when(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.MALE)).thenReturn("M");
		
		Provider result = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getPerson().getGender(), equalTo("M"));
	}
	
	@Test
	public void shouldTranslateFhirNameToPersonName() {
		PersonName personName = new PersonName();
		personName.setGivenName(GIVEN_NAME);
		personName.setFamilyName(FAMILY_NAME);
		
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		HumanName name = practitioner.addName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		when(nameTranslator.toOpenmrsType(name)).thenReturn(personName);
		
		Provider result = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getPerson().getGivenName(), equalTo(GIVEN_NAME));
		assertThat(result.getPerson().getFamilyName(), equalTo(FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateFhirAddressToPersonAddress() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setUuid(ADDRESS_UUID);
		personAddress.setCityVillage(ADDRESS_CITY);
		
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		Address address = practitioner.addAddress();
		address.setId(ADDRESS_UUID);
		address.setCity(ADDRESS_CITY);
		when(addressTranslator.toOpenmrsType(address)).thenReturn(personAddress);
		
		Provider result = practitionerTranslator.toOpenmrsType(practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getPerson().getPersonAddress(), notNullValue());
		assertThat(result.getPerson().getPersonAddress().getUuid(), equalTo(ADDRESS_UUID));
		assertThat(result.getPerson().getPersonAddress().getCityVillage(), equalTo(ADDRESS_CITY));
	}
	
	@Test
	public void toFhirResource_shouldAddProvenanceResources() {
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		
		when(provenanceTranslator.getCreateProvenance(provider)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(provider)).thenReturn(provenance);
		
		org.hl7.fhir.r4.model.Practitioner result = practitionerTranslator.toFhirResource(provider);
		
		assertThat(result, notNullValue());
		assertThat(result.getContained(), not(empty()));
		assertThat(result.getContained().size(), greaterThanOrEqualTo(2));
		assertThat(result.getContained().stream()
		        .anyMatch(resource -> resource.getResourceType().name().equals(Provenance.class.getSimpleName())),
		    is(true));
	}
}
