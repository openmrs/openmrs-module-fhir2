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
 * Registering one from outside was not previously possible. The servlet unregisters every
 * interceptor when the module context refreshes, and that happens whenever any OpenMRS module
 * starts or stops, so an interceptor added at runtime worked until the next module event and then
 * silently stopped.
 * <p>
 * The bean must carry at least one {@code Hook} method. HAPI refuses one that does not, logging
 * "Interceptor registered with no valid hooks" and continuing, so an annotated bean without hooks is
 * absent from the request path with nothing but a warning to say so.
 * <p>
 * Beans found here are registered after the interceptors this module owns, so authentication has
 * already run. Order among contributed interceptors is HAPI's own, from
 * {@code Interceptor(order = ...)}. They are registered on every version-specific servlet, so an
 * interceptor meant for one FHIR version should check the version it is handed.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
