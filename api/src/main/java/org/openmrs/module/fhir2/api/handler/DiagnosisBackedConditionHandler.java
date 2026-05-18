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
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_HANDLER_CONDITION_DIAGNOSIS_BACKING_KEY;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Maps the FHIR {@link Condition} resource onto the OpenMRS {@code Diagnosis} domain object by
 * delegating every {@link org.openmrs.module.fhir2.api.FhirService} call to the
 * {@link FhirDiagnosisService}. Sibling of {@link ConditionBackedConditionHandler}.
 * <p>
 * Claims an incoming resource on writes when {@link FhirUtils#getOpenmrsConditionType} resolves to
 * {@code DIAGNOSIS} ({@code encounter-diagnosis} category). Opts out of search when the
 * {@code category} parameter targets a coding in
 * {@link FhirConstants#CONDITION_CATEGORY_SYSTEM_URI} whose code isn't
 * {@code "encounter-diagnosis"}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DiagnosisBackedConditionHandler implements FhirResourceHandler<Condition> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-diagnosis";
	
	private static final String BACKING_KEY = OPENMRS_HANDLER_CONDITION_DIAGNOSIS_BACKING_KEY;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirDiagnosisService diagnosisService;
	
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
	public boolean canHandle(@Nonnull Condition condition) {
		return FhirUtils.getOpenmrsConditionType(condition).filter(type -> type == FhirUtils.OpenmrsConditionType.DIAGNOSIS)
		        .isPresent();
	}
	
	@Override
	public boolean acceptsSearch(@Nonnull SearchParameterMap params) {
		return !HandlerSupport.routingCategoryExcludes(params, FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		    FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
	}
	
	@Override
	public Condition get(@Nonnull String uuid) {
		return diagnosisService.get(uuid);
	}
	
	@Nonnull
	@Override
	public List<Condition> get(@Nonnull Collection<String> uuids) {
		return diagnosisService.get(uuids);
	}
	
	@Override
	public boolean exists(@Nonnull String uuid) {
		return diagnosisService.exists(uuid);
	}
	
	@Override
	public Condition create(@Nonnull Condition newResource) {
		return diagnosisService.create(newResource);
	}
	
	@Override
	public Condition update(@Nonnull String uuid, @Nonnull Condition updatedResource) {
		return diagnosisService.update(uuid, updatedResource);
	}
	
	@Override
	public Condition update(@Nonnull String uuid, @Nonnull Condition updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		return diagnosisService.update(uuid, updatedResource, requestDetails, createIfNotExists);
	}
	
	@Override
	public Condition patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		return diagnosisService.patch(uuid, patchType, body, requestDetails);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		diagnosisService.delete(uuid);
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return diagnosisService.searchDiagnoses(params);
	}
}
