/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import javax.validation.constraints.NotNull;

import java.util.List;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Condition30_40;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("conditionFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class ConditionFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirConditionService conditionService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Condition.class;
	}
	
	@Read
	public Condition getConditionById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Condition condition = conditionService.get(id.getIdPart());
		if (condition == null) {
			throw new ResourceNotFoundException("Could not find condition with Id " + id.getIdPart());
		}
		
		return Condition30_40.convertCondition(condition);
	}
	
	@History
	public List<Resource> getConditionHistoryById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Condition condition = conditionService.get(id.getIdPart());
		if (condition == null) {
			throw new ResourceNotFoundException("Could not find condition with Id " + id.getIdPart());
		}
		return Condition30_40.convertCondition(condition).getContained();
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createCondition(@ResourceParam Condition newCondition) {
		return FhirProviderUtils.buildCreate(
		    Condition30_40.convertCondition(conditionService.saveCondition(Condition30_40.convertCondition(newCondition))));
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
	        @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam recordedDate,
	        @OptionalParam(name = Condition.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort) {
		if (patientParam == null) {
			patientParam = subjectParam;
		}
		
		return conditionService.searchConditions(patientParam, code, clinicalStatus, onsetDate, onsetAge, recordedDate, id,
		    lastUpdated, sort);
	}
}
