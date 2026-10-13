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

import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Maps the FHIR {@link Practitioner} resource onto the OpenMRS {@code User} domain object by
 * delegating reads and searches to the existing {@link FhirUserService}. Sibling of
 * {@link ProviderBackedPractitionerHandler}.
 * <p>
 * The user backing is read-only through the FHIR API. {@link #canHandle(Practitioner)} returns
 * {@code false} so no content-based dispatch selects it, and every write operation throws
 * {@link MethodNotAllowedException} so that neither a {@code meta.profile} naming this handler nor
 * UUID-based ownership of an existing user can reach {@link FhirUserService}'s write path. An
 * external module wanting to enable user writes via FHIR would register an override handler with
 * the same implicit profile and a higher priority.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UserBackedPractitionerHandler implements FhirResourceHandler<Practitioner> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-user";
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirUserService userService;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull Practitioner resource) {
		return false;
	}
	
	@Override
	public Practitioner get(@Nonnull String uuid) {
		return userService.get(uuid);
	}
	
	@Nonnull
	@Override
	public List<Practitioner> get(@Nonnull Collection<String> uuids) {
		return userService.get(uuids);
	}
	
	@Override
	public boolean exists(@Nonnull String uuid) {
		return userService.exists(uuid);
	}
	
	@Override
	public Practitioner create(@Nonnull Practitioner newResource) {
		throw readOnly();
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource) {
		throw readOnly();
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		throw readOnly();
	}
	
	@Override
	public Practitioner patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		throw readOnly();
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		throw readOnly();
	}
	
	private static MethodNotAllowedException readOnly() {
		return new MethodNotAllowedException("Practitioners backed by OpenMRS users are read-only through the FHIR API",
		        RequestTypeEnum.GET);
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		// Preserve OLD behaviour: the previous orchestrator called userService.searchForUsers with
		// an empty SearchParameterMap, returning every user regardless of the practitioner search
		// filters. Pre-existing limitation worth fixing separately — keeping the behaviour stable
		// across this migration.
		return userService.searchForUsers(new SearchParameterMap());
	}
}
