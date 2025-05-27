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

import static lombok.AccessLevel.PROTECTED;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.FhirEpisodeOfCareService;
import org.openmrs.module.fhir2.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.fhir2.api.translators.EpisodeOfCareTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirEpisodeOfCareServiceImpl extends BaseFhirService<EpisodeOfCare, PatientProgram> implements FhirEpisodeOfCareService {
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirEpisodeOfCareDao dao;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EpisodeOfCareTranslator translator;
}
