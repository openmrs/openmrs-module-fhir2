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
import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.CompositeBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.ProfileRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

/**
 * Base class for services whose FHIR resource type {@code R} may be backed by more than one OpenMRS
 * domain object. Spring injects the {@link FhirResourceHandler}s registered for {@code R}, which
 * are sorted by {@code @Order} priority, and each {@link FhirService} call is dispatched to one or
 * more of them.
 * <h2>Dispatch</h2>
 * <ul>
 * <li>{@code get(uuid)} tries each handler in priority order. A handler's
 * {@link ResourceNotFoundException} moves on to the next; a
 * {@link ca.uhn.fhir.rest.server.exceptions.ResourceGoneException} propagates.
 * <li>{@code get(uuids)} aggregates the results of every handler.
 * <li>{@code update}, {@code patch} and {@code delete} go to the first handler whose
 * {@link FhirService#exists(String) exists} reports the UUID, and fail with
 * {@link ResourceNotFoundException} if none does. Ownership is resolved before the body is
 * validated, so an unknown UUID is a 404 even when the body's id is wrong; a known UUID with a bad
 * id is still a 400.
 * <li>{@code create}, and {@code update} with {@code createIfNotExists} when no handler owns the
 * UUID, go to the handler whose {@link FhirResourceHandler#getImplicitProfile() implicit profile}
 * appears in {@code meta.profile}. Failing that, they go to the handler whose
 * {@link FhirResourceHandler#canHandle(IAnyResource) canHandle} claims the body, with several
 * claimants resolved by {@code @Order} unless {@link #resolvesOverlappingClaimsByOrder()} is
 * {@code false}. A tie at the top, or no claimant at all, is an {@link InvalidRequestException}; no
 * registered handlers is a {@link NotImplementedOperationException}. A handler chosen by profile is
 * used unconditionally and may itself reject a body it cannot store.
 * <li>{@link #doSearch(SearchParameterMap) Searches} go to the handlers named by the request's
 * {@code _profile} (see {@link ProfileRoutingContext}), or otherwise to every handler whose
 * {@link FhirResourceHandler#acceptsSearch(SearchParameterMap) acceptsSearch} accepts the
 * parameters, and their results are merged by {@link CompositeBundleProvider}.
 * </ul>
 * <h2>Profiles</h2> Each resource a handler returns has that handler's implicit profile added to
 * {@code meta.profile}, so clients can discover the routing key. In a search bundle only the
 * matched resources are stamped; {@code _include} and {@code _revinclude} resources come from their
 * own resource's service.
 * <h2>Privileges</h2> A missing privilege is reported as {@link ResourceNotFoundException}, so
 * clients cannot use "forbidden" versus "not found" to learn whether a UUID exists. {@code get}
 * skips a handler that denies access; for writes, a denial by the handler the write is dispatched
 * to, including the create branch of an upsert, is reported as not found. The ownership probe does
 * not require the read privilege, so a write needs only the privilege of the owning handler's
 * operation.
 */
public abstract class BaseCompositeFhirService<R extends IAnyResource> implements FhirService<R> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected final Class<? super R> resourceClass;
	
	private List<FhirResourceHandler<R>> handlers = Collections.emptyList();
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ProfileRoutingContext profileRoutingContext = new ProfileRoutingContext();
	
	@SuppressWarnings("UnstableApiUsage")
	protected BaseCompositeFhirService() {
		// @formatter:off
		TypeToken<R> resourceTypeToken = new TypeToken<R>(getClass()) {};
		// @formatter:on
		this.resourceClass = resourceTypeToken.getRawType();
	}
	
	@Override
	public R get(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		// Calls get() directly rather than probing exists() first, which would double the lookups; a handler's get()
		// fails fast with ResourceNotFoundException for a UUID it does not hold.
		for (FhirResourceHandler<R> handler : handlers) {
			try {
				R result = handler.get(uuid);
				stampProfile(result, handler);
				return result;
			}
			catch (ResourceNotFoundException | APIAuthenticationException e) {
				// not in this handler's backing, or not visible to this user; try the next one
			}
		}
		
		throw resourceNotFound(uuid);
	}
	
	@Override
	public List<R> get(@Nonnull Collection<String> uuids) {
		List<R> aggregated = new ArrayList<>();
		for (FhirResourceHandler<R> handler : handlers) {
			List<R> chunk;
			try {
				chunk = handler.get(uuids);
			}
			catch (APIAuthenticationException e) {
				continue;
			}
			
			if (chunk == null || chunk.isEmpty()) {
				continue;
			}
			
			for (R resource : chunk) {
				stampProfile(resource, handler);
				aggregated.add(resource);
			}
		}
		
		return aggregated;
	}
	
	@Override
	public R create(@Nonnull R newResource) {
		if (newResource == null) {
			throw new InvalidRequestException("A resource of type " + resourceClass.getSimpleName() + " must be supplied");
		}
		
		FhirResourceHandler<R> handler = resolveForCreate(newResource);
		R result = handler.create(newResource);
		stampProfile(result, handler);
		return result;
	}
	
	@Override
	public R update(@Nonnull String uuid, @Nonnull R updatedResource) {
		return update(uuid, updatedResource, null, false);
	}
	
	@Override
	public R update(@Nonnull String uuid, @Nonnull R updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (updatedResource == null) {
			throw new InvalidRequestException("Resource cannot be null.");
		}
		
		FhirResourceHandler<R> handler = findOwner(uuid);
		
		if (handler == null) {
			if (!createIfNotExists) {
				throw resourceNotFound(uuid);
			}
			
			handler = resolveForCreate(updatedResource);
		}
		
		R result;
		try {
			result = handler.update(uuid, updatedResource, requestDetails, createIfNotExists);
		}
		catch (APIAuthenticationException e) {
			throw resourceNotFound(uuid);
		}
		
		stampProfile(result, handler);
		return result;
	}
	
	@Override
	public R patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		if (uuid == null) {
			throw new InvalidRequestException("id cannot be null");
		}
		
		FhirResourceHandler<R> owner = findOwner(uuid);
		if (owner == null) {
			throw resourceNotFound(uuid);
		}
		
		R result;
		try {
			result = owner.patch(uuid, patchType, body, requestDetails);
		}
		catch (APIAuthenticationException e) {
			throw resourceNotFound(uuid);
		}
		
		stampProfile(result, owner);
		return result;
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		FhirResourceHandler<R> owner = findOwner(uuid);
		if (owner == null) {
			throw resourceNotFound(uuid);
		}
		
		try {
			owner.delete(uuid);
		}
		catch (APIAuthenticationException e) {
			throw resourceNotFound(uuid);
		}
	}
	
	@Autowired(required = false)
	protected void setHandlers(List<FhirResourceHandler<R>> rawHandlers) {
		if (rawHandlers == null || rawHandlers.isEmpty()) {
			this.handlers = Collections.emptyList();
			return;
		}
		
		// Sorted here rather than trusting Spring's injection order, so that every dispatch decision, including
		// the tie-break in create(), reads precedence from the same place; see orderOf().
		List<FhirResourceHandler<R>> sorted = new ArrayList<>(rawHandlers);
		sorted.sort(Comparator.comparingInt(BaseCompositeFhirService::orderOf));
		
		// Handlers sharing an implicit profile collapse to the highest-priority one (lowest @Order). This is
		// how an external module replaces a built-in backing without both running on fan-out operations.
		Map<String, FhirResourceHandler<R>> winners = new LinkedHashMap<>();
		for (FhirResourceHandler<R> handler : sorted) {
			String profile = handler.getImplicitProfile();
			FhirResourceHandler<R> winner = winners.get(profile);
			if (winner == null) {
				winners.put(profile, handler);
			} else {
				// Warn, since this is either a deliberate override the operator should confirm or a
				// misconfigured @Order on a handler that meant to override but didn't.
				log.warn(
				    "Handler {} for {} profile '{}' was overridden by {} (lower numerical @Order wins). "
				            + "If this is unintended, raise the priority of {} (smaller @Order value) above {}'s.",
				    handler.getClass().getName(), resourceClass.getSimpleName(), profile, winner.getClass().getName(),
				    handler.getClass().getName(), winner.getClass().getName());
			}
		}
		
		this.handlers = new ArrayList<>(winners.values());
	}
	
	@PostConstruct
	void announceHandlers() {
		if (handlers.isEmpty()) {
			log.warn(
			    "No {} handlers registered for {}; all read/write requests for this resource type "
			            + "will fail with ResourceNotFoundException or NotImplementedOperationException until "
			            + "at least one FhirResourceHandler bean is provided.",
			    FhirResourceHandler.class.getSimpleName(), resourceClass.getSimpleName());
			return;
		}
		
		if (log.isInfoEnabled()) {
			log.info("{} handlers for {}: active = {}", FhirResourceHandler.class.getSimpleName(),
			    resourceClass.getSimpleName(), describe(handlers));
		}
	}
	
	private static <R extends IAnyResource> String describe(Collection<FhirResourceHandler<R>> handlers) {
		return handlers.stream().map(h -> h.getImplicitProfile() + " (" + h.getClass().getSimpleName() + ")")
		        .collect(Collectors.joining(", ", "[", "]"));
	}
	
	/**
	 * Searches the selected handlers and merges their results with {@link CompositeBundleProvider}.
	 * <p>
	 * If the request's {@code _profile} for this resource type (see {@link ProfileRoutingContext})
	 * names any handler's {@link FhirResourceHandler#getImplicitProfile() implicit profile}, only those
	 * handlers are searched and {@code acceptsSearch} is not consulted, unless
	 * {@link #profileRoutedSearchConsultsAcceptsSearch()} says otherwise. Otherwise, including when
	 * {@code _profile} names only profiles no handler owns (e.g., an unrelated conformance profile),
	 * every handler whose {@link FhirResourceHandler#acceptsSearch acceptsSearch} accepts the
	 * parameters is searched; any other handler-specific routing, such as by {@code _tag}, belongs
	 * there.
	 */
	protected IBundleProvider doSearch(@Nonnull SearchParameterMap params) {
		List<FhirResourceHandler<R>> targets = selectSearchTargets(params);
		
		if (targets.isEmpty()) {
			return new SimpleBundleProvider();
		}
		
		List<IBundleProvider> bundles = new ArrayList<>(targets.size());
		for (FhirResourceHandler<R> handler : targets) {
			IBundleProvider bundle = handler.search(params);
			if (bundle != null) {
				bundles.add(stampingProvider(bundle, handler.getImplicitProfile()));
			}
		}
		
		if (bundles.isEmpty()) {
			return new SimpleBundleProvider();
		}
		
		if (bundles.size() == 1) {
			return bundles.get(0);
		}
		
		return new CompositeBundleProvider(bundles, globalPropertyService);
	}
	
	private List<FhirResourceHandler<R>> selectSearchTargets(SearchParameterMap params) {
		Set<String> requestedProfiles = requestedProfiles();
		if (!requestedProfiles.isEmpty()) {
			List<FhirResourceHandler<R>> byProfile = handlers.stream()
			        .filter(h -> requestedProfiles.contains(h.getImplicitProfile())).collect(Collectors.toList());
			if (!byProfile.isEmpty()) {
				if (!profileRoutedSearchConsultsAcceptsSearch()) {
					return byProfile;
				}
				
				return byProfile.stream().filter(h -> h.acceptsSearch(params)).collect(Collectors.toList());
			}
			// _profile named only profiles no handler claims (e.g. an unrelated conformance profile);
			// treat it as a non-routing filter and fall through to the acceptsSearch fan-out.
		}
		
		return handlers.stream().filter(h -> h.acceptsSearch(params)).collect(Collectors.toList());
	}
	
	/**
	 * Whether a search routed by {@code _profile} is still narrowed by each named handler's
	 * {@link FhirResourceHandler#acceptsSearch acceptsSearch}. Override to return {@code true} when
	 * {@code acceptsSearch} applies a standard search parameter that the handlers' DAOs do not, so that
	 * {@code _profile} is ANDed with it rather than replacing it.
	 */
	protected boolean profileRoutedSearchConsultsAcceptsSearch() {
		return false;
	}
	
	/**
	 * Whether a body claimed by several handlers' {@code canHandle} goes to the one with the highest
	 * {@code @Order} precedence. Override to return {@code false} to reject every such body as
	 * ambiguous, so the client must choose a backing with {@code meta.profile}.
	 */
	protected boolean resolvesOverlappingClaimsByOrder() {
		return true;
	}
	
	private Set<String> requestedProfiles() {
		return profileRoutingContext.getRequestedProfiles(resourceClass.getSimpleName());
	}
	
	/**
	 * Picks the handler that owns a content-bearing write. Each {@code canHandle} speaks only for its
	 * own backing, so overlap is resolved here: by {@code @Order}, with a tie at the top rejected
	 * rather than settled by declaration order.
	 * <p>
	 * When no handler claims the body, having no handlers at all is a {@code 501}, since this
	 * deployment cannot store the resource type in any form; handlers that all decline is a
	 * {@code 400}, since the fault lies with the body.
	 */
	private FhirResourceHandler<R> resolveForCreate(R resource) {
		FhirResourceHandler<R> byProfile = resolveByProfile(resource);
		if (byProfile != null) {
			return byProfile;
		}
		
		List<FhirResourceHandler<R>> claimants = handlers.stream().filter(handler -> handler.canHandle(resource))
		        .collect(Collectors.toList());
		
		if (claimants.size() == 1) {
			return claimants.get(0);
		}
		
		if (claimants.isEmpty()) {
			if (handlers.isEmpty()) {
				throw new NotImplementedOperationException("No " + FhirResourceHandler.class.getSimpleName()
				        + " is registered for resources of type " + resourceClass.getSimpleName());
			}
			
			throw new InvalidRequestException("None of the registered handlers for " + resourceClass.getSimpleName()
			        + " accepts the supplied resource; set meta.profile to one of " + profilesOf(handlers)
			        + " to target one explicitly");
		}
		
		List<FhirResourceHandler<R>> mostSpecific = resolvesOverlappingClaimsByOrder() ? highestPrecedenceOf(claimants)
		        : claimants;
		if (mostSpecific.size() == 1) {
			return mostSpecific.get(0);
		}
		
		throw new InvalidRequestException(
		        "The supplied " + resourceClass.getSimpleName() + " is claimed by more than one backing of equal precedence "
		                + profilesOf(mostSpecific) + "; set meta.profile to one of them to choose");
	}
	
	/** Narrows the claiming handlers to those sharing the lowest {@code @Order} value among them. */
	private List<FhirResourceHandler<R>> highestPrecedenceOf(List<FhirResourceHandler<R>> claimants) {
		int winning = claimants.stream().mapToInt(BaseCompositeFhirService::orderOf).min().orElse(Ordered.LOWEST_PRECEDENCE);
		return claimants.stream().filter(handler -> orderOf(handler) == winning).collect(Collectors.toList());
	}
	
	/**
	 * A handler's precedence: {@link Ordered#getOrder()} if it implements {@link Ordered}, otherwise
	 * {@code @Order} on its class, read from the target class since a handler proxied by Spring AOP
	 * (e.g., one that is {@code @Transactional}) does not carry the annotation on its proxy class.
	 * {@code @Order} on a {@code @Bean} method is not visible here and so is not honoured.
	 */
	private static int orderOf(Object handler) {
		if (handler instanceof Ordered) {
			return ((Ordered) handler).getOrder();
		}
		
		return OrderUtils.getOrder(AopUtils.getTargetClass(handler), Ordered.LOWEST_PRECEDENCE);
	}
	
	private String profilesOf(Collection<FhirResourceHandler<R>> subject) {
		return subject.stream().map(FhirResourceHandler::getImplicitProfile).collect(Collectors.joining(", ", "[", "]"));
	}
	
	private FhirResourceHandler<R> resolveByProfile(R resource) {
		IBaseMetaType meta = resource.getMeta();
		if (meta == null) {
			return null;
		}
		
		List<? extends IPrimitiveType<String>> profiles = meta.getProfile();
		if (profiles == null || profiles.isEmpty()) {
			return null;
		}
		
		for (IPrimitiveType<String> profile : profiles) {
			if (profile == null) {
				continue;
			}
			
			String url = profile.getValue();
			if (url == null || url.isEmpty()) {
				continue;
			}
			
			for (FhirResourceHandler<R> handler : handlers) {
				if (url.equals(handler.getImplicitProfile())) {
					return handler;
				}
			}
		}
		
		// unrecognised profiles are ignored rather than rejected, leaving the choice to canHandle()
		return null;
	}
	
	/**
	 * A handler whose {@code exists} is guarded and denies the probe may own the UUID, so we report not
	 * found rather than skip it; skipping would let an update-as-create write the same UUID into a
	 * second backing.
	 */
	private FhirResourceHandler<R> findOwner(String uuid) {
		for (FhirResourceHandler<R> handler : handlers) {
			try {
				if (handler.exists(uuid)) {
					return handler;
				}
			}
			catch (APIAuthenticationException e) {
				throw resourceNotFound(uuid);
			}
		}
		return null;
	}
	
	private void stampProfile(R resource, FhirResourceHandler<R> handler) {
		String url = handler.getImplicitProfile();
		if (url == null || url.isEmpty() || resource == null) {
			return;
		}
		
		IBaseMetaType meta = resource.getMeta();
		if (meta == null) {
			return;
		}
		
		boolean already = meta.getProfile().stream().map(IPrimitiveType::getValue).filter(java.util.Objects::nonNull)
		        .anyMatch(url::equals);
		if (!already) {
			meta.addProfile(url);
		}
	}
	
	private IBundleProvider stampingProvider(IBundleProvider delegate, String profileUrl) {
		if (profileUrl == null || profileUrl.isEmpty()) {
			return delegate;
		}
		
		return new ProfileStampingBundleProvider(delegate, profileUrl, resourceClass);
	}
	
	protected ResourceNotFoundException resourceNotFound(String uuid) {
		return new ResourceNotFoundException(
		        "Resource of type " + resourceClass.getSimpleName() + " with ID " + uuid + " is not known");
	}
	
	protected List<FhirResourceHandler<R>> getHandlers() {
		return handlers;
	}
	
	protected FhirGlobalPropertyService getGlobalPropertyService() {
		return globalPropertyService;
	}
	
	/**
	 * Wraps an {@link IBundleProvider} to stamp a profile URL onto each returned resource's
	 * {@code meta.profile} (deduplicating against any already-present entry).
	 */
	private static class ProfileStampingBundleProvider implements IBundleProvider {
		
		private final IBundleProvider delegate;
		
		private final String profileUrl;
		
		private final Class<?> resourceClass;
		
		ProfileStampingBundleProvider(IBundleProvider delegate, String profileUrl, Class<?> resourceClass) {
			this.delegate = delegate;
			this.profileUrl = profileUrl;
			this.resourceClass = resourceClass;
		}
		
		/**
		 * Stamps only the matched resources of the page. A search bundle provider returns the
		 * {@code toIndex - fromIndex} matches first and appends any {@code _include} / {@code _revinclude}
		 * resources after them; those come from their own resource's service and already carry the right
		 * profile.
		 * <p>
		 * Only resources of the service's own type are stamped, because not every provider bounds its
		 * matches by the page span: {@code $everything} pre-materialises matches and includes into a single
		 * list and reports them all as results.
		 */
		@Nonnull
		@Override
		public List<IBaseResource> getResources(int fromIndex, int toIndex) {
			List<IBaseResource> raw = delegate.getResources(fromIndex, toIndex);
			if (raw == null || raw.isEmpty()) {
				return raw == null ? Collections.emptyList() : raw;
			}
			
			int matches = toIndex > fromIndex ? Math.min(toIndex - fromIndex, raw.size()) : raw.size();
			for (int i = 0; i < matches; i++) {
				IBaseResource resource = raw.get(i);
				if (!resourceClass.isInstance(resource)) {
					continue;
				}
				
				IBaseMetaType meta = resource.getMeta();
				if (meta == null) {
					continue;
				}
				
				boolean already = meta.getProfile().stream().map(IPrimitiveType::getValue).filter(java.util.Objects::nonNull)
				        .anyMatch(profileUrl::equals);
				if (!already) {
					meta.addProfile(profileUrl);
				}
			}
			
			return raw;
		}
		
		@Override
		public IPrimitiveType<Date> getPublished() {
			return delegate.getPublished();
		}
		
		@Override
		public Integer preferredPageSize() {
			return delegate.preferredPageSize();
		}
		
		@Override
		public Integer size() {
			return delegate.size();
		}
		
		@Override
		public String getUuid() {
			return delegate.getUuid();
		}
	}
}
