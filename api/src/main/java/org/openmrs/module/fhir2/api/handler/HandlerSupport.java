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
 * The main use today is search-routing via a coding system shared by handlers backing the same FHIR
 * resource type. Each handler declares its routing code in that system and opts out of a search
 * whenever a routing token (read from {@code _tag} or from a domain-specific token search param
 * such as {@code category}) references the shared system but doesn't include the caller's code.
 * Tokens in unrelated coding systems are treated as content filters and never cause opt-out.
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
		return tokenParamExcludes(extractTokenAndListParam(params, FhirConstants.TAG_SEARCH_HANDLER), routingSystem,
		    routingCode);
	}
	
	/**
	 * Returns whether a {@code category} parameter on the search request routes the request to a
	 * different handler than the caller. Semantics mirror {@link #routingTagExcludes}: an AND clause
	 * excludes the caller when it references the given coding system but none of its OR alternatives
	 * carry the caller's code.
	 *
	 * @param params the search parameter map; the {@code category} parameter is read from the entry
	 *            stored under {@link FhirConstants#CATEGORY_SEARCH_HANDLER}
	 * @param routingSystem the coding system that handlers in this resource family use to discriminate
	 *            categories (e.g. {@code CONDITION_CATEGORY_SYSTEM_URI})
	 * @param routingCode the calling handler's code in the routing system (e.g.
	 *            {@code "problem-list-item"} or {@code "encounter-diagnosis"})
	 * @return {@code true} if any AND clause of {@code category} references the routing system but
	 *         doesn't include the caller's code
	 */
	public static boolean routingCategoryExcludes(@Nonnull SearchParameterMap params, @Nonnull String routingSystem,
	        @Nonnull String routingCode) {
		return tokenParamExcludes(extractTokenAndListParam(params, FhirConstants.CATEGORY_SEARCH_HANDLER), routingSystem,
		    routingCode);
	}
	
	private static boolean tokenParamExcludes(TokenAndListParam tokenParam, String routingSystem, String routingCode) {
		if (tokenParam == null || tokenParam.size() == 0) {
			return false;
		}
		
		for (TokenOrListParam orList : tokenParam.getValuesAsQueryTokens()) {
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
	
	private static TokenAndListParam extractTokenAndListParam(SearchParameterMap params, String handlerKey) {
		for (Map.Entry<String, List<PropParam<?>>> entry : params.getParameters()) {
			if (!handlerKey.equals(entry.getKey())) {
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
