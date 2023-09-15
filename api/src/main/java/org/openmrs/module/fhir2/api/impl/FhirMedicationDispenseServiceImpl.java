/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirMedicationDispenseServiceImpl implements FhirMedicationDispenseService {
	
	@Override
	public MedicationDispense get(@Nonnull String uuid) {
		throw new ResourceNotFoundException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public List<MedicationDispense> get(@Nonnull Collection<String> uuids) {
		throw new ResourceNotFoundException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public MedicationDispense create(@Nonnull MedicationDispense newResource) {
		throw new UnsupportedOperationException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public MedicationDispense update(@Nonnull String uuid, @Nonnull MedicationDispense updatedResource) {
		throw new UnsupportedOperationException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public MedicationDispense patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		throw new UnsupportedOperationException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		throw new UnsupportedOperationException("MedicationDispense is not available in OpenMRS versions < 2.6.0");
	}
	
	@Override
	public IBundleProvider searchMedicationDispenses(MedicationDispenseSearchParams searchParams) {
		return new SimpleBundleProvider();
	}
}
