/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Serves FHIR {@link Practitioner} resources backed by the OpenMRS {@code Provider}, for use by
 * {@link org.openmrs.module.fhir2.api.handler.ProviderBackedPractitionerHandler}.
 * {@link FhirUserService} serves those backed by the OpenMRS {@code User}.
 * <p>
 * Callers should generally use {@link FhirPractitionerService}, which dispatches across both
 * backings.
 */
public interface FhirOpenmrsProviderService extends FhirService<Practitioner> {
	
	IBundleProvider searchForPractitioners(SearchParameterMap theParams);
}
