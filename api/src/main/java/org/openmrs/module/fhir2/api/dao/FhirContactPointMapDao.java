/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import java.util.Optional;

import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.model.FhirContactPointMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirContactPointMapDao extends FhirDaoAop {
	
	@Authorized
	Optional<FhirContactPointMap> getFhirContactPointMapByUuid(String uuid);
	
	@Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
	Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(PersonAttributeType attributeType);
	
	@Authorized(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES)
	Optional<FhirContactPointMap> getFhirContactPointMapForLocationAttributeType(LocationAttributeType attributeType);
	
	@Authorized(PrivilegeConstants.GET_PROVIDER_ATTRIBUTE_TYPES)
	Optional<FhirContactPointMap> getFhirContactPointMapForProviderAttributeType(ProviderAttributeType attributeType);
	
	@Authorized(PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES)
	FhirContactPointMap saveFhirContactPointMapForPersonAttributeType(FhirContactPointMap contactPointMap);
	
	@Authorized(PrivilegeConstants.MANAGE_LOCATION_ATTRIBUTE_TYPES)
	FhirContactPointMap saveFhirContactPointMapForLocationAttributeType(FhirContactPointMap contactPointMap);
	
	/**
	 * Core has no privilege for managing provider attribute types, so this uses the one for providers.
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROVIDERS)
	FhirContactPointMap saveFhirContactPointMapForProviderAttributeType(FhirContactPointMap contactPointMap);
	
}
