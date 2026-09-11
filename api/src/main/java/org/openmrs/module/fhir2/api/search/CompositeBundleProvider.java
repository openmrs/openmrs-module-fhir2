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

import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * An N-ary {@link IBundleProvider} that concatenates results from a list of providers in order,
 * preserving each provider's internal sort and grouping all "main" resources before any "_include"
 * resources (so a paged response interleaves main results from successive providers before any
 * included resources).
 * <p>
 * Cross-provider sorting is intentionally not supported — each provider sorts its own slice and the
 * slices are concatenated in declaration order. The total reported size is the sum of each
 * provider's reported size; if any provider reports an unknown size the total is reported as
 * {@link Integer#MAX_VALUE}.
 * <p>
 * <b>Mains-then-includes ordering contract.</b> When a paged response spans more than one provider,
 * this class splits each provider's chunk into "main" results and "_include" results by assuming
 * the chunk is laid out as {@code [main_0, ..., main_{n-1}, include_0, ...]} where
 * {@code n = sliceEnd - sliceStart}. {@code SearchQueryBundleProvider} (the standard backing for
 * handler searches) honours this contract; an underlying {@link IBundleProvider} that interleaves
 * mains and includes within {@link IBundleProvider#getResources(int, int)} would defeat the split.
 * Handler implementations passing custom bundle providers into the composite must follow this
 * layout.
 */
public class CompositeBundleProvider implements IBundleProvider {
	
	private final List<IBundleProvider> providers;
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	private final int[] providerSizes;
	
	@Getter
	private final IPrimitiveType<Date> published;
	
	@Getter
	private final String uuid;
	
	private transient Integer pageSize;
	
	private transient Integer total;
	
	public CompositeBundleProvider(@Nonnull List<? extends IBundleProvider> providers,
	    FhirGlobalPropertyService globalPropertyService) {
		if (providers == null || providers.isEmpty()) {
			throw new IllegalArgumentException("providers must be non-null and non-empty");
		}
		
		this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
		this.globalPropertyService = globalPropertyService;
		this.published = InstantDt.withCurrentTime();
		this.uuid = FhirUtils.newUuid();
		
		this.providerSizes = new int[this.providers.size()];
		for (int i = 0; i < this.providers.size(); i++) {
			this.providerSizes[i] = Optional.ofNullable(this.providers.get(i).size()).orElse(Integer.MAX_VALUE);
		}
	}
	
	@Transactional(readOnly = true)
	@Nonnull
	@Override
	public List<IBaseResource> getResources(int fromIndex, int toIndex) {
		int firstResult = Math.max(fromIndex, 0);
		
		Integer total = size();
		int absoluteEnd = (total == null) ? Integer.MAX_VALUE : total;
		
		int lastResult = absoluteEnd;
		if (toIndex - firstResult > 0) {
			lastResult = Math.min(lastResult, toIndex);
		}
		
		if (firstResult >= absoluteEnd || firstResult >= lastResult) {
			return Collections.emptyList();
		}
		
		// Walk providers and record the local slice each one needs to contribute.
		int[] localFrom = new int[providers.size()];
		int[] localTo = new int[providers.size()];
		int firstHit = -1;
		int lastHit = -1;
		
		long cum = 0;
		for (int i = 0; i < providers.size(); i++) {
			long size = providerSizes[i];
			long providerStart = cum;
			long providerEnd = (size == Integer.MAX_VALUE) ? Integer.MAX_VALUE : cum + size;
			
			long sliceStart = Math.max(firstResult, providerStart);
			long sliceEnd = Math.min(lastResult, providerEnd);
			
			if (sliceStart < sliceEnd) {
				if (firstHit < 0) {
					firstHit = i;
				}
				lastHit = i;
				localFrom[i] = (int) (sliceStart - providerStart);
				localTo[i] = (int) (sliceEnd - providerStart);
			}
			
			cum = providerEnd;
			if (cum >= lastResult) {
				break;
			}
		}
		
		if (firstHit < 0) {
			return Collections.emptyList();
		}
		
		// Fast path: page is entirely within one provider — return its slice verbatim, preserving
		// any of its own _include ordering.
		if (firstHit == lastHit) {
			return providers.get(firstHit).getResources(localFrom[firstHit], localTo[firstHit]);
		}
		
		// Cross-provider: split each chunk into mains and includes and concatenate
		// (mains-from-p0, mains-from-p1, ..., includes-from-p0, includes-from-p1, ...).
		List<IBaseResource> mains = new ArrayList<>();
		List<IBaseResource> includes = new ArrayList<>();
		
		for (int i = firstHit; i <= lastHit; i++) {
			if (localFrom[i] == localTo[i]) {
				continue;
			}
			
			List<IBaseResource> chunk = providers.get(i).getResources(localFrom[i], localTo[i]);
			if (chunk == null || chunk.isEmpty()) {
				continue;
			}
			
			int requestedMains = localTo[i] - localFrom[i];
			int actualMains = Math.min(requestedMains, chunk.size());
			mains.addAll(chunk.subList(0, actualMains));
			if (chunk.size() > actualMains) {
				includes.addAll(chunk.subList(actualMains, chunk.size()));
			}
		}
		
		List<IBaseResource> result = new ArrayList<>(mains.size() + includes.size());
		result.addAll(mains);
		result.addAll(includes);
		return result;
	}
	
	@Override
	public Integer preferredPageSize() {
		if (pageSize == null) {
			pageSize = globalPropertyService.getGlobalPropertyAsInteger(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10);
		}
		
		return pageSize;
	}
	
	@Nullable
	@Override
	public Integer size() {
		if (total == null) {
			long sum = 0;
			for (int s : providerSizes) {
				if (s == Integer.MAX_VALUE) {
					return Integer.MAX_VALUE;
				}
				sum += s;
				if (sum > Integer.MAX_VALUE) {
					return Integer.MAX_VALUE;
				}
			}
			total = (int) sum;
		}
		
		return total;
	}
}
