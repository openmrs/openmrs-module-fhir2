/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.List;

import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.util.FhirUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class LocationFhirResourceProvider implements IResourceProvider {
	
	@Inject
	FhirLocationService fhirLocationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Location.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Location getLocationById(@IdParam @NotNull IdType id) {
		Location location = fhirLocationService.getLocationByUuid(id.getIdPart());
		if (location == null) {
			throw new ResourceNotFoundException("Could not find location with Id " + id.getIdPart());
		}
		return location;
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationByName(@RequiredParam(name = Location.SP_NAME) StringParam name) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationByName(name.getValue()));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationByCity(@RequiredParam(name = Location.SP_ADDRESS_CITY) StringParam city) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationsByCity(city.getValue()));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationByCountry(@RequiredParam(name = Location.SP_ADDRESS_COUNTRY) StringParam country) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationsByCountry(country.getValue()));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationByPostalCode(@RequiredParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postalCode) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationsByPostalCode(postalCode.getValue()));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationByState(@RequiredParam(name = Location.SP_ADDRESS_STATE) StringParam state) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationsByState(state.getValue()));
	}
	
	@Search
	@SuppressWarnings("unused")
	public Bundle findLocationsByTag(@RequiredParam(name = "_tag") TokenParam tag) {
		return FhirUtils.convertSearchResultsToBundle(fhirLocationService.findLocationsByTag(tag));
	}
	
	@History
	@SuppressWarnings("unused")
	public List<Resource> getLocationHistoryById(@IdParam @NotNull IdType id) {
		Location location = fhirLocationService.getLocationByUuid(id.getIdPart());
		if (location == null) {
			throw new ResourceNotFoundException("Could not find location with Id " + id.getIdPart());
		}
		return location.getContained();
	}
}
