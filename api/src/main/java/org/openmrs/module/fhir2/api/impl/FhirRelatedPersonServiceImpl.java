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
import org.hl7.fhir.r4.model.RelatedPerson;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirRelatedPersonServiceImpl implements FhirRelatedPersonService {
	
	@Autowired
	private FhirRelatedPersonDao fhirRelatedPersonDao;
	
	@Autowired
	private RelatedPersonTranslator relatedPersonTranslator;
	
	/**
	 * @see org.openmrs.module.fhir2.api.FhirRelatedPersonService#getRelatedPersonById(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public RelatedPerson getRelatedPersonById(String Id) {
		return relatedPersonTranslator.toFhirResource(fhirRelatedPersonDao.getRelationshipByUuid(Id));
	}
}
