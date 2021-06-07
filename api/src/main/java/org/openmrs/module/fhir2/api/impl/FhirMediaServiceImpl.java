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

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMediaService;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MediaTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.MODULE)
public class FhirMediaServiceImpl extends BaseFhirService<Media, Obs> implements FhirMediaService {
	
	@Autowired
	private FhirMediaDao dao;
	
	@Autowired
	private MediaTranslator translator;
	
	@Autowired
	private SearchQuery<Obs, Media, FhirMediaDao, MediaTranslator, SearchQueryInclude<Media>> searchQuery;
	
	@Autowired
	private SearchQueryInclude<Media> searchQueryInclude;

	@Override
	public IBundleProvider searchForMedia(TokenAndListParam status, TokenAndListParam type, ReferenceAndListParam subject,
	        ReferenceAndListParam encounterReference, DateRangeParam createdDateTime, TokenAndListParam contentType,
	        TokenAndListParam id, StringAndListParam contentDataType, StringAndListParam contentTitle,
	        DateRangeParam contentCreated, DateRangeParam lastUpdated, HashSet<Include> includes,
	        HashSet<Include> revIncludes, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.MEDIA_STATUS, status)
		        .addParameter(FhirConstants.MEDIA_TYPE, type).addParameter(FhirConstants.MEDIA_SUBJECT, subject)
		        .addParameter(FhirConstants.MEDIA_ENCOUNTER_REFERENCE, encounterReference)
		        .addParameter(FhirConstants.MEDIA_CREATED_DATE_TIME, createdDateTime)
		        .addParameter(FhirConstants.MEDIA_CONTENT_TYPE, contentType)
		        .addParameter(FhirConstants.CONTENT_DATA, contentDataType)
		        .addParameter(FhirConstants.CONTENT_TITLE, contentTitle)
		        .addParameter(FhirConstants.CONTENT_DATE_OF_CREATION, contentCreated)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, includes)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, revIncludes).setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
