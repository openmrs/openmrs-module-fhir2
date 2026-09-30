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
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Static helpers for {@link FhirResourceHandler} implementations.
 * <p>
 * Each helper takes the caller's own coding system and code, never a list of the alternatives, so
 * that a backing contributed by another module needs no changes here.
 * <p>
 * On search, a handler opts out whenever an AND clause of a routing token (read from {@code _tag}
 * or from a domain-specific token search param such as {@code category}) fails to name the caller's
 * code. A token names the caller when its code matches and its system is either the routing system
 * or absent; the system-less form follows the FHIR token rule that {@code [code]} matches any
 * system. A clause naming only other codes or only other systems excludes the caller, because the
 * backings this module ships carry no tags or categories outside their routing system, so no
 * resource of theirs could satisfy such a clause.
 */
public final class HandlerSupport {
	
	private HandlerSupport() {
		// no instances
	}
	
	/**
	 * A {@link FhirResourceHandler#canHandle} predicate for backings marked by membership of a coding
	 * system, whatever the code — as the OpenMRS encounter-type and visit-type systems are.
	 *
	 * @param concepts the concepts to inspect (e.g. {@code Encounter.type}); may be empty
	 * @param system the coding system that marks a body as the caller's
	 */
	public static boolean hasCodingInSystem(@Nonnull List<CodeableConcept> concepts, @Nonnull String system) {
		return codings(concepts).anyMatch(coding -> system.equals(coding.getSystem()));
	}
	
	/**
	 * A {@link FhirResourceHandler#canHandle} predicate for backings that share a coding system and are
	 * distinguished by code, as the {@code Condition} categories are.
	 *
	 * @param concepts the concepts to inspect (e.g. {@code Condition.category}); may be empty
	 * @param system the shared coding system
	 * @param code the calling handler's code within that system
	 */
	public static boolean hasCoding(@Nonnull List<CodeableConcept> concepts, @Nonnull String system, @Nonnull String code) {
		return codings(concepts).anyMatch(coding -> system.equals(coding.getSystem()) && code.equals(coding.getCode()));
	}
	
	/**
	 * Returns whether the concepts carry no codings at all — neither any {@code CodeableConcept}, nor
	 * one with a populated {@code coding}. The handler that owns the unmarked case, where the client
	 * has named no backing, claims on this.
	 *
	 * @param concepts the concepts to inspect; may be empty
	 */
	public static boolean hasNoCodings(@Nonnull List<CodeableConcept> concepts) {
		return !codings(concepts).findAny().isPresent();
	}
	
	private static Stream<Coding> codings(List<CodeableConcept> concepts) {
		return concepts.stream().filter(CodeableConcept::hasCoding).flatMap(concept -> concept.getCoding().stream());
	}
	
	/**
	 * Returns whether a {@code _tag} parameter on the search request routes the request away from the
	 * caller. Every AND clause of {@code _tag} must admit the caller: a clause does so when one of its
	 * OR alternatives carries the caller's code, either in the routing system or with no system at all.
	 *
	 * @param params the search parameter map; the {@code _tag} parameter is read from the entry stored
	 *            under {@link FhirConstants#TAG_SEARCH_HANDLER}
	 * @param routingSystem the coding system that handlers in this resource family use to route search
	 *            requests (e.g. {@code OPENMRS_FHIR_EXT_ENCOUNTER_TAG})
	 * @param routingCode the calling handler's code in the routing system (e.g. {@code "encounter"} or
	 *            {@code "visit"})
	 * @return {@code true} if any AND clause of {@code _tag} fails to name the caller's code (meaning
	 *         the caller should opt out of the search)
	 */
	public static boolean routingTagExcludes(@Nonnull SearchParameterMap params, @Nonnull String routingSystem,
	        @Nonnull String routingCode) {
		return tokenParamExcludes(extractTokenAndListParam(params, FhirConstants.TAG_SEARCH_HANDLER), routingSystem,
		    routingCode);
	}
	
	/**
	 * Returns whether a {@code category} parameter on the search request routes the request away from
	 * the caller. Semantics mirror {@link #routingTagExcludes}: every AND clause must carry the
	 * caller's code, in the routing system or with no system.
	 *
	 * @param params the search parameter map; the {@code category} parameter is read from the entry
	 *            stored under {@link FhirConstants#CATEGORY_SEARCH_HANDLER}
	 * @param routingSystem the coding system that handlers in this resource family use to discriminate
	 *            categories (e.g. {@code CONDITION_CATEGORY_SYSTEM_URI})
	 * @param routingCode the calling handler's code in the routing system (e.g.
	 *            {@code "problem-list-item"} or {@code "encounter-diagnosis"})
	 * @return {@code true} if any AND clause of {@code category} fails to name the caller's code
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
			if (orList == null || orList.getValuesAsQueryTokens().isEmpty()) {
				continue;
			}
			
			boolean namesCaller = false;
			for (TokenParam token : orList.getValuesAsQueryTokens()) {
				if (token != null && namesCaller(token, routingSystem, routingCode)) {
					namesCaller = true;
					break;
				}
			}
			
			if (!namesCaller) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean namesCaller(TokenParam token, String routingSystem, String routingCode) {
		if (!routingCode.equals(token.getValue())) {
			return false;
		}
		
		String system = token.getSystem();
		return system == null || system.isEmpty() || routingSystem.equals(system);
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
