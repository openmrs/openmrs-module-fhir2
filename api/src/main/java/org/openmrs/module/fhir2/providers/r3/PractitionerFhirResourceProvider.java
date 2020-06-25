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

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Practitioner30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("practitionerFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class PractitionerFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPractitionerService practitionerService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Practitioner getPractitionerById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Practitioner practitioner = practitionerService.get(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner with Id " + id.getIdPart());
		}
		
		return Practitioner30_40.convertPractitioner(practitioner);
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPractitionerHistoryById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.Practitioner practitioner = practitionerService.get(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner with Id " + id.getIdPart());
		}
		return Practitioner30_40.convertPractitioner(practitioner).getContained();
	}
	
	@Search
	public IBundleProvider searchForPractitioners(@OptionalParam(name = Practitioner.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenAndListParam identifier,
	        @OptionalParam(name = Practitioner.SP_GIVEN) StringAndListParam given,
	        @OptionalParam(name = Practitioner.SP_FAMILY) StringAndListParam family,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_COUNTRY) StringAndListParam country) {
		return practitionerService.searchForPractitioners(name, identifier, given, family, city, state, postalCode, country);
	}
	
}
