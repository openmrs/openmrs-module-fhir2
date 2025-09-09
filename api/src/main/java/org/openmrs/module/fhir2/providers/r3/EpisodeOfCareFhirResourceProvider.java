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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirEpisodeOfCareService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("episodeOfCareFhirR3ResourceProvider")
@R3Provider
public class EpisodeOfCareFhirResourceProvider implements IResourceProvider {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirEpisodeOfCareService episodeOfCareService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return EpisodeOfCare.class;
	}
	
	@Read
	public EpisodeOfCare getEpisodeOfCareById(@IdParam IdType id) {
		org.hl7.fhir.r4.model.EpisodeOfCare episodeOfCare = episodeOfCareService.get(id.getIdPart());
		if (episodeOfCare == null) {
			throw new ResourceNotFoundException("Could not find EpisodeOfCare with Id " + id.getIdPart());
		}
		return (EpisodeOfCare) VersionConvertorFactory_30_40.convertResource(episodeOfCare);
	}
}
