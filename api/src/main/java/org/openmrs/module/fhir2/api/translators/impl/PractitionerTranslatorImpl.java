/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerTranslatorImpl implements PractitionerTranslator {
	
	@Inject
	private PersonNameTranslator nameTranslator;
	
	@Inject
	private AddressTranslator addressTranslator;
	
	@Inject
	private GenderTranslator genderTranslator;
	
	@Override
	public Provider toOpenmrsType(Provider existingProvider, Practitioner practitioner) {
		Validate.notNull(existingProvider, "Existing provider cannot be null");
		if (practitioner == null) {
			return existingProvider;
		}
		existingProvider.setUuid(practitioner.getId());
		Validate.notEmpty(practitioner.getIdentifier(), "Practitioner Identifier cannot be empty");
		existingProvider.setIdentifier(practitioner.getIdentifier().get(0).getValue());
		if (!practitioner.getActive()) {
			existingProvider.setRetired(practitioner.getActive());
			existingProvider.setDateRetired(new Date());
			existingProvider.setRetireReason("Retired By FHIR module");
		}
		
		return existingProvider;
	}
	
	@Override
	public Practitioner toFhirResource(Provider provider) {
		Practitioner practitioner = new Practitioner();
		if (provider == null) {
			return practitioner;
		}
		List<Identifier> identifiers = new ArrayList<Identifier>();
		Identifier identifier = new Identifier();
		identifier.setValue(provider.getIdentifier());
		identifiers.add(identifier);
		practitioner.setIdentifier(identifiers);
		practitioner.setId(provider.getUuid());
		practitioner.setActive(provider.getRetired());
		
		if (provider.getPerson() != null) {
			practitioner.setBirthDate(provider.getPerson().getBirthdate());
			practitioner.setGender(genderTranslator.toFhirResource(provider.getPerson().getGender()));
			for (PersonName name : provider.getPerson().getNames()) {
				practitioner.addName(nameTranslator.toFhirResource(name));
			}
			for (PersonAddress address : provider.getPerson().getAddresses()) {
				practitioner.addAddress(addressTranslator.toFhirResource(address));
			}
		}
		
		return practitioner;
	}
	
	@Override
	public Provider toOpenmrsType(Practitioner practitioner) {
		return toOpenmrsType(new org.openmrs.Provider(), practitioner);
	}
	
}
