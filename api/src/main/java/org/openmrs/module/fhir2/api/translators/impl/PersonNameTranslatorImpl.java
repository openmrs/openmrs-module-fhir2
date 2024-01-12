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

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.springframework.stereotype.Component;

@Component
public class PersonNameTranslatorImpl implements PersonNameTranslator {
	
	@Override
	public HumanName toFhirResource(@Nonnull PersonName name) {
		if (name == null || name.getVoided()) {
			return null;
		}
		
		HumanName humanName = new HumanName();
		humanName.setId(name.getUuid());
		if (name.getGivenName() != null) {
			humanName.addGiven(name.getGivenName());
		}
		
		if (name.getMiddleName() != null) {
			humanName.addGiven(name.getMiddleName());
		}
		
		if (name.getFamilyName() != null) {
			humanName.setFamily(name.getFamilyName());
		}
		
		addNameExtension(humanName, "prefix", name.getPrefix());
		addNameExtension(humanName, "familyNamePrefix", name.getFamilyNamePrefix());
		addNameExtension(humanName, "familyName2", name.getFamilyName2());
		addNameExtension(humanName, "familyNameSuffix", name.getFamilyNameSuffix());
		addNameExtension(humanName, "degree", name.getDegree());
		
		humanName.setText(name.getFullName());
		
		return humanName;
	}
	
	@Override
	public PersonName toOpenmrsType(@Nonnull HumanName name) {
		if (name == null) {
			return null;
		}
		
		return toOpenmrsType(new PersonName(), name);
	}
	
	@Override
	public PersonName toOpenmrsType(@Nonnull PersonName personName, @Nonnull HumanName name) {
		if (personName == null || name == null) {
			return personName;
		}
		
		if (name.hasId()) {
			personName.setUuid(name.getId());
		}
		
		List<StringType> givenNames = name.getGiven();
		if (!givenNames.isEmpty()) {
			personName.setGivenName(givenNames.get(0).getValue());
			
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < givenNames.size(); i++) {
				sb.append(givenNames.get(i).getValue()).append(" ");
			}
			
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			
			personName.setMiddleName(sb.toString());
		}
		
		if (name.hasFamily()) {
			personName.setFamilyName(name.getFamily());
		}
		
		getOpenmrsNameExtension(name).ifPresent(ext -> ext.getExtension()
		        .forEach(e -> addNameComponent(personName, e.getUrl(), ((StringType) e.getValue()).getValue())));
		
		return personName;
	}
	
	private void addNameExtension(@Nonnull HumanName name, @Nonnull String extensionProperty, @Nonnull String value) {
		if (value == null) {
			return;
		}
		
		getOpenmrsNameExtension(name).orElseGet(() -> name.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME))
		        .addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#" + extensionProperty, new StringType(value));
	}
	
	private void addNameComponent(@Nonnull PersonName name, @Nonnull String url, @Nonnull String value) {
		if (value == null || url == null || !url.startsWith(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#")) {
			return;
		}
		
		String propertyName = url.substring(url.lastIndexOf('#') + 1);
		switch (propertyName) {
			case "prefix":
				name.setPrefix(value);
				break;
			case "familyNamePrefix":
				name.setFamilyNamePrefix(value);
				break;
			case "familyName2":
				name.setFamilyName2(value);
				break;
			case "familyNameSuffix":
				name.setFamilyNameSuffix(value);
				break;
			case "degree":
				name.setDegree(value);
				break;
		}
	}
	
	private Optional<Extension> getOpenmrsNameExtension(@Nonnull HumanName name) {
		return Optional.ofNullable(name.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME));
	}
}
