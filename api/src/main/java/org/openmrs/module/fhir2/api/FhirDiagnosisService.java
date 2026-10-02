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
 * {@code Diagnosis} domain object (and its table), as distinct from
 * {@link FhirOpenmrsConditionService} which serves the same FHIR resource type from the
 * {@code Condition} backing. Sibling of {@link FhirOpenmrsConditionService} — the
 * {@link org.openmrs.module.fhir2.api.handler.DiagnosisBackedConditionHandler} composes this
 * service in the same way the
 * {@link org.openmrs.module.fhir2.api.handler.ConditionBackedConditionHandler} composes
 * {@code FhirOpenmrsConditionService}.
 * <p>
 * Callers should generally use {@link FhirConditionService} (the composite orchestrator); this
 * interface is reserved for the handler-side delegate.
 */
public interface FhirDiagnosisService extends FhirService<Condition> {
	
	IBundleProvider searchDiagnoses(SearchParameterMap theParams);
}
