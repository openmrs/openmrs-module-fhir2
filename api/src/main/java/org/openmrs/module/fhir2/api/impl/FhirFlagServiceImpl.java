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

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Flag;
import org.openmrs.module.fhir2.api.FhirFlagService;
import org.openmrs.module.fhir2.api.dao.FhirFlagDao;
import org.openmrs.module.fhir2.api.translators.PatientFlagTranslator;
import org.openmrs.module.patientflags.PatientFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirFlagServiceImpl extends BaseFhirService<Flag, PatientFlag> implements FhirFlagService {
	
	@Autowired
	private FhirFlagDao dao;
	
	@Autowired
	private PatientFlagTranslator translator;
	
	public Flag getFlag(@Nonnull Integer id) {
		return translator.toFhirResource(dao.getPatientFlagForPatientId(id));
	}
}
