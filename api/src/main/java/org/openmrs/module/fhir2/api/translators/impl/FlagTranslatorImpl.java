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

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.FlagStatusTranslator;
import org.openmrs.module.fhir2.api.translators.FlagTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.model.FhirFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FlagTranslatorImpl implements FlagTranslator {
	
	@Autowired
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FlagStatusTranslator flagStatusTranslator;
	
	@Override
	public Flag toFhirResource(@Nonnull FhirFlag fhirFlag) {
		notNull(fhirFlag, "The FhirFlag object should not be null");
		
		Flag flag = new Flag();
		flag.setId(fhirFlag.getUuid());
		flag.setCode(new CodeableConcept().setText(fhirFlag.getFlag()));
		
		// Set period
		Period period = new Period();
		period.setStart(fhirFlag.getStartDate());
		period.setEnd(fhirFlag.getEndDate());
		flag.setPeriod(period);
		
		flag.setStatus(flagStatusTranslator.toFhirResource(fhirFlag.getStatus()));
		flag.setSubject(patientReferenceTranslator.toFhirResource(fhirFlag.getPatient()));
		flag.setAuthor(practitionerReferenceTranslator.toFhirResource(fhirFlag.getCreator()));
		
		return flag;
	}
	
	@Override
	public FhirFlag toOpenmrsType(@Nonnull Flag flag) {
		return this.toOpenmrsType(new FhirFlag(), flag);
	}
	
	@Override
	public FhirFlag toOpenmrsType(@Nonnull FhirFlag existingFhirFlag, @Nonnull Flag flag) {
		notNull(existingFhirFlag, "The ExistingFhirFlag object should not be null");
		notNull(flag, "The Flag object should not be null");
		
		if (flag.hasId()) {
			existingFhirFlag.setUuid(flag.getId());
		}
		
		if (flag.hasStatus()) {
			existingFhirFlag.setStatus(flagStatusTranslator.toOpenmrsType(flag.getStatus()));
		}
		
		if (flag.hasAuthor()) {
			existingFhirFlag.setCreator(practitionerReferenceTranslator.toOpenmrsType(flag.getAuthor()));
		}
		
		if (flag.hasSubject()) {
			existingFhirFlag.setPatient(patientReferenceTranslator.toOpenmrsType(flag.getSubject()));
		}
		
		if (flag.hasCode()) {
			existingFhirFlag.setFlag(flag.getCode().getText());
		}
		
		existingFhirFlag.setPriority(FhirFlag.FlagPriority.HIGH);
		
		if (flag.hasPeriod()) {
			Period period = flag.getPeriod();
			existingFhirFlag.setStartDate(period.getStart());
			existingFhirFlag.setEndDate(period.getEnd());
		}
		return existingFhirFlag;
	}
}
