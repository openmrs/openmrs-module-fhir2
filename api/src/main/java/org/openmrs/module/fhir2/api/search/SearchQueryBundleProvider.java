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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;

public class SearchQueryBundleProvider<T extends OpenmrsObject & Auditable, U extends IBaseResource> implements IBundleProvider, Serializable {
	
	private static final long serialVersionUID = 3L;
	
	private final FhirDao<T> dao;
	
	private final Date datePublished;
	
	private final SearchParameterMap theParams;
	
	private final ToFhirTranslator<T, U> translator;
	
	private final UUID uuid;
	
	private transient Integer count;
	
	private transient List<String> matchingResourceUuids;
	
	private final SearchQueryInclude<U> searchQueryInclude;
	
	public SearchQueryBundleProvider(SearchParameterMap theParams, FhirDao<T> dao, ToFhirTranslator<T, U> translator,
	    SearchQueryInclude<U> searchQueryInclude) {
		this.dao = dao;
		this.datePublished = new Date();
		this.theParams = theParams;
		this.translator = translator;
		this.uuid = UUID.randomUUID();
		this.searchQueryInclude = searchQueryInclude;
	}
	
	@Override
	public IPrimitiveType<Date> getPublished() {
		return new InstantType(datePublished);
	}
	
	@Nonnull
	@Override
	public List<IBaseResource> getResources(int fromIndex, int toIndex) {
		if (this.matchingResourceUuids == null) {
			this.matchingResourceUuids = dao.getResultUuids(theParams);
		}
		
		int firstResult = 0;
		if (fromIndex >= 0) {
			firstResult = fromIndex;
		}
		
		int lastResult = this.size();
		if (toIndex - firstResult > 0) {
			lastResult = Math.min(lastResult, toIndex);
		}
		
		List<U> returnedResourceList = dao.search(this.theParams, this.matchingResourceUuids, firstResult, lastResult)
		        .stream().map(translator::toFhirResource).collect(Collectors.toList());
		
		Set<IBaseResource> includedResources = searchQueryInclude.getIncludedResources(returnedResourceList, this.theParams);
		
		List<IBaseResource> pagedResultList = new ArrayList<>(returnedResourceList);
		pagedResultList.addAll(includedResources);
		
		return pagedResultList;
	}
	
	@Nullable
	@Override
	public String getUuid() {
		return uuid.toString();
	}
	
	@Override
	public Integer preferredPageSize() {
		return dao.getPreferredPageSize();
	}
	
	@Nullable
	@Override
	public Integer size() {
		if (this.matchingResourceUuids == null) {
			this.matchingResourceUuids = dao.getResultUuids(theParams);
		}
		
		if (count == null) {
			count = this.matchingResourceUuids.size();
		}
		
		return count;
	}
}
