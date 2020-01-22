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

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.module.fhir2.FhirConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirConceptSourceServiceImpl implements FhirConceptSourceService {
	
	@Inject
	private FhirConceptSourceDao dao;
	
	@Override
	@Transactional(readOnly = true)
	public Collection<FhirConceptSource> getFhirConceptSources() {
		return dao.getFhirConceptSources();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirConceptSource> getFhirConceptSourceByUrl(String url) {
		return dao.getFhirConceptSourceByUrl(url);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSourceName(String sourceName) {
		return dao.getFhirConceptSourceByConceptSourceName(sourceName);
	}
}
