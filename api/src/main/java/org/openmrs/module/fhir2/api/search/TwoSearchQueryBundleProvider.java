/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import java.util.Arrays;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

/**
 * Two-provider façade preserved for backward compatibility with callers written before the N-ary
 * {@link CompositeBundleProvider} existed. Behaviour is identical to constructing a
 * {@code CompositeBundleProvider} with the two providers in the same order.
 *
 * @deprecated use {@link CompositeBundleProvider} directly. New code should pass an N-element list
 *             rather than reaching for this two-element specialisation.
 */
@Deprecated
public class TwoSearchQueryBundleProvider extends CompositeBundleProvider {
	
	public TwoSearchQueryBundleProvider(IBundleProvider firstProvider, IBundleProvider secondProvider,
	    FhirGlobalPropertyService globalPropertyService) {
		super(Arrays.asList(firstProvider, secondProvider), globalPropertyService);
	}
}
