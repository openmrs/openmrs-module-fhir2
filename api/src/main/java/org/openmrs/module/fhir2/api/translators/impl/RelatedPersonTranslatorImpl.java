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

import java.util.Date;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class RelatedPersonTranslatorImpl implements RelatedPersonTranslator {
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FhirPatientDao patientDao;
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator#toFhirResource(org.openmrs.Relationship)
	 */
	@Override
	public RelatedPerson toFhirResource(Relationship relationship) {
		if (relationship == null) {
			return null;
		}
		Person omrsRelatedPerson = relationship.getPersonA();
		RelatedPerson relatedPerson = new RelatedPerson();
		
		relatedPerson.setId(relationship.getUuid());
		relatedPerson.setBirthDate(omrsRelatedPerson.getBirthdate());
		
		if (relationship.getPersonB().getIsPatient()) {
			relatedPerson.setPatient(
			    patientReferenceTranslator.toFhirResource(patientDao.getPatientByUuid(relationship.getPersonB().getUuid())));
		}
		
		if (omrsRelatedPerson.getGender() != null) {
			relatedPerson.setGender(genderTranslator.toFhirResource(omrsRelatedPerson.getGender()));
		}
		
		for (PersonName name : omrsRelatedPerson.getNames()) {
			relatedPerson.addName(nameTranslator.toFhirResource(name));
		}
		
		for (PersonAddress address : omrsRelatedPerson.getAddresses()) {
			relatedPerson.addAddress(addressTranslator.toFhirResource(address));
		}
		
		//identifier
		Identifier relationshipIdentifier = new Identifier();
		relationshipIdentifier.setSystem(FhirConstants.RELATED_PERSON);
		relationshipIdentifier.setValue(FhirConstants.PERSON + "/" + omrsRelatedPerson.getUuid());
		relatedPerson.addIdentifier(relationshipIdentifier);
		
		// Active
		if (relationship.getStartDate() == null
		        || relationship.getStartDate().before(new Date()) && relationship.getEndDate() == null
		        || relationship.getEndDate().after(new Date())) {
			relatedPerson.setActive(true);
		} else {
			relatedPerson.setActive(false);
		}
		
		Period period = new Period();
		period.setStart(relationship.getStartDate());
		period.setEnd(relationship.getEndDate());
		relatedPerson.setPeriod(period);
		
		return relatedPerson;
	}
}
