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
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.LocationSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("locationFhirR4ResourceProvider")
@R4Provider
public class LocationFhirResourceProvider extends BaseUpsertFhirResourceProvider<Location> {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirLocationService fhirLocationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Location.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Location getLocationById(@IdParam @Nonnull IdType id) {
		Location location = fhirLocationService.get(id.getIdPart());
		if (location == null) {
			throw new ResourceNotFoundException("Could not find location with Id " + id.getIdPart());
		}
		return location;
	}
	
	@Create
	public MethodOutcome createLocation(@ResourceParam Location location) {
		return FhirProviderUtils.buildCreate(fhirLocationService.create(location));
	}
	
	@Override
	public MethodOutcome doUpsert(IdType id, Location location, RequestDetails requestDetails, boolean createIfNotExists) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		
		return FhirProviderUtils
		        .buildUpdate(fhirLocationService.update(id.getIdPart(), location, requestDetails, createIfNotExists));
	}
	
	@Patch
	public MethodOutcome patchLocation(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch Location resource");
		}
		
		Location location = fhirLocationService.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(location);
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteLocation(@IdParam @Nonnull IdType id) {
		fhirLocationService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	public IBundleProvider searchLocations(@OptionalParam(name = Location.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Location.SP_ADDRESS_CITY) StringAndListParam city,
	        @OptionalParam(name = Location.SP_ADDRESS_COUNTRY) StringAndListParam country,
	        @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringAndListParam postalCode,
	        @OptionalParam(name = Location.SP_ADDRESS_STATE) StringAndListParam state,
	        @OptionalParam(name = "_tag") TokenAndListParam tag,
	        @OptionalParam(name = Location.SP_PARTOF, chainWhitelist = { "", Location.SP_NAME, Location.SP_ADDRESS_CITY,
	                Location.SP_ADDRESS_STATE, Location.SP_ADDRESS_COUNTRY,
	                Location.SP_ADDRESS_POSTALCODE }, targetTypes = Location.class) ReferenceAndListParam parent,
	        @OptionalParam(name = Location.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
	        @IncludeParam(allow = { "Location:" + Location.SP_PARTOF }) HashSet<Include> includes,
	        @IncludeParam(reverse = true, allow = { "Location:" + Location.SP_PARTOF,
	                "Encounter:" + Encounter.SP_LOCATION }) HashSet<Include> revIncludes,
	        @Sort SortSpec sort) {
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		if (CollectionUtils.isEmpty(revIncludes)) {
			revIncludes = null;
		}
		
		return (fhirLocationService.searchForLocations(new LocationSearchParams(name, city, country, postalCode, state, tag,
		        parent, id, lastUpdated, sort, includes, revIncludes)));
	}
}
