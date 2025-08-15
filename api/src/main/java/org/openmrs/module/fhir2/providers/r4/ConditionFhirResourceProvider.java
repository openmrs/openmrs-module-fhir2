/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("conditionFhirR4ResourceProvider")
@R4Provider
public class ConditionFhirResourceProvider implements IResourceProvider {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirConditionService conditionService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Condition.class;
	}
	
	@Read
	public Condition getConditionByUuid(@IdParam @Nonnull IdType id) {
		Condition condition = conditionService.get(id.getIdPart());
		if (condition == null) {
			throw new ResourceNotFoundException("Could not find condition with Id " + id.getIdPart());
		}
		return condition;
	}
	
	@Create
	public MethodOutcome createCondition(@ResourceParam Condition newCondition) {
		return FhirProviderUtils.buildCreate(conditionService.create(newCondition));
	}
	
	@Update
	public MethodOutcome updateCondition(@IdParam IdType id, @ResourceParam Condition updatedCondition) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		updatedCondition.setId(id);
		
		return FhirProviderUtils.buildUpdate(conditionService.update(id.getIdPart(), updatedCondition));
	}
	
	@Patch
	public MethodOutcome patchCondition(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch Condition");
		}
		
		Condition condition = conditionService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(condition);
	}
	
	@Delete
	public OperationOutcome deleteCondition(@IdParam IdType id) {
		conditionService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	public IBundleProvider searchConditions(
	        @OptionalParam(name = Condition.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_NAME,
	                Patient.SP_GIVEN, Patient.SP_FAMILY }) ReferenceAndListParam patientParam,
	        @OptionalParam(name = Condition.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_NAME,
	                Patient.SP_GIVEN, Patient.SP_FAMILY }) ReferenceAndListParam subjectParam,
	        @OptionalParam(name = Condition.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenAndListParam clinicalStatus,
	        @OptionalParam(name = Condition.SP_ONSET_DATE) DateRangeParam onsetDate,
	        @OptionalParam(name = Condition.SP_ONSET_AGE) QuantityAndListParam onsetAge,
	        @OptionalParam(name = Condition.SP_RECORDED_DATE) DateRangeParam recordedDate,
	        @OptionalParam(name = Condition.SP_CATEGORY) TokenAndListParam category,
	        @OptionalParam(name = Condition.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @IncludeParam(allow = { "Condition:" + Condition.SP_PATIENT }) HashSet<Include> includes) {
		if (patientParam == null) {
			patientParam = subjectParam;
		}
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return conditionService.searchConditions(new ConditionSearchParams(patientParam, code, clinicalStatus, onsetDate,
		        onsetAge, recordedDate, category, id, lastUpdated, sort, includes));
	}
	
}
