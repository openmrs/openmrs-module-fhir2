/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPractitionerServiceImpl implements FhirPractitionerService {
	
	@Inject
	private FhirPractitionerDao dao;
	
	@Inject
	private PractitionerTranslator translator;
	
	@Override
	public Practitioner getPractitionerByUuid(String uuid) {
		return translator.toFhirResource(dao.getProviderByUuid(uuid));
	}
	
	@Override
	public Collection<Practitioner> findPractitionerByName(String name) {
		return dao.findProviderByName(name).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Collection<Practitioner> findPractitionerByIdentifier(String identifier) {
		return dao.findProviderByIdentifier(identifier).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
}
