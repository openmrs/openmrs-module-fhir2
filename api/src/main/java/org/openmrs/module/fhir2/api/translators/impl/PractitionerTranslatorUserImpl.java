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

import javax.inject.Inject;

import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerTranslatorUserImpl implements PractitionerTranslator<User> {
	
	@Inject
	private PersonNameTranslator nameTranslator;
	
	@Inject
	private AddressTranslator addressTranslator;
	
	@Inject
	private GenderTranslator genderTranslator;
	
	@Inject
	private ProvenanceTranslator<User> provenanceTranslator;
	
	@Override
	public Practitioner toFhirResource(User user) {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(user.getUuid());
		
		Identifier userIdentifier = new Identifier();
		userIdentifier.setSystem(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER);
		userIdentifier.setValue(user.getUserId().toString());
		practitioner.addIdentifier(userIdentifier);
		
		if (user.getPerson() != null) {
			practitioner.setBirthDate(user.getPerson().getBirthdate());
			practitioner.setGender(genderTranslator.toFhirResource(user.getPerson().getGender()));
			for (PersonName name : user.getPerson().getNames()) {
				practitioner.addName(nameTranslator.toFhirResource(name));
			}
			for (PersonAddress address : user.getPerson().getAddresses()) {
				practitioner.addAddress(addressTranslator.toFhirResource(address));
			}
		}
		practitioner.getMeta().setLastUpdated(user.getDateChanged());
		practitioner.addContained(provenanceTranslator.getCreateProvenance(user));
		practitioner.addContained(provenanceTranslator.getUpdateProvenance(user));
		
		return practitioner;
	}
	
	@Override
	public User toOpenmrsType(User user, Practitioner practitioner) {
		if (practitioner == null) {
			return user;
		}
		user.setUuid(practitioner.getId());
		user.setUserId(getUserId(practitioner));
		user.setDateChanged(practitioner.getMeta().getLastUpdated());
		
		return user;
	}
	
	@Override
	public User toOpenmrsType(Practitioner practitioner) {
		return this.toOpenmrsType(new User(), practitioner);
	}
	
	private Integer getUserId(Practitioner practitioner) {
		return Integer.parseInt(
		    practitioner.getIdentifier().stream().map(this::isEquals).collect(Collectors.toList()).get(0).getValue());
	}
	
	private Identifier isEquals(Identifier identifier) {
		if (identifier.getSystem().equals(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER))
			return identifier;
		return null;
	}
}
