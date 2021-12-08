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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Calendar;
import java.util.Date;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonTranslatorImplTest {
	
	private static final String RELATIONSHIP_UUID = "2d298ef7-4eb5-4753-a998-5b5e4b1cf48a";
	
	private static final String PATIENT_UUID = "4x398ef7-4eb5-4753-a998-5b5e4b1cf90u";
	
	private static final String PERSON_FAMILY_NAME = "John";
	
	private static final String PERSON_A_UUID = "de6e58a8-8cec-421b-8d2d-ce5c9b088a3c";
	
	private static final String PERSON_B_UUID = "54411b08-dcfe-4d59-9e3c-d4de6b5f6132";
	
	private static final String PERSON_GIVEN_NAME = "Joe";
	
	private static final String ADDRESS_UUID = "135791-xxxxxx-135791";
	
	private static final String ADDRESS_CITY = "vancouver";
	
	private static final String GENDER = "M";
	
	private static final String USER_UUID = "68b1e787-e68d-424e-8aac-c3387a0ab7b5";
	
	@Mock
	private GenderTranslator genderTranslator;
	
	@Mock
	private PersonNameTranslator nameTranslator;
	
	@Mock
	private PersonAddressTranslator addressTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private FhirPatientDao patientDao;
	
	private BirthDateTranslator birthDateTranslator = new BirthDateTranslatorImpl();
	
	private RelatedPersonTranslatorImpl relatedPersonTranslator;
	
	private Relationship relationship;
	
	private Person personA;
	
	@Before
	public void setup() {
		relatedPersonTranslator = new RelatedPersonTranslatorImpl();
		relatedPersonTranslator.setGenderTranslator(genderTranslator);
		relatedPersonTranslator.setNameTranslator(nameTranslator);
		relatedPersonTranslator.setAddressTranslator(addressTranslator);
		relatedPersonTranslator.setPatientDao(patientDao);
		relatedPersonTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		relatedPersonTranslator.setBirthDateTranslator(birthDateTranslator);
		
		User user = new User();
		user.setUuid(USER_UUID);
		
		personA = new Person();
		personA.setUuid(PERSON_A_UUID);
		personA.setGender(GENDER);
		personA.setCreator(user);
		personA.setDateCreated(new Date());
		personA.setChangedBy(user);
		personA.setDateChanged(new Date());
		
		Person personB = new Person();
		personB.setUuid(PERSON_A_UUID);
		personB.setPersonId(2);
		personB.setGender("F");
		personB.setCreator(user);
		personB.setDateCreated(new Date());
		personB.setChangedBy(user);
		personB.setDateChanged(new Date());
		
		relationship = new Relationship();
		relationship.setUuid(RELATIONSHIP_UUID);
		relationship.setPersonA(personA);
		relationship.setPersonB(personB);
	}
	
	@Test
	public void shouldTranslateOpenMrsRelationshipToFhirRelatedPerson() {
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfRelationshipToTranslateIsNull() {
		relatedPersonTranslator.toFhirResource(null);
	}
	
	@Test
	public void shouldTranslateOpenMrsRelationshipPersonAGenderToFhirRelatedPersonGenderType() {
		
		when(genderTranslator.toFhirResource(argThat(equalTo("M")))).thenReturn(Enumerations.AdministrativeGender.MALE);
		
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getGender(), is(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldNotTranslateGenderIfPersonAGenderIsNull() {
		personA.setGender(null);
		relationship.setPersonA(personA);
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result, notNullValue());
		assertThat(result.getGender(), nullValue());
	}
	
	@Test
	public void shouldTranslateRelationshipUuidToFhirIdType() {
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void shouldAddRelationshipIdentifier() {
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getIdentifier(), not(empty()));
		assertThat(result.getIdentifierFirstRep(), notNullValue());
		assertThat(result.getIdentifierFirstRep().getValue(), equalTo(FhirConstants.PERSON + "/" + PERSON_A_UUID));
		assertThat(result.getIdentifierFirstRep().getSystem(), equalTo(FhirConstants.RELATED_PERSON));
	}
	
	@Test
	public void shouldTranslateToActiveRelatedPerson() {
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(true));
	}
	
	@Test
	public void shouldTranslateToInactiveRelatedPerson() {
		relationship.setStartDate(new Date());
		relationship.setEndDate(new Date());
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(false));
	}
	
	@Test
	public void shouldTranslateRelationshipPersonAToFhirRelatedPersonName() {
		HumanName humanName = new HumanName();
		humanName.addGiven(PERSON_GIVEN_NAME);
		humanName.setFamily(PERSON_FAMILY_NAME);
		when(nameTranslator.toFhirResource(argThat(allOf(hasProperty("givenName", equalTo(PERSON_GIVEN_NAME)),
		    hasProperty("familyName", equalTo(PERSON_FAMILY_NAME)))))).thenReturn(humanName);
		
		PersonName name = new PersonName();
		name.setGivenName(PERSON_GIVEN_NAME);
		name.setFamilyName(PERSON_FAMILY_NAME);
		personA.addName(name);
		relationship.setPersonA(personA);
		
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result.getName(), not(empty()));
		assertThat(result.getName().get(0), notNullValue());
		assertThat(result.getName().get(0).getGivenAsSingleString(), equalTo(PERSON_GIVEN_NAME));
		assertThat(result.getName().get(0).getFamily(), equalTo(PERSON_FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateRelationshipPersonAAddressToFhirAddress() {
		Address address = new Address();
		address.setId(ADDRESS_UUID);
		address.setCity(ADDRESS_CITY);
		when(addressTranslator.toFhirResource(
		    argThat(allOf(hasProperty("uuid", equalTo(ADDRESS_UUID)), hasProperty("cityVillage", equalTo(ADDRESS_CITY))))))
		            .thenReturn(address);
		PersonAddress personAddress = new PersonAddress();
		personAddress.setUuid(ADDRESS_UUID);
		personAddress.setCityVillage(ADDRESS_CITY);
		personA.addAddress(personAddress);
		relationship.setPersonA(personA);
		
		org.hl7.fhir.r4.model.RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result.getAddress(), notNullValue());
		assertThat(result.getAddress(), not(empty()));
		assertThat(result.getAddress().get(0), equalTo(address));
	}
	
	@Test
	public void shouldTranslateStartDateEndDateToRelatedPersonPeriod() {
		relationship.setStartDate(new Date());
		relationship.setEndDate(new Date());
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getPeriod(), notNullValue());
		assertThat(result.getPeriod().getStart(), DateMatchers.sameDay(new Date()));
		assertThat(result.getPeriod().getEnd(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateToPeriodWithNullStartAndEnd() {
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getPeriod().getStart(), nullValue());
		assertThat(result.getPeriod().getEnd(), nullValue());
	}
	
	@Test
	public void shouldTranslatePersonBIfPatientToPatientReference() {
		Person personMock = Mockito.mock(Person.class);
		personMock.setUuid(PERSON_B_UUID);
		relationship.setPersonB(personMock);
		
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		Reference patientReference = new Reference();
		patientReference.setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID);
		
		when(personMock.getIsPatient()).thenReturn(true);
		when(personMock.getUuid()).thenReturn(PERSON_B_UUID);
		when(patientDao.get(PERSON_B_UUID)).thenReturn(patient);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient(), equalTo(patientReference));
		assertThat(result.getPatient().getReference(), equalTo(patientReference.getReference()));
	}
	
	@Test
	public void shouldTranslatePersonBToNullPatientReferenceIfNotPatient() {
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		assertThat(result, notNullValue());
		assertThat(result.getPatient().getReference(), nullValue());
	}
	
	@Test
	public void shouldTranslateToFhirBirthdate() {
		Person person = new Person();
		Calendar calendar = Calendar.getInstance();
		DateType dateType = new DateType();
		
		// when birthdate more than 5 year
		calendar.set(2000, Calendar.AUGUST, 12);
		person.setBirthdateEstimated(true);
		person.setBirthdate(calendar.getTime());
		relationship.setPersonA(person);
		
		RelatedPerson result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result, notNullValue());
		assertThat(result.getBirthDateElement().getPrecision(), equalTo(TemporalPrecisionEnum.YEAR));
		assertThat(result.getBirthDateElement().getYear(), equalTo(2000));
		
		//when birthDate less then 5 year
		Date date = new Date();
		person.setBirthdate(date);
		dateType.setValue(date, TemporalPrecisionEnum.MONTH);
		relationship.setPersonA(person);
		
		result = relatedPersonTranslator.toFhirResource(relationship);
		
		assertThat(result, notNullValue());
		assertThat(result.getBirthDateElement().getPrecision(), equalTo(TemporalPrecisionEnum.MONTH));
		assertThat(result.getBirthDateElement().getYear(), equalTo(dateType.getYear()));
		assertThat(result.getBirthDateElement().getMonth(), equalTo(dateType.getMonth()));
	}
	
	@Test
	public void shouldTranslateRelatedPersonToRelationship() {
		RelatedPerson relatedPerson = new RelatedPerson();
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateRelatedPersonIdToRelationshipUuid() {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATIONSHIP_UUID);
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void shouldTranslateFhirNameToOpenmrsName() {
		PersonName personName = new PersonName();
		personName.setGivenName(PERSON_GIVEN_NAME);
		personName.setFamilyName(PERSON_FAMILY_NAME);
		
		RelatedPerson relatedPerson = new RelatedPerson();
		HumanName name = relatedPerson.addName();
		name.addGiven(PERSON_GIVEN_NAME);
		name.setFamily(PERSON_FAMILY_NAME);
		when(nameTranslator.toOpenmrsType(name)).thenReturn(personName);
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getGivenName(), equalTo(PERSON_GIVEN_NAME));
		assertThat(result.getPersonA().getFamilyName(), equalTo(PERSON_FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateFhirAddressToOpenmrsAddress() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setUuid(ADDRESS_UUID);
		personAddress.setCityVillage(ADDRESS_CITY);
		
		RelatedPerson relatedPerson = new RelatedPerson();
		Address address = relatedPerson.addAddress();
		address.setId(ADDRESS_UUID);
		address.setCity(ADDRESS_CITY);
		when(addressTranslator.toOpenmrsType(address)).thenReturn(personAddress);
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getPersonAddress(), notNullValue());
		assertThat(result.getPersonA().getPersonAddress().getUuid(), equalTo(ADDRESS_UUID));
		assertThat(result.getPersonA().getPersonAddress().getCityVillage(), equalTo(ADDRESS_CITY));
		
	}
	
	@Test
	public void shouldTranslateFhirGenderToOpenmrsGender() {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setGender(Enumerations.AdministrativeGender.MALE);
		when(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.MALE)).thenReturn("M");
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getGender(), equalTo("M"));
	}
	
	@Test
	public void shouldTranslateToOpenmrsBirthdate() {
		RelatedPerson relatedPerson = new RelatedPerson();
		Date date = new Date();
		DateType dateType = new DateType();
		
		// for TemporalPrecisionEnum.DAY
		dateType.setValue(date, TemporalPrecisionEnum.DAY);
		relatedPerson.setBirthDateElement(dateType);
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getBirthdateEstimated(), equalTo(false));
		assertThat(result.getPersonA().getBirthdate(), equalTo(date));
		
		// for TemporalPrecisionEnum.Month
		dateType.setValue(date, TemporalPrecisionEnum.MONTH);
		relatedPerson.setBirthDateElement(dateType);
		
		result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getBirthdateEstimated(), equalTo(true));
		assertThat(result.getPersonA().getBirthdate(), equalTo(date));
		
		// for TemporalPrecisionEnum.YEAR
		dateType.setValue(date, TemporalPrecisionEnum.YEAR);
		relatedPerson.setBirthDateElement(dateType);
		
		result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonA().getBirthdateEstimated(), equalTo(true));
		assertThat(result.getPersonA().getBirthdate(), equalTo(date));
	}
	
	@Test
	public void shouldTranslateFhirStartDateAndEndDateToOpenmrsStartDateAndEndDate() {
		RelatedPerson relatedPerson = new RelatedPerson();
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.APRIL, 16);
		relatedPerson.getPeriod().setStart(calendar.getTime());
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getStartDate().getTime(), equalTo(calendar.getTime().getTime()));
		
		calendar.set(2000, Calendar.SEPTEMBER, 29);
		relatedPerson.getPeriod().setEnd(calendar.getTime());
		
		result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getEndDate().getTime(), equalTo(calendar.getTime().getTime()));
	}
	
	@Test
	public void shouldTranslateFhirPatientToOpenmrsPersonB() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		RelatedPerson relatedPerson = new RelatedPerson();
		Reference reference = new Reference();
		relatedPerson.setPatient(reference);
		
		when(patientReferenceTranslator.toOpenmrsType(relatedPerson.getPatient())).thenReturn(patient);
		
		Relationship result = relatedPersonTranslator.toOpenmrsType(relatedPerson);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonB().getUuid(), equalTo(PATIENT_UUID));
	}
}
