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
import javax.transaction.Transactional;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientIdentifierSystemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirPatientIdentifierSystemImpl implements FhirPatientIdentifierSystemDao {
	
	@Autowired
	private FhirPatientIdentifierSystemDao dao;
	
	@Override
	public String getUrlByPatientIdentifierType(@Nonnull PatientIdentifierType patientIdentifierType) {
		return dao.getUrlByPatientIdentifierType(patientIdentifierType);
	}
}
