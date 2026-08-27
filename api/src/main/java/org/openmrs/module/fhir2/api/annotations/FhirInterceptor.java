/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Marks a Spring bean as a HAPI interceptor every FHIR REST servlet registers, the way
 * {@link R4Provider} marks a resource provider.
 * <p>
 * Carry at least one {@code Hook} method; HAPI drops an interceptor without one.
 * <p>
 * On a hook handed no {@code RequestDetails}, read the FHIR version from
 * {@code FhirVersionUtils.getFhirVersion(request)} rather than the servlet path.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
