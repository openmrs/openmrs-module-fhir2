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
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_HANDLER_ENCOUNTER_ENCOUNTER_BACKING_KEY;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirOpenmrsEncounterService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Maps the FHIR {@link Encounter} resource onto the OpenMRS {@code Encounter} domain object by
 * delegating every {@link org.openmrs.module.fhir2.api.FhirService} call to the
 * {@link FhirOpenmrsEncounterService}. Sibling of {@link VisitBackedEncounterHandler}, which
 * applies the same composition pattern over {@code FhirVisitService}.
 * <p>
 * Claims an incoming resource on writes when its {@code type[].coding[].system} equals
 * {@link FhirConstants#ENCOUNTER_TYPE_SYSTEM_URI}. Opts out of search when the {@code _tag}
 * parameter contains a coding in the {@code OPENMRS_FHIR_EXT_ENCOUNTER_TAG} system whose value
 * isn't {@code "encounter"}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class EncounterBackedEncounterHandler implements FhirResourceHandler<Encounter> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-encounter";
	
	private static final String BACKING_KEY = OPENMRS_HANDLER_ENCOUNTER_ENCOUNTER_BACKING_KEY;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirOpenmrsEncounterService encounterService;
	
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
	public boolean canHandle(@Nonnull Encounter encounter) {
		return encounter.getType().stream().flatMap(t -> t.getCoding().stream())
		        .anyMatch(c -> FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI.equals(c.getSystem()));
	}
	
	@Override
	public boolean acceptsSearch(@Nonnull SearchParameterMap params) {
		return !HandlerSupport.routingTagExcludes(params, FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter");
	}
	
	@Override
	public Encounter get(@Nonnull String uuid) {
		return encounterService.get(uuid);
	}
	
	@Nonnull
	@Override
	public List<Encounter> get(@Nonnull Collection<String> uuids) {
		return encounterService.get(uuids);
	}
	
	@Override
	public boolean exists(@Nonnull String uuid) {
		return encounterService.exists(uuid);
	}
	
	@Override
	public Encounter create(@Nonnull Encounter newResource) {
		return encounterService.create(newResource);
	}
	
	@Override
	public Encounter update(@Nonnull String uuid, @Nonnull Encounter updatedResource) {
		return encounterService.update(uuid, updatedResource);
	}
	
	@Override
	public Encounter update(@Nonnull String uuid, @Nonnull Encounter updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		return encounterService.update(uuid, updatedResource, requestDetails, createIfNotExists);
	}
	
	@Override
	public Encounter patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		return encounterService.patch(uuid, patchType, body, requestDetails);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		encounterService.delete(uuid);
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return encounterService.searchForEncounters(params);
	}
}
