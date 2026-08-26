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
 * Marks a Spring bean as a HAPI interceptor the FHIR REST servlets should register, so that a
 * module other than this one can add a cross-cutting concern to the FHIR API -- authorization,
 * audit, consent. The bean carries HAPI's own {@code Hook} methods; this annotation only says it
 * should be picked up, in the way {@link R4Provider} says so for a resource provider.
 * <p>
 * The bean must carry at least one {@code Hook} method. HAPI refuses one that does not, logging
 * "Interceptor registered with no valid hooks" and continuing, so an annotated bean without hooks
 * is absent from the request path with nothing but a warning to say so.
 * <p>
 * Registration order is only HAPI's tie-break: on each hook it sorts by
 * {@code Interceptor(order = ...)}, which {@code Hook(order = ...)} overrides per method when
 * non-zero. Every interceptor this module owns leaves that at the default 0, so a contributed bean
 * with a negative order runs ahead of them -- authentication included.
 * <p>
 * They are registered on every version-specific servlet, so an interceptor meant for one FHIR
 * version has to work out which it is serving. On a hook handed {@code RequestDetails} that is
 * {@code getFhirContext().getVersion().getVersion()}. On
 * {@code Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED} there is no such handle -- it is passed
 * only the request and the response -- so the servlet path is the only discriminator.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
