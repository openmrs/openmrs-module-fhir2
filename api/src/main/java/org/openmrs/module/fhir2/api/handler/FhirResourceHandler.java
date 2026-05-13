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
 * A handler <em>is</em> a {@link FhirService} — it inherits the standard CRUD contract and exposes
 * a few extra hooks the orchestrator uses to dispatch among handlers. There are exactly two
 * dispatch primitives:
 * <ul>
 * <li><b>Content-based</b> — {@link #canHandle(IAnyResource)}. Used for {@code create} and for the
 * {@code createIfNotExists} branch of {@code update}. The orchestrator picks the first handler in
 * priority order whose predicate returns {@code true} (or whose {@link #getImplicitProfile()}
 * matches the incoming resource's {@code meta.profile}).
 * <li><b>UUID-based</b> — {@link FhirService#exists(String)}. Used for {@code get}, {@code update},
 * {@code patch}, {@code delete}. The orchestrator picks the first handler whose backing store
 * reports the UUID exists. {@code BaseFhirService.exists} provides a cheap DAO row check; handlers
 * that compose another service should override to delegate (e.g. to
 * {@code visitService.exists(uuid)}).
 * </ul>
 * <p>
 * Search restriction (e.g. {@code _tag}-based routing between an encounter handler and a visit
 * handler) lives inside each handler's {@link #acceptsSearch(SearchParameterMap)} — the
 * orchestrator stays agnostic about the routing protocol and just fans out across handlers that
 * accept the search.
 * <h2>Handler priority and @Order</h2> Handlers are sorted by Spring's
 * {@link org.springframework.core.annotation.Order @Order} annotation (or by implementing
 * {@link org.springframework.core.Ordered}). Lower numerical value means higher priority — first to
 * claim a {@code canHandle} / first probed by {@code exists}.
 * <p>
 * Built-in handlers shipped by this module register at
 * {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE} (or close to it) on purpose, so any
 * external module's handler with an unspecified or lower-valued {@code @Order} runs first and can
 * override the built-in mapping. External modules that want to <em>complement</em> rather than
 * override should still use a value lower than {@code LOWEST_PRECEDENCE} so the ordering is
 * deterministic — beans without an explicit {@code @Order} are tied at {@code LOWEST_PRECEDENCE}
 * and their relative order is undefined.
 * <p>
 * Tie-breaking semantics on {@code canHandle}: when two handlers both claim the same incoming
 * resource (e.g. an {@code Encounter} body carrying both encounter-type and visit-type codings),
 * the higher-priority handler wins silently. Use {@code meta.profile} to target a specific handler
 * when ambiguity needs to be resolved by the client.
 * <h2>Implementing a handler</h2> Handler implementations typically bundle a DAO and a translator
 * (often by extending {@code BaseFhirService}) or compose an existing OpenMRS-typed service. A
 * minimal composing implementation — for example, the visit-backed mapping for FHIR
 * {@code Encounter}, delegating to {@code FhirVisitService} — looks like: <pre>
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
 *         // Claim incoming Encounters whose type[].coding has the OpenMRS visit-type system.
 *         return encounter.getType().stream().flatMap(t -&gt; t.getCoding().stream())
 *             .anyMatch(c -&gt; FhirConstants.VISIT_TYPE_SYSTEM_URI.equals(c.getSystem()));
 *     }
 *
 *     {@literal @}Override
 *     public boolean acceptsSearch(SearchParameterMap params) {
 *         // Opt out when _tag in the encounter-tag system specifies a non-visit code.
 *         return !excludedByRoutingTag(params, OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
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
 * </pre> The dispatch methods worth getting right:
 * <ul>
 * <li>{@link #getImplicitProfile()} — a stable canonical URL identifying this handler. Clients can
 * discover it from {@code meta.profile} on returned resources, and use it to target this handler on
 * a write. The URL itself doesn't need to resolve to a published {@code StructureDefinition}, but
 * it must be unique across all registered handlers for the same resource type. URLs under
 * {@code http://fhir.openmrs.org/StructureDefinition/openmrs-*} are reserved for built-in handlers;
 * external modules should namespace by module id.
 * <li>{@link #canHandle(IAnyResource)} — the dispatch predicate for content-bearing writes when
 * {@code meta.profile} doesn't pin the request to a specific handler. Examine tags, codings,
 * profiles, references — whatever distinguishes resources this handler should own from those a
 * sibling handler should own. Keep it cheap; it runs on every create. This is <em>not</em> input
 * validation: a handler may claim a resource and still reject specific malformed inputs at
 * {@link #create(IAnyResource)} time.
 * <li>{@link #acceptsSearch(SearchParameterMap)} — the participation predicate for searches.
 * Default {@code true} (always participate). Override to opt out — typically when the search params
 * reference fields the handler cannot honor, or when a {@code _tag} parameter routes the request to
 * a different handler. Tag-based routing is conventionally implemented by checking whether the
 * search params include a {@code _tag} in this handler's routing coding system whose value doesn't
 * match this handler's code; if so, return {@code false}. Tags from unrelated coding systems are
 * treated as content filters and don't cause opt-out.
 * <li>{@link FhirService#exists(String)} (inherited) — the cheap UUID probe. Handlers extending
 * {@code BaseFhirService} get a row-existence DAO check for free; handlers composing another
 * service should override to delegate (e.g. {@code visitService.exists(uuid)}). Avoid going through
 * {@code get()} for the probe — it pays the translator cost which the orchestrator doesn't need at
 * probe time.
 * </ul>
 */
public interface FhirResourceHandler<R extends IAnyResource> extends FhirService<R> {
	
	/**
	 * The canonical URL identifying this handler as an implicit FHIR profile. The orchestrator stamps
	 * this URL onto the {@code meta.profile} of every resource the handler returns, and clients may
	 * target this handler explicitly on a write by setting {@code meta.profile} to this URL.
	 * <p>
	 * URLs under {@code http://fhir.openmrs.org/StructureDefinition/openmrs-*} are reserved for
	 * handlers shipped by this module. Handlers shipped by other modules should namespace their URLs by
	 * module id (e.g. {@code http://fhir.openmrs.org/StructureDefinition/{moduleId}/...}).
	 *
	 * @return the canonical URL of this handler's implicit profile, never {@code null}
	 */
	@Nonnull
	String getImplicitProfile();
	
	/**
	 * Predicate that decides whether this handler claims an incoming resource on a content-bearing
	 * write — {@link #create(IAnyResource)}, or {@link #update(String, IAnyResource)} when no handler
	 * currently owns the UUID and {@code createIfNotExists} is {@code true}.
	 * <p>
	 * This is a <em>dispatch</em> predicate, not input validation. A handler may return {@code true}
	 * here and still reject specific malformed inputs at create or update time by throwing
	 * {@link ca.uhn.fhir.rest.server.exceptions.InvalidRequestException}.
	 *
	 * @param resource the FHIR resource being submitted, never {@code null}
	 * @return {@code true} if this handler is willing to process the resource
	 */
	boolean canHandle(@Nonnull R resource);
	
	/**
	 * Stable, opaque identifier for the mapping this handler implements — distinct from
	 * {@link #getImplicitProfile()} in that it is <em>not</em> exposed to FHIR clients. Two handlers
	 * that return the same backing key are treated by the orchestrator as overrides of the same
	 * mapping: only the highest-priority one (by {@code @Order}) participates in any dispatch —
	 * single-result reads, content-based writes, and fan-out search alike. The lower-priority one is
	 * dropped at startup, and the orchestrator logs a warning so operators can confirm the override is
	 * the one they intended.
	 * <p>
	 * The default is {@link Class#getName() getClass().getName()}, which keeps unrelated handlers
	 * independent without any extra configuration. Built-in handlers shipped by this module override
	 * the default with short stable strings (e.g. {@code "openmrs.encounter"},
	 * {@code "openmrs.visit"}); an external module that wants to replace a built-in returns the same
	 * string. New backings introduced by external modules should namespace by module id (e.g.
	 * {@code "myModule.imagingOrder"}) so they don't collide with built-ins.
	 * <p>
	 * This is the only mechanism by which two handlers for the same FHIR resource type can coordinate
	 * "I replace you" semantics. Without it, two handlers scanning the same backing would both run on
	 * fan-out operations (search, {@link #get(java.util.Collection)}) — the default's results would
	 * interleave with the override's, producing duplicate scans and surprising output.
	 *
	 * @return the backing-key identifier for this handler, never {@code null}
	 */
	@Nonnull
	default String getBackingKey() {
		return getClass().getName();
	}
	
	/**
	 * Returns whether this handler should participate in a search with the given parameters. The
	 * default returns {@code true}; override to opt out when the parameters target a different handler
	 * (e.g. via {@code _tag}-based routing) or reference fields this handler cannot honor.
	 * <p>
	 * The orchestrator runs the search against every handler whose {@code acceptsSearch} returns
	 * {@code true} and merges the results via
	 * {@link org.openmrs.module.fhir2.api.search.CompositeBundleProvider}.
	 *
	 * @param params the search parameter map, never {@code null}
	 * @return {@code true} to include this handler in the fan-out search
	 */
	default boolean acceptsSearch(@Nonnull SearchParameterMap params) {
		return true;
	}
	
	/**
	 * Runs a search and returns the matching resources as an {@link IBundleProvider}. Implementations
	 * should honor sort, paging, and {@code _include}/{@code _revinclude} parameters in the same way
	 * the standard single-resource services do.
	 *
	 * @param params the search parameters
	 * @return a bundle provider over the matching resources
	 */
	IBundleProvider search(@Nonnull SearchParameterMap params);
}
