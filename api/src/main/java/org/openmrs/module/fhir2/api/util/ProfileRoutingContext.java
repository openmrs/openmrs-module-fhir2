/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Carries the {@code _profile} values requested by the current FHIR search from the web layer down
 * to {@code BaseCompositeFhirService}, which uses them to route a search to a specific
 * {@link org.openmrs.module.fhir2.api.handler.FhirResourceHandler handler}.
 * <p>
 * A request-scoped holder is used rather than threading {@code _profile} through every resource
 * provider and search-parameter class because handler routing must work for <em>any</em> resource
 * type — including one an external module has added a backing to — without that resource's provider
 * knowing about profiles.
 * <p>
 * Callers outside an HTTP request (another module invoking a service directly) never populate this
 * and always see an empty set; they can still target a handler by putting
 * {@link org.openmrs.module.fhir2.FhirConstants#PROFILE_SEARCH_HANDLER} into a
 * {@code SearchParameterMap} themselves.
 */
@Component
public class ProfileRoutingContext {
	
	private final ThreadLocal<Set<String>> requestedProfiles = new ThreadLocal<>();
	
	/**
	 * Records the profiles requested by the current request, replacing any previous value. Passing
	 * {@code null} or an empty collection clears the holder rather than storing an empty set, so a
	 * request without {@code _profile} leaves nothing behind for the next user of this thread.
	 *
	 * @param profiles the canonical profile URLs the client asked for
	 */
	public void setRequestedProfiles(Collection<String> profiles) {
		if (profiles == null || profiles.isEmpty()) {
			clear();
			return;
		}
		
		requestedProfiles.set(Collections.unmodifiableSet(new LinkedHashSet<>(profiles)));
	}
	
	/**
	 * The profiles requested by the current request, in the order they were supplied.
	 *
	 * @return the requested profile URLs, or an empty set when none were requested
	 */
	@Nonnull
	public Set<String> getRequestedProfiles() {
		Set<String> profiles = requestedProfiles.get();
		return profiles == null ? Collections.emptySet() : profiles;
	}
	
	/**
	 * Discards the profiles held for the current thread. Uses {@link ThreadLocal#remove()} rather than
	 * storing {@code null} so the entry is not retained against a pooled thread.
	 */
	public void clear() {
		requestedProfiles.remove();
	}
}
