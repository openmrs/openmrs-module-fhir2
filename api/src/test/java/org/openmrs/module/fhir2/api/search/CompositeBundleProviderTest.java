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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

@RunWith(MockitoJUnitRunner.class)
public class CompositeBundleProviderTest {
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private IBundleProvider firstProvider;
	
	@Mock
	private IBundleProvider secondProvider;
	
	@Mock
	private IBundleProvider thirdProvider;
	
	@Before
	public void setUp() {
		// lenient — not every test exercises preferredPageSize()
		lenient().when(globalPropertyService.getGlobalPropertyAsInteger(anyString(), anyInt())).thenReturn(10);
	}
	
	@Test
	public void shouldRejectEmptyProviderList() {
		// given
		List<IBundleProvider> empty = Collections.emptyList();
		
		// when / then
		assertThrows(IllegalArgumentException.class, () -> new CompositeBundleProvider(empty, globalPropertyService));
	}
	
	@Test
	public void shouldRejectNullProviderList() {
		assertThrows(IllegalArgumentException.class, () -> new CompositeBundleProvider(null, globalPropertyService));
	}
	
	@Test
	public void shouldSumProviderSizes() {
		// given
		when(firstProvider.size()).thenReturn(3);
		when(secondProvider.size()).thenReturn(5);
		
		// when
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// then
		assertThat(provider.size(), equalTo(8));
	}
	
	@Test
	public void shouldReportUnknownSizeWhileAnyProviderSizeIsUnknown() {
		// given
		when(firstProvider.size()).thenReturn(3);
		when(secondProvider.size()).thenReturn(null);
		
		// when
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// then
		assertThat(provider.size(), nullValue());
	}
	
	@Test
	public void shouldUseGlobalPropertyForPreferredPageSize() {
		// given
		when(firstProvider.size()).thenReturn(1);
		CompositeBundleProvider provider = new CompositeBundleProvider(Collections.singletonList(firstProvider),
		        globalPropertyService);
		
		// when
		Integer pageSize = provider.preferredPageSize();
		
		// then
		assertThat(pageSize, equalTo(10));
	}
	
	@Test
	public void shouldExposePublishedDateAndUuid() {
		// given
		when(firstProvider.size()).thenReturn(1);
		
		// when
		CompositeBundleProvider provider = new CompositeBundleProvider(Collections.singletonList(firstProvider),
		        globalPropertyService);
		
		// then
		assertThat(provider.getPublished(), notNullValue());
		assertThat(provider.getPublished().getValue(), notNullValue());
		assertThat(provider.getUuid(), notNullValue());
	}
	
	@Test
	public void shouldGenerateDifferentUuidPerInstance() {
		// given
		when(firstProvider.size()).thenReturn(1);
		
		// when
		CompositeBundleProvider a = new CompositeBundleProvider(Collections.singletonList(firstProvider),
		        globalPropertyService);
		CompositeBundleProvider b = new CompositeBundleProvider(Collections.singletonList(firstProvider),
		        globalPropertyService);
		
		// then
		assertThat(a.getUuid(), notNullValue());
		assertThat(b.getUuid(), notNullValue());
		assertThat(a.getUuid().equals(b.getUuid()), equalTo(false));
	}
	
	@Test
	public void shouldDelegateVerbatimWhenPageWithinSingleProvider() {
		// given — page [0, 3) is entirely within firstProvider (size 5)
		when(firstProvider.size()).thenReturn(5);
		when(secondProvider.size()).thenReturn(4);
		List<IBaseResource> firstSlice = patientList("a", "b", "c");
		when(firstProvider.getResources(0, 3)).thenReturn(firstSlice);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(0, 3);
		
		// then
		assertThat(result, equalTo(firstSlice));
		verify(secondProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldDelegateWithShiftedIndicesWhenPageWithinSecondProvider() {
		// given — page [5, 8) is entirely within secondProvider; local [0, 3)
		when(firstProvider.size()).thenReturn(5);
		when(secondProvider.size()).thenReturn(4);
		List<IBaseResource> secondSlice = patientList("d", "e", "f");
		when(secondProvider.getResources(0, 3)).thenReturn(secondSlice);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(5, 8);
		
		// then
		assertThat(result, equalTo(secondSlice));
		verify(firstProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldConcatenateMainsThenIncludesWhenSpanningTwoProviders() {
		// given — providers of size 3 and 4. Page [1, 5) takes 2 from p1 (with 1 include) and 2 from p2 (with 1 include).
		when(firstProvider.size()).thenReturn(3);
		when(secondProvider.size()).thenReturn(4);
		
		List<IBaseResource> firstChunk = patientList("a2", "a3", "i_a");
		List<IBaseResource> secondChunk = patientList("b1", "b2", "i_b");
		when(firstProvider.getResources(1, 3)).thenReturn(firstChunk);
		when(secondProvider.getResources(0, 2)).thenReturn(secondChunk);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(1, 5);
		
		// then — mains-from-p1, mains-from-p2, includes-from-p1, includes-from-p2
		assertThat(idsOf(result), contains("a2", "a3", "b1", "b2", "i_a", "i_b"));
	}
	
	@Test
	public void shouldConcatenateAcrossThreeProvidersWhenSpanningMultipleBoundaries() {
		// given — sizes 2, 2, 2. Page [1, 5) takes 1 from p1, 2 from p2, 1 from p3.
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(2);
		when(thirdProvider.size()).thenReturn(2);
		
		when(firstProvider.getResources(1, 2)).thenReturn(patientList("a2"));
		when(secondProvider.getResources(0, 2)).thenReturn(patientList("b1", "b2"));
		when(thirdProvider.getResources(0, 1)).thenReturn(patientList("c1"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(firstProvider, secondProvider, thirdProvider), globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(1, 5);
		
		// then
		assertThat(idsOf(result), contains("a2", "b1", "b2", "c1"));
	}
	
	@Test
	public void shouldReturnEmptyWhenPageStartsBeyondTotalSize() {
		// given — total size 4, page starts at 10
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(2);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(10, 20);
		
		// then
		assertThat(result, empty());
		verify(firstProvider, never()).getResources(anyInt(), anyInt());
		verify(secondProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldClampPageToProviderSizes() {
		// given — total size 5, page asks for [3, 100)
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(3);
		when(secondProvider.getResources(1, 3)).thenReturn(patientList("b2", "b3"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		// when
		List<IBaseResource> result = provider.getResources(3, 100);
		
		// then
		assertThat(result, hasSize(2));
		assertThat(idsOf(result), contains("b2", "b3"));
	}
	
	@Test
	public void shouldReturnEverythingFromStartWhenToIndexIsNotAfterFromIndex() {
		// toIndex <= fromIndex means "all remaining", the way HAPI's getAllResources() asks for a page.
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(2);
		when(firstProvider.getResources(1, 2)).thenReturn(patientList("a2"));
		when(secondProvider.getResources(0, 2)).thenReturn(patientList("b1", "b2"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		assertThat(idsOf(provider.getResources(1, 0)), contains("a2", "b1", "b2"));
		assertThat(idsOf(provider.getResources(1, 1)), contains("a2", "b1", "b2"));
	}
	
	@Test
	public void shouldReturnEmptyWhenPageStartsExactlyAtTotalSize() {
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(2);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		assertThat(provider.getResources(4, 10), empty());
		verify(firstProvider, never()).getResources(anyInt(), anyInt());
		verify(secondProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldSkipZeroSizeProviderAtTheFront() {
		when(firstProvider.size()).thenReturn(0);
		when(secondProvider.size()).thenReturn(2);
		when(thirdProvider.size()).thenReturn(2);
		when(secondProvider.getResources(0, 2)).thenReturn(patientList("b1", "b2"));
		when(thirdProvider.getResources(0, 1)).thenReturn(patientList("c1"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(firstProvider, secondProvider, thirdProvider), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(0, 3)), contains("b1", "b2", "c1"));
		verify(firstProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldSkipZeroSizeProviderInTheMiddle() {
		when(firstProvider.size()).thenReturn(2);
		when(secondProvider.size()).thenReturn(0);
		when(thirdProvider.size()).thenReturn(2);
		when(firstProvider.getResources(1, 2)).thenReturn(patientList("a2"));
		when(thirdProvider.getResources(0, 1)).thenReturn(patientList("c1"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(firstProvider, secondProvider, thirdProvider), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(1, 3)), contains("a2", "c1"));
		verify(secondProvider, never()).getResources(anyInt(), anyInt());
	}
	
	@Test
	public void shouldPassIncludesThroughVerbatimOnSinglePageFastPath() {
		when(firstProvider.size()).thenReturn(5);
		when(secondProvider.size()).thenReturn(4);
		List<IBaseResource> chunkWithIncludes = patientList("a1", "a2", "i_a");
		when(firstProvider.getResources(0, 2)).thenReturn(chunkWithIncludes);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		assertThat(provider.getResources(0, 2), equalTo(chunkWithIncludes));
	}
	
	@Test
	public void shouldTolerateChunkShorterThanPromisedWhenSpanningProviders() {
		// A row voided between the count and the fetch makes a provider return fewer rows than size() said.
		when(firstProvider.size()).thenReturn(3);
		when(secondProvider.size()).thenReturn(2);
		when(firstProvider.getResources(1, 3)).thenReturn(patientList("a2"));
		when(secondProvider.getResources(0, 2)).thenReturn(patientList("b1", "b2", "i_b"));
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		assertThat(idsOf(provider.getResources(1, 5)), contains("a2", "b1", "b2", "i_b"));
	}
	
	@Test
	public void shouldPageSequentiallyPastAnUnknownSizeProvider() {
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(new UnsizedProvider(false, "a1", "a2", "a3"), sized("b1", "b2")), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(0, 2)), contains("a1", "a2"));
		assertThat(provider.size(), nullValue());
		assertThat(idsOf(provider.getResources(2, 4)), contains("a3", "b1"));
		assertThat(idsOf(provider.getResources(4, 6)), contains("b2"));
		assertThat(provider.size(), equalTo(5));
	}
	
	@Test
	public void shouldMeasureAnUnknownSizeProviderWhenAPageStartsBeyondIt() {
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(new UnsizedProvider(false, "a1", "a2", "a3"), sized("b1", "b2")), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(4, 6)), contains("b2"));
		assertThat(provider.size(), equalTo(5));
	}
	
	@Test
	public void shouldNotCountIncludesAsMatchesWhereAnUnknownSizeProviderEnds() {
		// The page asks a1..a3 for two matches but only one remains, so its chunk is [a3, i_a3]: a short
		// chunk's length says nothing about how many entries are matches.
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(new UnsizedProvider(true, "a1", "a2", "a3"), sized("b1", "b2")), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(2, 4)), contains("a3", "b1", "i_a3"));
		assertThat(provider.size(), equalTo(5));
	}
	
	@Test
	public void shouldReachEveryProviderAfterSeveralOfUnknownSize() {
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(new UnsizedProvider(false, "a1", "a2"),
		    new UnsizedProvider(false, "b1", "b2", "b3"), sized("c1")), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(3, 6)), contains("b2", "b3", "c1"));
		assertThat(provider.size(), equalTo(6));
	}
	
	@Test
	public void shouldReturnEverythingForAnOpenEndedPageAcrossAnUnknownSizeProvider() {
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(new UnsizedProvider(false, "a1", "a2", "a3"), sized("b1", "b2")), globalPropertyService);
		
		assertThat(idsOf(provider.getResources(0, 0)), contains("a1", "a2", "a3", "b1", "b2"));
		assertThat(provider.size(), equalTo(5));
	}
	
	@Test
	public void shouldReturnEmptyBeyondTheEndOfAnUnknownSizeLastProvider() {
		CompositeBundleProvider provider = new CompositeBundleProvider(
		        Arrays.asList(sized("a1"), new UnsizedProvider(false, "b1", "b2")), globalPropertyService);
		
		assertThat(provider.getResources(3, 5), empty());
		assertThat(provider.size(), equalTo(3));
	}
	
	@Test
	public void shouldProbeAnUnknownSizeProviderAboutOncePerSequentialPage() {
		String[] ids = IntStream.range(0, 95).mapToObj(i -> "a" + i).toArray(String[]::new);
		UnsizedProvider unsized = new UnsizedProvider(false, ids);
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(unsized, sized("b1")),
		        globalPropertyService);
		
		List<String> seen = new ArrayList<>();
		for (int from = 0; from < 100; from += 10) {
			seen.addAll(idsOf(provider.getResources(from, from + 10)));
		}
		
		assertThat(seen, hasSize(96));
		assertThat(seen.get(95), equalTo("b1"));
		// a fetch and a probe per page, plus a handful of probes to find where the provider ends
		assertThat(unsized.calls, lessThanOrEqualTo(2 * 10 + 8));
	}
	
	@Test
	public void shouldReportMaxValueWhenFiniteSizesOverflow() {
		when(firstProvider.size()).thenReturn(Integer.MAX_VALUE - 1);
		when(secondProvider.size()).thenReturn(2);
		
		CompositeBundleProvider provider = new CompositeBundleProvider(Arrays.asList(firstProvider, secondProvider),
		        globalPropertyService);
		
		assertThat(provider.size(), equalTo(Integer.MAX_VALUE));
	}
	
	@Test
	public void shouldCachePreferredPageSize() {
		when(firstProvider.size()).thenReturn(1);
		CompositeBundleProvider provider = new CompositeBundleProvider(Collections.singletonList(firstProvider),
		        globalPropertyService);
		
		provider.preferredPageSize();
		provider.preferredPageSize();
		
		verify(globalPropertyService).getGlobalPropertyAsInteger(anyString(), anyInt());
	}
	
	private static IBundleProvider sized(String... ids) {
		return new SimpleBundleProvider(patientList(ids));
	}
	
	private static List<IBaseResource> patientList(String... ids) {
		return Arrays.stream(ids).map(id -> {
			Patient p = new Patient();
			p.setId(id);
			return (IBaseResource) p;
		}).collect(Collectors.toList());
	}
	
	private static List<String> idsOf(List<IBaseResource> resources) {
		return IntStream.range(0, resources.size()).mapToObj(i -> resources.get(i).getIdElement().getIdPart())
		        .collect(Collectors.toList());
	}
	
	/**
	 * Reports no size, as a contributed handler's bundle may. Each page holds its matches followed, if
	 * {@code withIncludes}, by one {@code i_<id>} include per match.
	 */
	private static class UnsizedProvider implements IBundleProvider {
		
		private final List<String> matches;
		
		private final boolean withIncludes;
		
		private int calls;
		
		UnsizedProvider(boolean withIncludes, String... matches) {
			this.matches = Arrays.asList(matches);
			this.withIncludes = withIncludes;
		}
		
		@Nonnull
		@Override
		public List<IBaseResource> getResources(int fromIndex, int toIndex) {
			calls++;
			List<String> page = fromIndex >= matches.size() ? Collections.emptyList()
			        : matches.subList(fromIndex, Math.min(toIndex, matches.size()));
			
			List<String> ids = new ArrayList<>(page);
			if (withIncludes) {
				page.forEach(id -> ids.add("i_" + id));
			}
			return patientList(ids.toArray(new String[0]));
		}
		
		@Override
		public Integer size() {
			return null;
		}
		
		@Override
		public IPrimitiveType<Date> getPublished() {
			return null;
		}
		
		@Override
		public String getUuid() {
			return null;
		}
		
		@Override
		public Integer preferredPageSize() {
			return null;
		}
	}
}
