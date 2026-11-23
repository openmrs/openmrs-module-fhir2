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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.search.param.PractitionerSearchParams;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirPractitionerServiceImpl extends BaseCompositeFhirService<Practitioner> implements FhirPractitionerService {
	
	@Override
	public Practitioner create(@Nonnull Practitioner newResource) {
		if (!newResource.hasIdentifier()) {
			throw new UnprocessableEntityException("New providers must have at least one identifier");
		}
		
		return super.create(newResource);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForPractitioners(PractitionerSearchParams practitionerSearchParams) {
		return doSearch(practitionerSearchParams.toSearchParameterMap());
	}
}
