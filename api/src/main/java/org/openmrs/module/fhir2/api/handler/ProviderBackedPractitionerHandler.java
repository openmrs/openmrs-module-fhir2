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
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_HANDLER_PRACTITIONER_PROVIDER_BACKING_KEY;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.module.fhir2.api.FhirOpenmrsProviderService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Maps the FHIR {@link Practitioner} resource onto the OpenMRS {@code Provider} domain object by
 * delegating every {@link org.openmrs.module.fhir2.api.FhirService} call to the
 * {@link FhirOpenmrsProviderService}. Sibling of {@link UserBackedPractitionerHandler}, which
 * applies the same composition pattern over {@code FhirUserService}.
 * <p>
 * Claims any incoming Practitioner on create — provider is the default backing for new Practitioner
 * resources (the OLD orchestrator created providers and never users from a FHIR Practitioner
 * write). The orchestrator's content-based dispatch resolves to this handler unless
 * {@code meta.profile} explicitly names the user-backed profile.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class ProviderBackedPractitionerHandler implements FhirResourceHandler<Practitioner> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-provider";
	
	private static final String BACKING_KEY = OPENMRS_HANDLER_PRACTITIONER_PROVIDER_BACKING_KEY;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirOpenmrsProviderService providerService;
	
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
		// The OLD orchestrator routed every create to the provider backing — there's no
		// content-level discriminator (no analogue of encounter.type[].coding.system). The
		// provider handler claims by default; an external module wanting to override or add a
		// new backing should use meta.profile to discriminate.
		return true;
	}
	
	@Override
	public Practitioner get(@Nonnull String uuid) {
		return providerService.get(uuid);
	}
	
	@Nonnull
	@Override
	public List<Practitioner> get(@Nonnull Collection<String> uuids) {
		return providerService.get(uuids);
	}
	
	@Override
	public boolean exists(@Nonnull String uuid) {
		return providerService.exists(uuid);
	}
	
	@Override
	public Practitioner create(@Nonnull Practitioner newResource) {
		return providerService.create(newResource);
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource) {
		return providerService.update(uuid, updatedResource);
	}
	
	@Override
	public Practitioner update(@Nonnull String uuid, @Nonnull Practitioner updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		return providerService.update(uuid, updatedResource, requestDetails, createIfNotExists);
	}
	
	@Override
	public Practitioner patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		return providerService.patch(uuid, patchType, body, requestDetails);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		providerService.delete(uuid);
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return providerService.searchForPractitioners(params);
	}
}
