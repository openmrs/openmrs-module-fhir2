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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.transaction.annotation.Transactional;

public class TwoSearchQueryBundleProvider implements IBundleProvider {
	
	private final IBundleProvider firstProvider;
	
	private final IBundleProvider secondProvider;
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	private transient Integer pageSize;
	
	private transient Integer count;
	
	private final int firstProviderSize;
	
	private final int secondProviderSize;
	
	@Getter
	private final IPrimitiveType<Date> published;
	
	@Getter
	private final String uuid;
	
	public TwoSearchQueryBundleProvider(IBundleProvider firstProvider, IBundleProvider secondProvider,
	    FhirGlobalPropertyService globalPropertyService) {
		this.firstProvider = firstProvider;
		this.secondProvider = secondProvider;
		this.published = InstantDt.withCurrentTime();
		this.uuid = UUID.randomUUID().toString();
		this.globalPropertyService = globalPropertyService;
		this.firstProviderSize = Optional.ofNullable(firstProvider.size()).orElse(Integer.MAX_VALUE);
		this.secondProviderSize = Optional.ofNullable(secondProvider.size()).orElse(Integer.MAX_VALUE);
	}
	
	@Transactional(readOnly = true)
	@Nonnull
	@Override
	public List<IBaseResource> getResources(int fromIndex, int toIndex) {
		int firstResult = 0;
		if (fromIndex >= 0) {
			firstResult = fromIndex;
		}
		
		Integer size = size();
		if (size != null && firstResult > size) {
			return Collections.emptyList();
		}
		
		// NPE-safe unboxing
		int lastResult = Integer.MAX_VALUE;
		lastResult = size == null ? lastResult : size;
		
		if (toIndex - firstResult > 0) {
			lastResult = Math.min(lastResult, toIndex);
		}
		
		if (lastResult <= firstProviderSize) {
			return firstProvider.getResources(firstResult, lastResult);
		}
		
		if (firstResult >= firstProviderSize) {
			return secondProvider.getResources(firstResult - firstProviderSize, lastResult - firstProviderSize);
		}
		
		// case of intersection
		int numResultsFromFirstProvider = firstProviderSize - firstResult;
		int numResultsFromSecondProvider = lastResult - firstProviderSize;
		
		List<IBaseResource> resourcesFromFirstProvider = firstProvider.getResources(firstResult, firstProviderSize);
		List<IBaseResource> requiredResourcesFromFirstProvider = resourcesFromFirstProvider.subList(0,
		    numResultsFromFirstProvider);
		List<IBaseResource> includedResourcesFromFirstProvider = resourcesFromFirstProvider
		        .subList(numResultsFromFirstProvider, resourcesFromFirstProvider.size());
		
		List<IBaseResource> resourcesFromSecondProvider = secondProvider.getResources(0, numResultsFromSecondProvider);
		List<IBaseResource> requiredResourcesFromSecondProvider = resourcesFromSecondProvider.subList(0,
		    numResultsFromSecondProvider);
		List<IBaseResource> includedResourcesFromSecondProvider = resourcesFromSecondProvider
		        .subList(numResultsFromSecondProvider, resourcesFromSecondProvider.size());
		
		List<IBaseResource> resultList = new ArrayList<>();
		
		// resources to be added in the following way for every page -
		// resources from first -> resources from second -> included from first -> included from second
		resultList.addAll(requiredResourcesFromFirstProvider);
		resultList.addAll(requiredResourcesFromSecondProvider);
		resultList.addAll(includedResourcesFromFirstProvider);
		resultList.addAll(includedResourcesFromSecondProvider);
		
		return resultList;
	}
	
	@Override
	public Integer preferredPageSize() {
		if (pageSize == null) {
			pageSize = globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10);
		}
		
		return pageSize;
	}
	
	@Nullable
	@Override
	public Integer size() {
		if (count == null) {
			if (firstProviderSize == Integer.MAX_VALUE || secondProviderSize == Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			} else {
				count = firstProviderSize + secondProviderSize;
			}
			
			// accounting for integer overflow
			if (count < 0) {
				count = Integer.MAX_VALUE;
			}
		}
		
		return count;
	}
}
