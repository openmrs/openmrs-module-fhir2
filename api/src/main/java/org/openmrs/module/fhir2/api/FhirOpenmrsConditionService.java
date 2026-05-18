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
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * The CRUD and search service for FHIR {@link Condition} resources backed by the OpenMRS
 * {@code Condition} domain object (and its table), as distinct from the
 * {@link FhirDiagnosisService} which serves the same FHIR resource type from the {@code Diagnosis}
 * backing. Sibling of {@link FhirDiagnosisService} — the
 * {@link org.openmrs.module.fhir2.api.handler.ConditionBackedConditionHandler} composes this
 * service in the same way the
 * {@link org.openmrs.module.fhir2.api.handler.DiagnosisBackedConditionHandler} composes
 * {@code FhirDiagnosisService}.
 * <p>
 * Callers should generally use {@link FhirConditionService} (the composite orchestrator) — this
 * interface exists to keep condition-table-only CRUD behaviour in an {@code api}-package type so
 * the handler in {@code api.handler} can depend on it without reaching into the {@code api.impl}
 * package.
 */
public interface FhirOpenmrsConditionService extends FhirService<Condition> {
	
	IBundleProvider searchForConditions(SearchParameterMap theParams);
}
