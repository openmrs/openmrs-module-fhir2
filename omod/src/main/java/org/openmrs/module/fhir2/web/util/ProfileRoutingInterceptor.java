/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.fhir2.api.util.ProfileRoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Captures the {@code _profile} search parameter so the composite services can route a search to
 * the handler that owns the named profile.
 * <p>
 * HAPI ignores {@code _profile} when matching a request to a {@code @Search} method — any
 * underscore-prefixed parameter other than {@code _id}, {@code _include} and {@code _revinclude} is
 * skipped — so the value never reaches a resource provider and cannot be picked up by declaring it
 * on one. Reading it from the request here makes handler routing work uniformly for every resource
 * type instead of only those whose provider was individually taught about profiles.
 */
@Component
@Interceptor
public class ProfileRoutingInterceptor {
	
	@Getter(PROTECTED)
	@Setter(value = PUBLIC, onMethod_ = @Autowired)
	private ProfileRoutingContext profileRoutingContext;
	
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public void captureRequestedProfiles(RequestDetails requestDetails) {
		profileRoutingContext.setRequestedProfiles(requestedProfiles(requestDetails));
	}
	
	/**
	 * Clears the captured profiles once the request is done. Bound to
	 * {@code SERVER_PROCESSING_COMPLETED} rather than {@code SERVER_PROCESSING_COMPLETED_NORMALLY}
	 * because it fires whether or not the request succeeded — a failed request must not leave a profile
	 * behind on a pooled thread for the next request to pick up.
	 */
	@Hook(Pointcut.SERVER_PROCESSING_COMPLETED)
	public void clearRequestedProfiles(RequestDetails requestDetails) {
		profileRoutingContext.clear();
	}
	
	/**
	 * Reads the {@code _profile} values off the request. Repeated parameters and comma-separated values
	 * are flattened together: routing treats every profile the client mentioned as a candidate handler,
	 * so the AND/OR distinction FHIR draws for content filtering does not apply. Modified forms such as
	 * {@code _profile:below} are not recognised and are left to be handled as ordinary unsupported
	 * parameters.
	 */
	private static Set<String> requestedProfiles(RequestDetails requestDetails) {
		String[] values = requestDetails.getParameters().get(Constants.PARAM_PROFILE);
		
		if (values == null || values.length == 0) {
			return Collections.emptySet();
		}
		
		Set<String> profiles = new LinkedHashSet<>();
		for (String value : values) {
			if (value == null) {
				continue;
			}
			
			for (String profile : value.split(",")) {
				String trimmed = profile.trim();
				if (!trimmed.isEmpty()) {
					profiles.add(trimmed);
				}
			}
		}
		
		return profiles;
	}
}
