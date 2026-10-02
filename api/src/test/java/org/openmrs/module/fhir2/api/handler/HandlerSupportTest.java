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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public class HandlerSupportTest {
	
	private static final String SYSTEM = FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG;
	
	private static final String OTHER_SYSTEM = "http://example.org/some-other-system";
	
	private static final String CODE = "encounter";
	
	private static final String OTHER_CODE = "visit";
	
	@Test
	public void shouldNotExcludeWhenNoTagParamPresent() {
		assertFalse(HandlerSupport.routingTagExcludes(new SearchParameterMap(), SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenTagParamIsEmpty() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam());
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenTagIsInUnrelatedSystem() {
		// No resource of this backing carries a tag in another system, so the clause cannot be satisfied.
		SearchParameterMap params = paramsWithTag(new TokenParam(OTHER_SYSTEM, "anything"));
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenTagInUnrelatedSystemCarriesMyCode() {
		SearchParameterMap params = paramsWithTag(new TokenParam(OTHER_SYSTEM, CODE));
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenSystemlessTagCarriesMyCode() {
		// _tag=encounter: a token without a system matches any system, so it routes by code alone.
		SearchParameterMap params = paramsWithTag(new TokenParam(null, CODE));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenSystemlessTagCarriesAnotherCode() {
		SearchParameterMap params = paramsWithTag(new TokenParam(null, OTHER_CODE));
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldTreatEmptySystemLikeNoSystem() {
		// _tag=|encounter
		SearchParameterMap params = paramsWithTag(new TokenParam("", CODE));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldSkipEmptyAndClauses() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenOrListParam()));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenTagSystemAndCodeBothMatch() {
		SearchParameterMap params = paramsWithTag(new TokenParam(SYSTEM, CODE));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenTagSystemMatchesButCodeDiffers() {
		SearchParameterMap params = paramsWithTag(new TokenParam(SYSTEM, OTHER_CODE));
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenOrListContainsMyCode() {
		// _tag = encounter-tag|encounter,encounter-tag|visit  (single OR clause)
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(SYSTEM, OTHER_CODE);
		orList.add(SYSTEM, CODE);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenOrListInRoutingSystemHasNoMatchingCode() {
		// All alternatives are in the routing system but none is mine.
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(SYSTEM, OTHER_CODE);
		orList.add(SYSTEM, "yet-another-code");
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenAnyAndClauseIsInUnrelatedSystem() {
		// _tag = encounter-tag|encounter AND other-system|whatever
		// Every AND clause must be satisfiable by this backing; the second one is not.
		TokenAndListParam tag = new TokenAndListParam();
		tag.addAnd(new TokenParam(SYSTEM, CODE));
		tag.addAnd(new TokenParam(OTHER_SYSTEM, "something"));
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER, tag);
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenOrListMixesUnrelatedSystemWithMyCode() {
		// _tag = other-system|whatever,encounter-tag|encounter  (single OR clause)
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(OTHER_SYSTEM, "whatever");
		orList.add(SYSTEM, CODE);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldExcludeWhenAnyAndClauseInRoutingSystemHasWrongCode() {
		// _tag = encounter-tag|encounter AND encounter-tag|visit
		// The first clause admits us. The second clause excludes us.
		TokenAndListParam tag = new TokenAndListParam();
		tag.addAnd(new TokenParam(SYSTEM, CODE));
		tag.addAnd(new TokenParam(SYSTEM, OTHER_CODE));
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER, tag);
		
		assertTrue(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	@Test
	public void shouldNotExcludeWhenSearchParameterMapHasNoTagEntry() {
		// SearchParameterMap can hold many parameter keys — only TAG_SEARCH_HANDLER should be read.
		SearchParameterMap params = new SearchParameterMap().addParameter("UNRELATED_KEY",
		    new TokenAndListParam().addAnd(new TokenParam(SYSTEM, OTHER_CODE)));
		assertFalse(HandlerSupport.routingTagExcludes(params, SYSTEM, CODE));
	}
	
	private static SearchParameterMap paramsWithTag(TokenParam token) {
		return new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(token));
	}
	
	// ---- routingCategoryExcludes mirrors routingTagExcludes; one test per branch ----
	
	@Test
	public void categoryShouldNotExcludeWhenNoCategoryParamPresent() {
		assertFalse(HandlerSupport.routingCategoryExcludes(new SearchParameterMap(),
		    FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
	
	@Test
	public void categoryShouldNotExcludeWhenSystemAndCodeBothMatch() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_CONDITION)));
		assertFalse(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
	
	@Test
	public void categoryShouldExcludeWhenSystemMatchesButCodeDiffers() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)));
		assertTrue(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
	
	@Test
	public void categoryShouldNotExcludeWhenOrListContainsMyCode() {
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertFalse(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
	
	@Test
	public void categoryShouldRouteBySystemlessCode() {
		// Condition?category=encounter-diagnosis (no system) routes to the diagnosis backing only.
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(null, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)));
		assertTrue(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		assertFalse(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
	}
	
	@Test
	public void categoryShouldExcludeEveryoneWhenInUnrelatedSystem() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam("http://example.org/some-other-system", "value")));
		assertTrue(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		assertTrue(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
	}
	
	@Test
	public void categoryShouldIgnoreUnrelatedTagParam() {
		// Even if a _tag clause routes against us, routingCategoryExcludes only reads CATEGORY_SEARCH_HANDLER.
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)));
		assertFalse(HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
}
