/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import java.util.List;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.springframework.stereotype.Component;

@Component
public class PersonNameTranslatorImpl implements PersonNameTranslator {
	
	@Override
	public HumanName toFhirResource(PersonName name) {
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
		
		// TODO handle other name components
		
		return humanName;
	}
	
	@Override
	public PersonName toOpenmrsType(HumanName name) {
		PersonName personName = new PersonName();
		personName.setUuid(name.getId());
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
		
		if (name.getFamily() != null) {
			personName.setFamilyName(name.getFamily());
		}
		
		// TODO handle other name components
		
		return personName;
	}
}
