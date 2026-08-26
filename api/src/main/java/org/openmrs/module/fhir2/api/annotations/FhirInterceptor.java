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
 * Registering one from outside used not to last. The servlet unregisters every interceptor when the
 * module context refreshes, and that happens whenever any OpenMRS module starts or stops, so an
 * interceptor added from a Spring bean or from a module's {@code started()} worked until the next
 * module event and then silently stopped. A module that re-registered from its own
 * {@code contextRefreshed()} did survive, being called after this one's.
 * <p>
 * The bean must carry at least one {@code Hook} method. HAPI refuses one that does not, logging
 * "Interceptor registered with no valid hooks" and continuing, so an annotated bean without hooks
 * is absent from the request path with nothing but a warning to say so.
 * <p>
 * Beans found here are registered after the interceptors this module owns, but registration order
 * is only HAPI's tie-break. On any one hook it sorts by {@code Interceptor(order = ...)} first --
 * or {@code Hook(order = ...)}, which overrides it per method -- and every interceptor this module
 * owns leaves that at its default of 0, so a contributed bean declaring a negative order runs
 * before them on the hooks it shares with them, authentication among those.
 * <p>
 * They are registered on every version-specific servlet, so an interceptor meant for one FHIR
 * version has to work out which it is serving. On a hook handed {@code RequestDetails} that is
 * {@code getFhirContext().getVersion().getVersion()}. On
 * {@code Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED} -- the earliest, and the one this module's
 * own authentication interceptor uses -- there is no such handle: it is passed only the request and
 * the response, so the servlet path is the only discriminator.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
