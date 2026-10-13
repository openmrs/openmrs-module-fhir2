/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;

/**
 * Base class for {@link FhirResourceHandler} implementations that own a single OpenMRS-table
 * backing.
 * <p>
 * Today this is a pure alias for {@link BaseFhirService} — concrete handlers extend this for CRUD
 * wiring and declare {@code implements FhirResourceHandler<R>} themselves. The intent is for this
 * type to eventually subsume {@code BaseFhirService}'s role; new code should target this base so
 * the transition lands without churn.
 */
public abstract class BaseFhirResourceHandler<T extends IAnyResource, U extends OpenmrsObject & Auditable> extends BaseFhirService<T, U> {}
