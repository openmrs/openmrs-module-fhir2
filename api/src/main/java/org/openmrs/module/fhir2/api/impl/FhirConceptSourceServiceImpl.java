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
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.ConceptSource;
import org.openmrs.Duration;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class FhirConceptSourceServiceImpl implements FhirConceptSourceService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = { @Autowired, @VisibleForTesting })
	private FhirConceptSourceDao dao;
	
	@Override
	@Cacheable(value = "fhir2GetFhirConceptSources")
	public Collection<FhirConceptSource> getFhirConceptSources() {
		return dao.getFhirConceptSources();
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByUrl(@Nonnull String url) {
		return dao.getFhirConceptSourceByUrl(url);
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSource(@Nonnull ConceptSource conceptSource) {
		return dao.getFhirConceptSourceByConceptSource(conceptSource);
	}
	
	@Override
	public String getUrlForConceptSource(@Nonnull ConceptSource conceptSource) {
		return getFhirConceptSource(conceptSource).map(FhirConceptSource::getUrl)
		        .orElseGet(() -> Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())
		                ? FhirConstants.SNOMED_SYSTEM_URI
		                : null);
	}
	
	@Override
	public Optional<ConceptSource> getConceptSourceByUrl(@Nonnull String url) {
		if (url == null) {
			return Optional.empty();
		}
		
		Optional<FhirConceptSource> fhirConceptSource = getFhirConceptSourceByUrl(url);
		if (fhirConceptSource.isPresent()) {
			return Optional.ofNullable(fhirConceptSource.get().getConceptSource());
		}
		
		if (url.equals(FhirConstants.SNOMED_SYSTEM_URI)) {
			return dao.getConceptSourceByHl7Code(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
		}
		
		return Optional.empty();
	}
	
	@Override
	public Optional<ConceptSource> getConceptSourceByHl7Code(@Nonnull String hl7Code) {
		return dao.getConceptSourceByHl7Code(hl7Code);
	}
	
	@Override
	@CacheEvict(value = "fhir2GetFhirConceptSources", allEntries = true)
	public FhirConceptSource saveFhirConceptSource(@Nonnull FhirConceptSource fhirConceptSource) {
		return dao.saveFhirConceptSource(fhirConceptSource);
	}
}
