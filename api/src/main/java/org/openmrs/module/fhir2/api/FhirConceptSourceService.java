/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Optional;

import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.model.FhirConceptSource;

public interface FhirConceptSourceService extends FhirHelperService {
	
	Collection<FhirConceptSource> getFhirConceptSources();
	
	Optional<FhirConceptSource> getFhirConceptSourceByUrl(@Nonnull String url);
	
	Optional<FhirConceptSource> getFhirConceptSource(@Nonnull ConceptSource conceptSource);
	
	String getUrlForConceptSource(@Nonnull ConceptSource conceptSource);
	
	Optional<ConceptSource> getConceptSourceByUrl(@Nonnull String url);
	
	Optional<ConceptSource> getConceptSourceByHl7Code(@Nonnull String hl7Code);
	
	FhirConceptSource saveFhirConceptSource(@Nonnull FhirConceptSource fhirConceptSource);
}
