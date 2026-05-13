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

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Static helpers for {@link FhirResourceHandler} implementations.
 * <p>
 * The main use today is the tag-based search-routing convention. Two handlers backing the same FHIR
 * resource type (e.g. encounter and visit) share a routing coding system (the
 * {@code OPENMRS_FHIR_EXT_ENCOUNTER_TAG} system) with distinct codes; each handler opts out of a
 * search when {@code _tag} contains a coding in that shared system whose code isn't its own. Tags
 * from unrelated coding systems are treated as content filters and never cause opt-out.
 */
public final class HandlerSupport {
	
	private HandlerSupport() {
		// no instances
	}
	
	/**
	 * Returns whether a {@code _tag} parameter on the search request routes the request to a different
	 * handler than the caller. The check looks at every AND clause of the {@code _tag} parameter — a
	 * clause excludes the caller if it references the given routing system but none of its OR
	 * alternatives carry the caller's code.
	 *
	 * @param params the search parameter map; the {@code _tag} parameter is read from the entry stored
	 *            under {@link FhirConstants#TAG_SEARCH_HANDLER}
	 * @param routingSystem the coding system that handlers in this resource family use to route search
	 *            requests (e.g. {@code OPENMRS_FHIR_EXT_ENCOUNTER_TAG})
	 * @param routingCode the calling handler's code in the routing system (e.g. {@code "encounter"} or
	 *            {@code "visit"})
	 * @return {@code true} if any AND clause of {@code _tag} references the routing system but doesn't
	 *         include the caller's code (meaning the caller should opt out of the search)
	 */
	public static boolean routingTagExcludes(@Nonnull SearchParameterMap params, @Nonnull String routingSystem,
	        @Nonnull String routingCode) {
		TokenAndListParam tagParam = extractTagParam(params);
		if (tagParam == null || tagParam.size() == 0) {
			return false;
		}
		
		for (TokenOrListParam orList : tagParam.getValuesAsQueryTokens()) {
			if (orList == null) {
				continue;
			}
			
			boolean systemReferenced = false;
			boolean codeMatched = false;
			for (TokenParam token : orList.getValuesAsQueryTokens()) {
				if (token == null) {
					continue;
				}
				if (routingSystem.equals(token.getSystem())) {
					systemReferenced = true;
					if (routingCode.equals(token.getValue())) {
						codeMatched = true;
						break;
					}
				}
			}
			
			if (systemReferenced && !codeMatched) {
				return true;
			}
		}
		
		return false;
	}
	
	private static TokenAndListParam extractTagParam(SearchParameterMap params) {
		for (Map.Entry<String, List<PropParam<?>>> entry : params.getParameters()) {
			if (!FhirConstants.TAG_SEARCH_HANDLER.equals(entry.getKey())) {
				continue;
			}
			for (PropParam<?> propParam : entry.getValue()) {
				Object value = propParam.getParam();
				if (value instanceof TokenAndListParam) {
					return (TokenAndListParam) value;
				}
			}
		}
		return null;
	}
}
