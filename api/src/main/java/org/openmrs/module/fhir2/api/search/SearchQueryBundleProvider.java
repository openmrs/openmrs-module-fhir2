/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.transaction.annotation.Transactional;

public class SearchQueryBundleProvider<T extends OpenmrsObject & Auditable, U extends IBaseResource> implements IBundleProvider, Serializable {
	
	private static final long serialVersionUID = 4L;
	
	private final FhirDao<T> dao;
	
	@Getter
	private final IPrimitiveType<Date> published;
	
	private final SearchParameterMap searchParameterMap;
	
	private final ToFhirTranslator<T, U> translator;
	
	@Getter
	private final String uuid;
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	private transient Integer count;
	
	private transient Integer pageSize;
	
	private transient List<String> matchingResourceUuids;
	
	private final SearchQueryInclude<U> searchQueryInclude;
	
	public SearchQueryBundleProvider(SearchParameterMap searchParameterMap, FhirDao<T> dao,
	    ToFhirTranslator<T, U> translator, FhirGlobalPropertyService globalPropertyService,
	    SearchQueryInclude<U> searchQueryInclude) {
		this.dao = dao;
		this.published = InstantDt.withCurrentTime();
		this.searchParameterMap = searchParameterMap;
		this.translator = translator;
		this.uuid = FhirUtils.newUuid();
		this.globalPropertyService = globalPropertyService;
		this.searchQueryInclude = searchQueryInclude;
	}
	
	@Transactional(readOnly = true)
	@Override
	@Nonnull
	public List<IBaseResource> getResources(int fromIndex, int toIndex) {
		if (matchingResourceUuids == null) {
			matchingResourceUuids = dao.getSearchResultUuids(searchParameterMap);
		}
		
		if (matchingResourceUuids.isEmpty()) {
			return Collections.emptyList();
		}
		
		int firstResult = 0;
		if (fromIndex >= 0) {
			firstResult = fromIndex;
		}
		
		// NPE-safe unboxing
		int lastResult = Integer.MAX_VALUE;
		Integer lastResultHolder = size();
		lastResult = lastResultHolder == null ? lastResult : lastResultHolder;
		
		if (toIndex - firstResult > 0) {
			lastResult = Math.min(lastResult, toIndex);
		}
		
		List<U> returnedResourceList = dao
		        .getSearchResults(searchParameterMap, matchingResourceUuids, firstResult, lastResult).stream()
		        .map(translator::toFhirResource).filter(Objects::nonNull).collect(Collectors.toList());
		
		Set<IBaseResource> includedResources = searchQueryInclude.getIncludedResources(returnedResourceList,
		    this.searchParameterMap);
		
		List<IBaseResource> resultList = new ArrayList<>(returnedResourceList);
		resultList.addAll(includedResources);
		
		return resultList;
	}
	
	@Override
	public Integer preferredPageSize() {
		if (pageSize == null) {
			pageSize = globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10);
		}
		
		return pageSize;
	}
	
	@Override
	@Nullable
	public Integer size() {
		if (matchingResourceUuids == null) {
			matchingResourceUuids = dao.getSearchResultUuids(searchParameterMap);
		}
		
		if (count == null) {
			count = matchingResourceUuids.size();
		}
		
		return count;
	}
}
