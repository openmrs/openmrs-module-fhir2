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
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerTranslatorProviderImpl implements PractitionerTranslator<Provider> {
	
	@Inject
	private PersonNameTranslator nameTranslator;
	
	@Inject
	private AddressTranslator addressTranslator;
	
	@Inject
	private GenderTranslator genderTranslator;
	
	@Inject
	private TelecomTranslator<Object> telecomTranslator;
	
	@Inject
	private FhirPractitionerDao fhirPractitionerDao;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
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
		
		Set<ProviderAttribute> attributes = practitioner.getTelecom().stream().map(
		    contactPoint -> (ProviderAttribute) telecomTranslator.toOpenmrsType(new ProviderAttribute(), contactPoint))
		        .collect(Collectors.toSet());
		existingProvider.setAttributes(attributes);
		existingProvider.setDateChanged(practitioner.getMeta().getLastUpdated());
		
		return existingProvider;
	}
	
	@Override
	public Practitioner toFhirResource(Provider provider) {
		Practitioner practitioner = new Practitioner();
		if (provider == null) {
			return practitioner;
		}
		
		Identifier identifier = new Identifier();
		identifier.setSystem(FhirConstants.OPENMRS_FHIR_EXT_PROVIDER_IDENTIFIER);
		identifier.setValue(provider.getIdentifier());
		practitioner.addIdentifier(identifier);
		
		practitioner.setId(provider.getUuid());
		practitioner.setActive(provider.getRetired());
		practitioner.setTelecom(getProviderContactDetails(provider));
		
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
		practitioner.getMeta().setLastUpdated(provider.getDateChanged());
		
		return practitioner;
	}
	
	public List<ContactPoint> getProviderContactDetails(@NotNull Provider provider) {
		return fhirPractitionerDao
		        .getActiveAttributesByPractitionerAndAttributeTypeUuid(provider,
		            globalPropertyService.getGlobalProperty(FhirConstants.PROVIDER_ATTRIBUTE_TYPE_PROPERTY))
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Provider toOpenmrsType(Practitioner practitioner) {
		return toOpenmrsType(new org.openmrs.Provider(), practitioner);
	}
	
}
