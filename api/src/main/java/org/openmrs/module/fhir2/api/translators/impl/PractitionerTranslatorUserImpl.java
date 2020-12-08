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

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerTranslatorUserImpl implements PractitionerTranslator<User> {
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Override
	public Practitioner toFhirResource(@Nonnull User user) {
		notNull(user, "The User object should not be null");
		
		Practitioner practitioner = new Practitioner();
		practitioner.setId(user.getUuid());
		
		Identifier userIdentifier = new Identifier();
		userIdentifier.setSystem(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER);
		userIdentifier.setValue(user.getSystemId());
		practitioner.addIdentifier(userIdentifier);
		
		if (user.getPerson() != null) {
			
			if (user.getPerson().getBirthdateEstimated() != null) {
				if (user.getPerson().getBirthdateEstimated()) {
					DateType dateType = new DateType();
					int currentYear = LocalDate.now().getYear();
					int birthDateYear = LocalDate
					        .parse(new SimpleDateFormat("yyyy-MM-dd").format(user.getPerson().getBirthdate())).getYear();
					
					if ((currentYear - birthDateYear) > 5) {
						dateType.setValue(user.getPerson().getBirthdate(), TemporalPrecisionEnum.YEAR);
					} else {
						dateType.setValue(user.getPerson().getBirthdate(), TemporalPrecisionEnum.MONTH);
					}
					
					practitioner.setBirthDateElement(dateType);
				} else {
					practitioner.setBirthDate(user.getPerson().getBirthdate());
				}
			} else {
				practitioner.setBirthDate(user.getPerson().getBirthdate());
			}
			
			practitioner.setGender(genderTranslator.toFhirResource(user.getPerson().getGender()));
			for (PersonName name : user.getPerson().getNames()) {
				practitioner.addName(nameTranslator.toFhirResource(name));
			}
			for (PersonAddress address : user.getPerson().getAddresses()) {
				practitioner.addAddress(addressTranslator.toFhirResource(address));
			}
		}
		practitioner.getMeta().setLastUpdated(user.getDateChanged());
		
		return practitioner;
	}
	
	@Override
	public User toOpenmrsType(@Nonnull User user, @Nonnull Practitioner practitioner) {
		notNull(user, "The existing User object should not be null");
		notNull(practitioner, "The Practitioner object should not be null");
		
		user.setUuid(practitioner.getId());
		setSystemId(practitioner, user);
		user.setDateChanged(practitioner.getMeta().getLastUpdated());
		
		return user;
	}
	
	@Override
	public User toOpenmrsType(@Nonnull Practitioner practitioner) {
		notNull(practitioner, "The Practitioner object should not be null");
		return this.toOpenmrsType(new User(), practitioner);
	}
	
	private void setSystemId(Practitioner thePractitioner, User user) {
		thePractitioner.getIdentifier().forEach(practitioner -> {
			if (practitioner.hasSystem()) {
				if (practitioner.getSystem().equals(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER)) {
					user.setSystemId(practitioner.getValue());
				}
			}
		});
	}
}
