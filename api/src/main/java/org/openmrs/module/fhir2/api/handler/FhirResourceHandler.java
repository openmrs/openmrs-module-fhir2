/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * SPI for plugging additional OpenMRS-side mappings into a FHIR resource type that this module
 * already exposes. Each handler represents one OpenMRS backing for a single FHIR resource type (for
 * example one handler maps {@code Encounter} onto {@code org.openmrs.Encounter} while another maps
 * {@code Encounter} onto {@code org.openmrs.Visit}).
 * <p>
 * The orchestrator dispatches among handlers as follows:
 * <ul>
 * <li>{@code create}, and {@code update} with {@code createIfNotExists} for an unowned UUID: the
 * handler whose {@link #getImplicitProfile()} appears in {@code meta.profile}; failing that, the
 * highest-priority handler whose {@link #canHandle(IAnyResource)} claims the body.
 * <li>{@code update}, {@code patch} and {@code delete}: the first handler whose
 * {@link FhirService#exists(String)} reports the UUID.
 * <li>{@code get}: each handler's {@code get} in priority order until one returns the resource.
 * <li>{@code search}: the handlers named by {@code _profile}, or else every handler whose
 * {@link #acceptsSearch(SearchParameterMap)} accepts, with the results merged.
 * </ul>
 * <h2>Handler priority and @Order</h2> Handlers are sorted by Spring's
 * {@link org.springframework.core.annotation.Order @Order} annotation (or by implementing
 * {@link org.springframework.core.Ordered}). Lower numerical value means higher priority: it wins a
 * contested {@code canHandle}, is probed first by {@code exists} and {@code get}, and has its
 * results listed first in a merged search. The annotation must be on the handler's class;
 * {@code @Order} on a {@code @Bean} method is ignored, so a handler declared that way should
 * implement {@link org.springframework.core.Ordered} instead.
 * <p>
 * Built-in handlers register at {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}, or at
 * {@code LOWEST_PRECEDENCE - 10} for the primary backing of a resource with several (the OpenMRS
 * encounter, condition and provider backings). An external handler must declare a lower
 * {@code @Order} value than the built-in it means to outrank; a bean without an explicit
 * {@code @Order} ties with the built-ins, and the relative order of tied beans is undefined.
 * <p>
 * Two handlers that return the same {@link #getImplicitProfile() implicit profile} are treated as
 * the same backing: at startup the orchestrator keeps only the highest-priority one and logs a
 * warning. This is how an external module replaces a built-in mapping: return the built-in's
 * profile URL and a higher priority. A new backing should expose its own profile URL instead.
 * <p>
 * Write {@code canHandle} to describe <em>your</em> backing and nothing else: a handler that
 * enumerates its rivals cannot accommodate one added later by another module. The orchestrator
 * resolves overlapping claims by {@code @Order} and rejects a tie at the top, or a body no handler
 * claims, with {@link ca.uhn.fhir.rest.server.exceptions.InvalidRequestException}, since
 * declaration order is not an answer the client can predict or correct. Because
 * {@code meta.profile} is consulted first, neither rejection can block a client that has stated
 * which backing it means.
 * <h2>Implementing a handler</h2> Handler implementations typically bundle a DAO and a translator
 * (often by extending {@code BaseFhirService}) or compose an existing OpenMRS-typed service. A
 * minimal composing implementation, such as the visit-backed mapping for FHIR {@code Encounter}
 * that delegates to {@code FhirVisitService}, looks like: <pre>
 * {@literal @}Component
 * {@literal @}Order(Ordered.LOWEST_PRECEDENCE)        // or any lower value to override built-ins
 * public class VisitBackedEncounterHandler implements FhirResourceHandler&lt;Encounter&gt; {
 *
 *     {@literal @}Autowired
 *     private FhirVisitService visitService;
 *
 *     {@literal @}Override
 *     public String getImplicitProfile() {
 *         return "http://fhir.openmrs.org/StructureDefinition/openmrs-visit";
 *     }
 *
 *     {@literal @}Override
 *     public boolean canHandle(Encounter encounter) {
 *         return HandlerSupport.hasCodingInSystem(encounter.getType(), FhirConstants.VISIT_TYPE_SYSTEM_URI);
 *     }
 *
 *     {@literal @}Override
 *     public boolean acceptsSearch(SearchParameterMap params) {
 *         return !HandlerSupport.routingTagExcludes(params, OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
 *     }
 *
 *     {@literal @}Override
 *     public Encounter get(String uuid)               { return visitService.get(uuid); }
 *     {@literal @}Override
 *     public boolean exists(String uuid)              { return visitService.exists(uuid); }
 *     {@literal @}Override
 *     public Encounter create(Encounter r)            { return visitService.create(r); }
 *     // ... update / patch / delete / search delegate similarly to visitService
 * }
 * </pre> Handlers extending {@code BaseFhirService} get {@link FhirService#exists(String)} as a
 * row-existence DAO check; handlers composing another service should override it to delegate, as
 * above. Don't implement it via {@code get()}: that pays the translator cost and requires the read
 * privilege, which the probe must not (see {@code FhirDao#exists(String)}).
 */
public interface FhirResourceHandler<R extends IAnyResource> extends FhirService<R> {
	
	/**
	 * The canonical URL identifying this handler as an implicit FHIR profile. The orchestrator stamps
	 * it onto the {@code meta.profile} of every resource the handler returns, and clients target this
	 * handler with it in {@code meta.profile} on a write or {@code _profile} on a search. It need not
	 * resolve to a published {@code StructureDefinition}, but must be unique among handlers for the
	 * same resource type.
	 * <p>
	 * URLs under {@code http://fhir.openmrs.org/StructureDefinition/openmrs-*} are reserved for
	 * handlers shipped by this module. Handlers shipped by other modules should namespace their URLs by
	 * module id (e.g. {@code http://fhir.openmrs.org/StructureDefinition/{moduleId}/...}).
	 */
	@Nonnull
	String getImplicitProfile();
	
	/**
	 * Predicate that decides whether this handler claims an incoming resource on a content-bearing
	 * write: {@link #create(IAnyResource)}, or {@link #update(String, IAnyResource)} when no handler
	 * currently owns the UUID and {@code createIfNotExists} is {@code true}.
	 * <p>
	 * This is a <em>dispatch</em> predicate, not input validation. A handler may return {@code true}
	 * here and still reject specific malformed inputs at create or update time by throwing
	 * {@link ca.uhn.fhir.rest.server.exceptions.InvalidRequestException}.
	 * <p>
	 * Answer for your own backing only; the class documentation explains how the orchestrator resolves
	 * overlapping claims. A handler that is the only backing for its resource type should simply return
	 * {@code true}. Keep it cheap; it runs on every create.
	 */
	boolean canHandle(@Nonnull R resource);
	
	/**
	 * Returns whether this handler should participate in a search with the given parameters. The
	 * default returns {@code true}; override to opt out when the parameters target a different handler
	 * or reference fields this handler cannot honor. Use {@link HandlerSupport#routingTagExcludes} for
	 * {@code _tag}-based routing.
	 * <p>
	 * A request whose {@code _profile} names a handler's {@link #getImplicitProfile() implicit profile}
	 * goes straight to the named handlers, and by default this method is not consulted (see
	 * {@code BaseCompositeFhirService#profileRoutedSearchConsultsAcceptsSearch()}).
	 */
	default boolean acceptsSearch(@Nonnull SearchParameterMap params) {
		return true;
	}
	
	/**
	 * Implementations should honor sort, paging, and {@code _include}/{@code _revinclude} parameters in
	 * the same way the standard single-resource services do.
	 */
	IBundleProvider search(@Nonnull SearchParameterMap params);
}
