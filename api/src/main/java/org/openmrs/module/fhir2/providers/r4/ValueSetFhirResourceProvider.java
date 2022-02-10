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

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.Setter;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.fhir2.api.FhirValueSetService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("valueSetFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class ValueSetFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	FhirValueSetService fhirValueSetService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ValueSet.class;
	}
	
	@Read
	public ValueSet getValueSetByUuid(@IdParam @Nonnull IdType id) {
		ValueSet valueSet = fhirValueSetService.get(id.getIdPart());
		if (valueSet == null) {
			throw new ResourceNotFoundException("Could not find valueset with Id " + id.getIdPart());
		}
		return valueSet;
	}
	
	@Search
	public IBundleProvider searchValueSets(@OptionalParam(name = ValueSet.SP_TITLE) StringAndListParam title) {
		return fhirValueSetService.searchForValueSets(title);
	}
}
