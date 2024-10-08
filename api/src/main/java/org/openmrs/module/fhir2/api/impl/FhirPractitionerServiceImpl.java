/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.annotation.Nonnull;

import java.util.Collections;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.InternalCodingDt;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.TwoSearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.PractitionerSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirPractitionerServiceImpl extends BaseFhirService<Practitioner, Provider> implements FhirPractitionerService {
	
	@Autowired
	private FhirPractitionerDao dao;
	
	@Autowired
	private PractitionerTranslator<Provider> translator;
	
	@Autowired
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Autowired
	private SearchQuery<User, Practitioner, FhirUserDao, PractitionerTranslator<User>, SearchQueryInclude<Practitioner>> userSearchQuery;
	
	@Autowired
	private FhirUserService userService;
	
	@Override
	public Practitioner get(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		Practitioner result;
		try {
			result = super.get(uuid);
		}
		catch (ResourceNotFoundException e) {
			result = userService.get(uuid);
		}
		
		return result;
	}
	
	@Override
	public Practitioner create(@Nonnull Practitioner newResource) {
		if (!newResource.hasIdentifier()) {
			throw new UnprocessableEntityException("New providers must have at least one identifier");
		}
		
		return super.create(newResource);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForPractitioners(PractitionerSearchParams practitionerSearchParams) {
		
		IBundleProvider providerBundle = null;
		IBundleProvider userBundle = null;
		
		// TODO only search for providers if we have a provider roles tag
		if (shouldSearchExplicitlyFor(practitionerSearchParams.getTag(), "provider")) {
			providerBundle = searchQuery.getQueryResults(practitionerSearchParams.toSearchParameterMap(), dao, translator,
			    searchQueryInclude);
		}
		
		if (shouldSearchExplicitlyFor(practitionerSearchParams.getTag(), "user")) {
			SearchParameterMap theParams = new SearchParameterMap();
			userBundle = userService.searchForUsers(theParams);
		}
		
		if (providerBundle != null && userBundle != null) {
			return new TwoSearchQueryBundleProvider(providerBundle, userBundle, globalPropertyService);
		} else if (providerBundle == null && userBundle != null) {
			return userBundle;
		}
		
		return providerBundle;
	}
	
	protected boolean shouldSearchExplicitlyFor(TokenAndListParam tokenAndListParam, @Nonnull String valueToCheck) {
		if (tokenAndListParam == null || tokenAndListParam.size() == 0 || valueToCheck.isEmpty()) {
			return true;
		}
		
		return tokenAndListParam.getValuesAsQueryTokens().stream().anyMatch(
		    tokenOrListParam -> tokenOrListParam.doesCodingListMatch(Collections
		            .singletonList(new InternalCodingDt(FhirConstants.OPENMRS_FHIR_EXT_PRACTITIONER_TAG, valueToCheck))));
	}
}
