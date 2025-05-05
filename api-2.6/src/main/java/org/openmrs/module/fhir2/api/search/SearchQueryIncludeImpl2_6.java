/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@OpenmrsProfile(openmrsPlatformVersion = "2.6.* - 9.*")
public class SearchQueryIncludeImpl2_6<U extends IBaseResource> extends SearchQueryIncludeImpl<U> {
	
	@Setter(onMethod_ = @Autowired)
	private FhirPatientService patientService;
	
	@Setter(onMethod_ = @Autowired)
	private FhirMedicationDispenseService medicationDispenseService;
	
	@Override
	protected IBundleProvider handleRevIncludeParam(Set<Include> includeSet, Set<Include> revIncludeSet,
	        ReferenceAndListParam referenceParams, Include revIncludeParam) {
		if (revIncludeParam.getParamName().equals(FhirConstants.INCLUDE_PRESCRIPTION_PARAMETER)) {
			return handlePrescriptionReverseInclude(referenceParams, revIncludeParam.getParamType());
		}
		
		return super.handleRevIncludeParam(includeSet, revIncludeSet, referenceParams, revIncludeParam);
	}
	
	@Override
	protected Set<IBaseResource> handlePatientInclude(List<U> resourceList, String paramType) {
		switch (paramType) {
			case FhirConstants.MEDICATION_DISPENSE:
				Set<String> uniquePatientUUIDs = resourceList.stream()
				        .map(resource -> getIdFromReference(((MedicationDispense) resource).getSubject()))
				        .filter(Objects::nonNull).collect(Collectors.toSet());
				
				return new LinkedHashSet<>(patientService.get(uniquePatientUUIDs));
		}
		
		return super.handlePatientInclude(resourceList, paramType);
	}
	
	protected IBundleProvider handlePrescriptionReverseInclude(ReferenceAndListParam params, String targetType) {
		switch (targetType) {
			case FhirConstants.MEDICATION_DISPENSE:
				MedicationDispenseSearchParams medicationDispenseSearchParams = new MedicationDispenseSearchParams();
				medicationDispenseSearchParams.setMedicationRequest(params);
				return medicationDispenseService.searchMedicationDispenses(medicationDispenseSearchParams);
		}
		
		return null;
	}
}
