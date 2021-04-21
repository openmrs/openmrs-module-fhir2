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

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("practitionerFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class PractitionerFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirPractitionerService practitionerService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Practitioner getPractitionerById(@IdParam @Nonnull IdType id) {
		Practitioner practitioner = practitionerService.get(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner with Id " + id.getIdPart());
		}
		return practitioner;
	}
	
	@Create
	public MethodOutcome createPractitioner(@ResourceParam Practitioner practitioner) {
		return FhirProviderUtils.buildCreate(practitionerService.create(practitioner));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updatePractitioner(@IdParam IdType id, @ResourceParam Practitioner practitioner) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		practitioner.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(practitionerService.update(id.getIdPart(), practitioner));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deletePractitioner(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Practitioner practitioner = practitionerService.delete(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(practitioner);
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getPractitionerHistoryById(@IdParam @Nonnull IdType id) {
		Practitioner practitioner = practitionerService.get(id.getIdPart());
		if (practitioner == null) {
			throw new ResourceNotFoundException("Could not find practitioner with Id " + id.getIdPart());
		}
		return practitioner.getContained();
	}
	
	@Search
	public IBundleProvider searchForPractitioners(@OptionalParam(name = Practitioner.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenAndListParam identifier,
	        @OptionalParam(name = Practitioner.SP_GIVEN) StringAndListParam given,
	        @OptionalParam(name = Practitioner.SP_FAMILY) StringAndListParam family,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Practitioner.SP_ADDRESS_COUNTRY) StringAndListParam country,
	        @OptionalParam(name = Practitioner.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(reverse = true, allow = { "Encounter:" + Encounter.SP_PARTICIPANT,
	                "MedicationRequest:" + MedicationRequest.SP_REQUESTER,
	                "ServiceRequest:" + ServiceRequest.SP_REQUESTER }) HashSet<Include> revIncludes) {
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return practitionerService.searchForPractitioners(identifier, name, given, family, city, state, postalCode, country,
		    id, lastUpdated, revIncludes);
	}
}
