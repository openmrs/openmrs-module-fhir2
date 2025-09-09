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

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
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
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ImmunizationFhirR4ResourceProvider")
@R4Provider
public class ImmunizationFhirResourceProvider implements IResourceProvider {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirImmunizationService immunizationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Immunization.class;
	}
	
	@Read
	public Immunization getImmunizationByUuid(@IdParam @Nonnull IdType id) {
		Immunization immunization = immunizationService.get(id.getIdPart());
		if (immunization == null) {
			throw new ResourceNotFoundException("Could not find Immunization with Id " + id.getIdPart());
		}
		return immunization;
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createImmunization(@ResourceParam Immunization newImmunization) {
		return FhirProviderUtils.buildCreate(immunizationService.create(newImmunization));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateImmunization(@IdParam IdType id, @ResourceParam Immunization existingImmunization) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update resource");
		}
		
		existingImmunization.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(immunizationService.update(id.getIdPart(), existingImmunization));
	}
	
	@Patch
	@SuppressWarnings("unused")
	public MethodOutcome patchImmunization(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch the Immunization resource");
		}
		
		Immunization immunization = immunizationService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(immunization);
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteImmunization(@IdParam @Nonnull IdType id) {
		immunizationService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchImmunizations(@OptionalParam(name = Immunization.SP_PATIENT, chainWhitelist = { "",
	        Patient.SP_IDENTIFIER }) ReferenceAndListParam patientParam, @Sort SortSpec sort) {
		return immunizationService.searchImmunizations(patientParam, sort);
	}
	
}
