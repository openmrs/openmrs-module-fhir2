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
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * The CRUD and search service for FHIR {@link Encounter} resources backed by the OpenMRS
 * {@code Encounter} domain object (and its table), as distinct from the {@link FhirVisitService}
 * which serves the same FHIR resource type from the {@code Visit} backing. Sibling of
 * {@link FhirVisitService} — the
 * {@link org.openmrs.module.fhir2.api.handler.EncounterBackedEncounterHandler} composes this
 * service in the same way the
 * {@link org.openmrs.module.fhir2.api.handler.VisitBackedEncounterHandler} composes
 * {@code FhirVisitService}.
 * <p>
 * Callers should generally use {@link FhirEncounterService} (the composite orchestrator) — this
 * interface exists to keep encounter-table-only CRUD behaviour in an {@code api}-package type so
 * the handler in {@code api.handler} can depend on it without reaching into the {@code api.impl}
 * package.
 */
public interface FhirOpenmrsEncounterService extends FhirService<Encounter> {
	
	IBundleProvider searchForEncounters(SearchParameterMap theParams);
}
