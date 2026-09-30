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
 * Marks a Spring bean as a HAPI interceptor contributed by another module. It is registered on both
 * the R3 and the R4 servlet, and HAPI drops it unless it declares a {@code Hook} method.
 * <p>
 * A bean registered through this annotation runs after this module's own interceptors,
 * authentication included. This module's interceptors hold
 * {@link org.openmrs.module.fhir2.FhirConstants#BUILT_IN_INTERCEPTOR_ORDER}, and a contributed bean
 * has to stay at or above {@code Interceptor.DEFAULT_ORDER} to stay behind them: one whose
 * {@code Interceptor} or {@code Hook} declares a lower order is reported and left unregistered.
 * Order contributed interceptors against each other with positive values.
 * <p>
 * That check covers this annotation only. It is not a guarantee about the servlet as a whole -
 * other code can register an interceptor on it directly, at any order it likes.
 * <p>
 * The bean must be a class HAPI can scan: annotate a concrete class and keep it clear of
 * interface-based Spring AOP, which hides the hook methods behind a proxy. A bean HAPI finds no
 * hooks on is reported and does not run.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Qualifier
public @interface FhirInterceptor {
	
}
