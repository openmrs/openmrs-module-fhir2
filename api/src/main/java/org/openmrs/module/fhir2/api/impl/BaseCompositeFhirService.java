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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.CompositeBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract orchestrator for FHIR resource types backed by more than one OpenMRS domain object.
 * Subclasses provide the concrete resource type {@code R}; Spring injects the ordered list of
 * {@link FhirResourceHandler}s registered for that type. The orchestrator dispatches each
 * {@link FhirService} call using one of two primitives:
 * <ul>
 * <li><b>UUID-based</b> — {@link FhirService#exists(String)}. Used for {@code get}, {@code update},
 * {@code patch}, {@code delete}. Picks the first handler in {@code @Order} priority whose backing
 * store reports the UUID exists.
 * <li><b>Content-based</b> — {@link FhirResourceHandler#canHandle(IAnyResource)} (with
 * {@link FhirResourceHandler#getImplicitProfile() meta.profile} taking precedence). Used for
 * {@code create} and for the {@code createIfNotExists} branch of {@code update} when no handler
 * owns the UUID yet.
 * </ul>
 * <p>
 * Per-operation behaviour:
 * <ul>
 * <li>{@code get(uuid)} — {@code exists()} probe → {@code handler.get(uuid)}.
 * {@link ca.uhn.fhir.rest.server.exceptions.ResourceGoneException} from the owning handler
 * propagates without fall-through. {@link ResourceNotFoundException} if no handler claims it.
 * <li>{@code create(R)} — {@code meta.profile} match → first {@code canHandle} → otherwise
 * {@link NotImplementedOperationException}. Profile-targeted routing is unconditional — the named
 * handler validates the resource and may itself throw {@link InvalidRequestException} if the input
 * is malformed for it.
 * <li>{@code update(uuid, r)} / {@code update(uuid, r, RD, false)} — {@code exists()} probe →
 * {@code handler.update(...)}. {@link ResourceNotFoundException} if no handler owns the UUID.
 * <li>{@code update(uuid, r, RD, true)} — {@code exists()} probe first; if a handler owns the UUID,
 * dispatch to it. Otherwise fall back to {@code meta.profile} / {@code canHandle} dispatch and call
 * {@code handler.update(uuid, r, RD, true)} so the chosen handler runs its own create-if-not-exists
 * logic.
 * <li>{@code patch(uuid, ...)} — {@code exists()} probe → {@code handler.patch(uuid, ...)}.
 * <li>{@code delete(uuid)} — {@code exists()} probe → {@code handler.delete(uuid)}.
 * <li>{@code search(params)} — fan out to handlers whose
 * {@link FhirResourceHandler#acceptsSearch(SearchParameterMap) acceptsSearch} returns true; merge
 * results via {@link CompositeBundleProvider}. Tag-based routing logic is the handler's own
 * concern, embedded in {@code acceptsSearch} — the orchestrator stays agnostic.
 * </ul>
 * Resources returned by handlers have the handler's implicit profile stamped onto
 * {@code meta.profile} (deduped if already present) so clients can discover the routing key, both
 * for direct read/write returns and lazily on each page of a search bundle.
 */
public abstract class BaseCompositeFhirService<R extends IAnyResource> implements FhirService<R> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected final Class<? super R> resourceClass;
	
	private List<FhirResourceHandler<R>> handlers = Collections.emptyList();
	
	private Map<String, List<FhirResourceHandler<R>>> overriddenByKey = Collections.emptyMap();
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
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
		
		// Try each handler in priority order. By default, the FhirResourceHandler's get() method should be equivalent
		// to exists() if the item doesn't exist, i.e., bail early with a ResourceNotFound exception, so we avoid doing
		// 2 * N db lookups here.
		for (FhirResourceHandler<R> handler : handlers) {
			try {
				R result = handler.get(uuid);
				stampProfile(result, handler);
				return result;
			}
			catch (ResourceNotFoundException e) {
				// not in this handler's backing — try the next one
			}
		}
		
		throw resourceNotFound(uuid);
	}
	
	@Override
	public List<R> get(@Nonnull Collection<String> uuids) {
		List<R> aggregated = new ArrayList<>();
		for (FhirResourceHandler<R> handler : handlers) {
			List<R> chunk = handler.get(uuids);
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
		
		FhirResourceHandler<R> owner = findOwner(uuid);
		
		if (owner == null) {
			if (!createIfNotExists) {
				throw resourceNotFound(uuid);
			}
			
			FhirResourceHandler<R> handler = resolveForCreate(updatedResource);
			R result = handler.update(uuid, updatedResource, requestDetails, true);
			stampProfile(result, handler);
			return result;
		}
		
		R result = owner.update(uuid, updatedResource, requestDetails, createIfNotExists);
		stampProfile(result, owner);
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
		
		R result = owner.patch(uuid, patchType, body, requestDetails);
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
		
		owner.delete(uuid);
	}
	
	@Autowired(required = false)
	protected void setHandlers(List<FhirResourceHandler<R>> rawHandlers) {
		if (rawHandlers == null || rawHandlers.isEmpty()) {
			this.handlers = Collections.emptyList();
			this.overriddenByKey = Collections.emptyMap();
			return;
		}
		
		Map<String, FhirResourceHandler<R>> winners = new LinkedHashMap<>();
		Map<String, List<FhirResourceHandler<R>>> losers = new LinkedHashMap<>();
		for (FhirResourceHandler<R> handler : rawHandlers) {
			String key = handler.getBackingKey();
			if (winners.containsKey(key)) {
				losers.computeIfAbsent(key, k -> new ArrayList<>()).add(handler);
			} else {
				winners.put(key, handler);
			}
		}
		
		this.handlers = new ArrayList<>(winners.values());
		this.overriddenByKey = losers;
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
		
		log.info("{} handlers for {}: active = {}", FhirResourceHandler.class.getSimpleName(), resourceClass.getSimpleName(),
		    describe(handlers));
		
		// Each entry here means: a handler declared the same backing key as another but lost the
		// priority race. Logged as WARN because it's nearly always one of two things — a
		// legitimate override (the operator should confirm the right handler won) or a
		// configuration mistake (an external handler that *meant* to override the built-in but
		// has the wrong @Order). Either way the operator needs to see it.
		overriddenByKey.forEach((key, losers) -> {
			FhirResourceHandler<R> winner = winnerForKey(key);
			for (FhirResourceHandler<R> loser : losers) {
				log.warn(
				    "Handler {} for {} backing key '{}' was overridden by {} (lower numerical @Order wins). "
				            + "If this is unintended, raise the priority of {} (smaller @Order value) above {}'s.",
				    loser.getClass().getName(), resourceClass.getSimpleName(), key, winner.getClass().getName(),
				    loser.getClass().getName(), winner.getClass().getName());
			}
		});
	}
	
	private FhirResourceHandler<R> winnerForKey(String key) {
		for (FhirResourceHandler<R> h : handlers) {
			if (key.equals(h.getBackingKey())) {
				return h;
			}
		}
		return null;
	}
	
	private static <R extends IAnyResource> String describe(Collection<FhirResourceHandler<R>> handlers) {
		return handlers.stream().map(h -> h.getBackingKey() + " (" + h.getClass().getSimpleName() + ")")
		        .collect(Collectors.joining(", ", "[", "]"));
	}
	
	/**
	 * Runs a fan-out search across handlers whose {@link FhirResourceHandler#acceptsSearch} returns
	 * true and merges the result bundles via {@link CompositeBundleProvider}. Tag-based routing is the
	 * handler's own concern, embedded in {@code acceptsSearch} — the orchestrator stays agnostic about
	 * the routing protocol.
	 */
	protected IBundleProvider doSearch(@Nonnull SearchParameterMap params) {
		List<FhirResourceHandler<R>> targets = handlers.stream().filter(h -> h.acceptsSearch(params))
		        .collect(Collectors.toList());
		
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
	
	private FhirResourceHandler<R> resolveForCreate(R resource) {
		FhirResourceHandler<R> byProfile = resolveByProfile(resource);
		if (byProfile != null) {
			return byProfile;
		}
		
		for (FhirResourceHandler<R> handler : handlers) {
			if (handler.canHandle(resource)) {
				return handler;
			}
		}
		
		throw new NotImplementedOperationException(
		        "No registered handler claims the supplied " + resourceClass.getSimpleName());
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
		
		// Unrecognized profiles are intentionally ignored — the orchestrator falls through to
		// canHandle() rather than rejecting the request.
		return null;
	}
	
	private FhirResourceHandler<R> findOwner(String uuid) {
		for (FhirResourceHandler<R> handler : handlers) {
			if (handler.exists(uuid)) {
				return handler;
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
		
		return new ProfileStampingBundleProvider(delegate, profileUrl);
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
		
		ProfileStampingBundleProvider(IBundleProvider delegate, String profileUrl) {
			this.delegate = delegate;
			this.profileUrl = profileUrl;
		}
		
		@Nonnull
		@Override
		public List<IBaseResource> getResources(int fromIndex, int toIndex) {
			List<IBaseResource> raw = delegate.getResources(fromIndex, toIndex);
			if (raw == null || raw.isEmpty()) {
				return raw;
			}
			
			for (IBaseResource resource : raw) {
				if (resource == null) {
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
