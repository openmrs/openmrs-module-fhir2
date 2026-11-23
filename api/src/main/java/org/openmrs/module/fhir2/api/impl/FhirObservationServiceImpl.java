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

import java.util.Optional;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.search.param.ObservationSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirObservationServiceImpl extends BaseCompositeFhirService<Observation> implements FhirObservationService {
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForObservations(ObservationSearchParams observationSearchParams) {
		return doSearch(observationSearchParams.toSearchParameterMap());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getLastnObservations(NumberParam max, ObservationSearchParams observationSearchParams) {
		SearchParameterMap theParams = observationSearchParams.toSearchParameterMap()
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, Optional.ofNullable(max).orElse(new NumberParam(1)));
		
		return doSearch(theParams);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getLastnEncountersObservations(NumberParam max, ObservationSearchParams observationSearchParams) {
		SearchParameterMap theParams = observationSearchParams.toSearchParameterMap()
		        .addParameter(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, Optional.ofNullable(max).orElse(new NumberParam(1)));
		
		return doSearch(theParams);
	}
}
