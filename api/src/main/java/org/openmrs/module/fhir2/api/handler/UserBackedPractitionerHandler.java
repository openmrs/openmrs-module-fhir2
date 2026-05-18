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
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_HANDLER_PRACTITIONER_USER_BACKING_KEY;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
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
 * delegating every {@link org.openmrs.module.fhir2.api.FhirService} call to the existing
 * {@link FhirUserService}. Sibling of {@link ProviderBackedPractitionerHandler}.
 * <p>
 * {@link #canHandle(Practitioner)} returns {@code false}: the OLD orchestrator never created a User
 * from a FHIR Practitioner write, and this handler preserves that behaviour. The user backing only
 * participates in reads (UUID-based dispatch via {@code exists()}) and searches (fan-out from the
 * orchestrator). An external module wanting to enable user-create-via-FHIR would register an
 * override handler with the same backing key and a {@code canHandle} that returns {@code true}
 * under whatever discriminator it chooses.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UserBackedPractitionerHandler implements FhirResourceHandler<Practitioner> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-user";
	
	private static final String BACKING_KEY = OPENMRS_HANDLER_PRACTITIONER_USER_BACKING_KEY;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirUserService userService;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Nonnull
	@Override
	public String getBackingKey() {
		return BACKING_KEY;
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
		return userService.create(newResource);
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource) {
		return userService.update(uuid, updatedResource);
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		return userService.update(uuid, updatedResource, requestDetails, createIfNotExists);
	}
	
	@Override
	public Practitioner patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		return userService.patch(uuid, patchType, body, requestDetails);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		userService.delete(uuid);
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
