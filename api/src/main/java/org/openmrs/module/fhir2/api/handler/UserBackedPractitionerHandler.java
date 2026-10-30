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
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
 * delegating reads and searches to {@link FhirUserService}. Sibling of
 * {@link ProviderBackedPractitionerHandler}.
 * <p>
 * The user backing is read-only through the FHIR API. {@link #canHandle(Practitioner)} returns
 * {@code false} so no content-based dispatch selects it. A create naming this handler in
 * {@code meta.profile} throws {@link MethodNotAllowedException}; an update, patch or delete of an
 * existing user reports the UUID as not found, as for an unknown UUID, since a distinct error would
 * reveal to any client which UUIDs belong to users. Neither reaches {@link FhirUserService}'s write
 * path. An external module wanting to enable user writes via FHIR would register an override
 * handler with the same implicit profile and a higher priority.
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
		throw notFound(uuid);
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		throw notFound(uuid);
	}
	
	@Override
	public Practitioner patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		throw notFound(uuid);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		throw notFound(uuid);
	}
	
	/**
	 * Matches the composite service's report for an unknown UUID, so that a client without access to
	 * users cannot tell a user's UUID from one that does not exist.
	 */
	private static ResourceNotFoundException notFound(String uuid) {
		return new ResourceNotFoundException(
		        "Resource of type " + Practitioner.class.getSimpleName() + " with ID " + uuid + " is not known");
	}
	
	private static MethodNotAllowedException readOnly() {
		return new MethodNotAllowedException("Practitioners backed by OpenMRS users are read-only through the FHIR API",
		        RequestTypeEnum.GET);
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		// The practitioner search parameters are not mapped onto users, so every user is returned
		// regardless of the filters.
		return userService.searchForUsers(new SearchParameterMap());
	}
}
