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
 * provider's reported size, saturating at {@link Integer#MAX_VALUE}.
 * <p>
 * A provider whose {@link IBundleProvider#size()} is {@code null} is measured while paging: where a
 * page reaches into it, single-result probes find out whether it extends past the page and, once it
 * does not, exactly where it ends, so later providers are still reached. Until every provider's
 * size is known, {@link #size()} is {@code null} too. Sequential paging costs about one extra probe
 * per page; a jump far beyond what has been seen of such a provider costs a logarithmic number of
 * probes. Probing relies on the provider returning an empty page past its last result, since a page
 * carries {@code _include} resources only alongside matches.
 * <p>
 * When a paged response spans more than one provider, this class splits each provider's chunk into
 * "main" results and "_include" results by assuming the chunk is laid out as
 * {@code [main_0, ..., main_{n-1}, include_0, ...]} where {@code n = sliceEnd - sliceStart}.
 * {@code SearchQueryBundleProvider} (the standard backing for handler searches) honours this
 * contract; an underlying {@link IBundleProvider} that interleaves mains and includes within
 * {@link IBundleProvider#getResources(int, int)} would defeat the split. Handler implementations
 * passing custom bundle providers into the composite must follow this layout.
 */
public class CompositeBundleProvider implements IBundleProvider {
	
	private static final int UNKNOWN = -1;
	
	private final List<IBundleProvider> providers;
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	/** Each provider's size, or {@link #UNKNOWN} until paging has measured it. */
	private final int[] providerSizes;
	
	/** For a provider of unknown size, how many results it is known to have at least. */
	private final int[] knownMinimums;
	
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
		this.knownMinimums = new int[this.providers.size()];
		for (int i = 0; i < this.providers.size(); i++) {
			Integer size = this.providers.get(i).size();
			this.providerSizes[i] = size == null ? UNKNOWN : size;
		}
	}
	
	/**
	 * Synchronized because measuring a provider of unknown size updates state that the paging provider
	 * may share between concurrent page requests.
	 */
	@Transactional(readOnly = true)
	@Nonnull
	@Override
	public synchronized List<IBaseResource> getResources(int fromIndex, int toIndex) {
		int firstResult = Math.max(fromIndex, 0);
		
		// toIndex <= fromIndex asks for everything from fromIndex on
		long lastResult = toIndex > firstResult ? toIndex : Long.MAX_VALUE;
		
		Integer knownTotal = size();
		if (knownTotal != null) {
			lastResult = Math.min(lastResult, knownTotal);
		}
		
		if (firstResult >= lastResult) {
			return Collections.emptyList();
		}
		
		// Walk providers and record the local slice each one needs to contribute.
		int[] localFrom = new int[providers.size()];
		int[] localTo = new int[providers.size()];
		int firstHit = -1;
		int lastHit = -1;
		
		long cum = 0;
		for (int i = 0; i < providers.size() && cum < lastResult; i++) {
			long providerStart = cum;
			long providerEnd = providerStart + extentBelow(i, (int) Math.min(lastResult - providerStart, Integer.MAX_VALUE));
			
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
	
	/**
	 * @return the total number of matches, or {@code null} while any provider's size is still unknown
	 */
	@Nullable
	@Override
	public synchronized Integer size() {
		if (total == null) {
			long sum = 0;
			for (int s : providerSizes) {
				if (s == UNKNOWN) {
					return null;
				}
				sum += s;
			}
			total = (int) Math.min(sum, Integer.MAX_VALUE);
		}
		
		return total;
	}
	
	/**
	 * Returns how many of provider {@code i}'s results lie below the local index {@code limit}, i.e.
	 * {@code min(size, limit)}, measuring a provider of unknown size as far as that requires.
	 */
	private int extentBelow(int i, int limit) {
		if (providerSizes[i] != UNKNOWN) {
			return Math.min(providerSizes[i], limit);
		}
		
		if (limit <= knownMinimums[i] || hasResultAt(i, limit - 1)) {
			return limit;
		}
		
		providerSizes[i] = measure(i, limit - 1);
		return providerSizes[i];
	}
	
	/**
	 * Finds the size of provider {@code i}, given that it has no result at local index
	 * {@code absentAt}: gallops up from the results already known to exist to bracket the end, then
	 * binary-searches the bracket.
	 */
	private int measure(int i, int absentAt) {
		int present = knownMinimums[i] - 1;
		int absent = absentAt;
		
		for (long step = 1; present + step < absent; step <<= 1) {
			int probe = (int) (present + step);
			if (!hasResultAt(i, probe)) {
				absent = probe;
				break;
			}
			present = probe;
		}
		
		while (absent - present > 1) {
			int mid = present + (absent - present) / 2;
			if (hasResultAt(i, mid)) {
				present = mid;
			} else {
				absent = mid;
			}
		}
		
		return present + 1;
	}
	
	private boolean hasResultAt(int i, int index) {
		List<IBaseResource> page = providers.get(i).getResources(index, index + 1);
		if (page == null || page.isEmpty()) {
			return false;
		}
		
		knownMinimums[i] = Math.max(knownMinimums[i], index + 1);
		return true;
	}
}
