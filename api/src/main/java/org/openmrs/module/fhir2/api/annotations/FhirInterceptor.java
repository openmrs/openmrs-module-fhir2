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
 * Marks a Spring bean as a HAPI interceptor the FHIR REST servlets should register, so a module
 * other than this one can add a cross-cutting concern -- authorization, audit, consent -- in the
 * way {@link R4Provider} marks a resource provider.
 * <p>
 * The bean must carry at least one {@code Hook} method: HAPI logs "Interceptor registered with no
 * valid hooks" and drops one that does not, so it is absent from the request path with only a
 * warning to say so.
 * <p>
 * Registration order is only HAPI's tie-break -- it sorts each hook by {@code Interceptor(order)},
 * which a non-zero {@code Hook(order)} overrides per method. This module's own interceptors all
 * leave that at 0, so a contributed bean with a negative order runs ahead of them, authentication
 * included.
 * <p>
 * Every version-specific servlet registers it, so one meant for a single FHIR version must work out
 * which it is serving: {@code getFhirContext().getVersion().getVersion()} on a hook handed
 * {@code RequestDetails}, and on {@code Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED}, which is
 * passed only the request and the response, the servlet path.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
