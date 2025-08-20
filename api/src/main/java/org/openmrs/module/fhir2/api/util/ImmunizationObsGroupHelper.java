/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.FhirConstants.ADMINISTERING_ENCOUNTER_ROLE_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_CONCEPTS;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_FREE_TEXT_COMMENT_CONCEPT;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_GROUPING_CONCEPT;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_NEXT_DOSE_DATE_CONCEPT_CODE;
import static org.openmrs.module.fhir2.api.util.FhirUtils.createExceptionErrorOperationOutcome;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImmunizationObsGroupHelper {
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptService conceptService;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObsService obsService;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterService encounterService;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	public static UnprocessableEntityException createImmunizationRequestValidationError(@Nonnull String errMsg) {
		return new UnprocessableEntityException(errMsg, createExceptionErrorOperationOutcome(errMsg));
	}
	
	public static NotImplementedOperationException createImmunizationRequestSetupError(@Nonnull String errMsg) {
		return new NotImplementedOperationException(errMsg, createExceptionErrorOperationOutcome(errMsg));
	}
	
	public EncounterType getImmunizationsEncounterType() {
		String uuid = globalPropertyService.getGlobalProperty(IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY);
		return Optional.ofNullable(encounterService.getEncounterTypeByUuid(uuid)).orElseThrow(
		    () -> createImmunizationRequestSetupError(
		        "The Immunization resource requires an immunizations encounter type to be defined in the global property '"
		                + IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY
		                + "', but no immunizations encounter type is defined for this instance."));
	}
	
	public EncounterRole getAdministeringEncounterRole() {
		String uuid = globalPropertyService.getGlobalProperty(ADMINISTERING_ENCOUNTER_ROLE_PROPERTY);
		return Optional.ofNullable(encounterService.getEncounterRoleByUuid(uuid)).orElseThrow(
		    () -> createImmunizationRequestSetupError(
		        "The Immunization resource requires an administering encounter role to be defined in the global property '"
		                + ADMINISTERING_ENCOUNTER_ROLE_PROPERTY
		                + "', but no administering encounter role is defined for this instance."));
	}
	
	public Concept conceptOrNull(String refTerm) {
		return getConceptFromMapping(refTerm).orElse(null);
	}
	
	public Concept concept(String refTerm) {
		Set<String> directRefTerms = new HashSet<>(
		        Arrays.asList(IMMUNIZATION_FREE_TEXT_COMMENT_CONCEPT, IMMUNIZATION_NEXT_DOSE_DATE_CONCEPT_CODE));
		if (directRefTerms.contains(refTerm)) {
			return conceptOrNull(refTerm);
		}
		
		return getConceptFromMapping(refTerm).orElseThrow(
		    () -> createImmunizationRequestSetupError("The Immunization resource requires a concept mapped to '" + refTerm
		            + "', however either multiple concepts are mapped to that term or not concepts are mapped to that term."));
	}
	
	public Obs newImmunizationObsGroup() {
		Obs obs = new Obs();
		obs.setConcept(concept(IMMUNIZATION_GROUPING_CONCEPT));
		obs.setObsDatetime(new Date());
		
		IMMUNIZATION_CONCEPTS.forEach(refTerm -> {
			Obs o = new Obs();
			o.setConcept(concept(refTerm));
			o.setObsDatetime(obs.getObsDatetime());
			obs.addGroupMember(o);
		});
		
		return obs;
	}
	
	public Obs addNewObs(Obs immunizationObsGroup, String obsCode) {
		Obs obs = new Obs(immunizationObsGroup.getPerson(), concept(obsCode), immunizationObsGroup.getObsDatetime(),
		        immunizationObsGroup.getLocation());
		obs.setEncounter(immunizationObsGroup.getEncounter());
		immunizationObsGroup.addGroupMember(obs);
		return obs;
	}
	
	public Obs replaceObs(Obs immunizationObsGroup, Obs previousObs) {
		Obs result = Obs.newInstance(previousObs);
		result.setPreviousVersion(previousObs);
		obsService.voidObs(previousObs, "Value updated");
		immunizationObsGroup.removeGroupMember(previousObs);
		immunizationObsGroup.addGroupMember(result);
		return result;
	}
	
	public Provider getAdministeringProvider(Obs obs) {
		EncounterRole role = getAdministeringEncounterRole();
		return obs.getEncounter().getProvidersByRole(role).stream().findFirst().orElseThrow(
		    () -> createImmunizationRequestValidationError(
		        "The Immunization resource is required to be attached to an OpenMRS encounter involving a single encounter provider with the role '"
		                + role.getName() + "'. This is not the case for immunization '" + obs.getUuid()
		                + "' attached to encounter '" + obs.getEncounter().getUuid() + "'."));
	}
	
	public void validateImmunizationObsGroup(Obs obs) {
		if (!concept(IMMUNIZATION_GROUPING_CONCEPT).equals(obs.getConcept())) {
			throw createImmunizationRequestSetupError(
			    "The Immunization resource requires the underlying OpenMRS immunization obs group to be defined by a concept mapped as same as "
			            + IMMUNIZATION_GROUPING_CONCEPT + ". That is not the case for obs '" + obs.getUuid()
			            + "' that is defined by the concept named '" + obs.getConcept().getName().toString() + "'.");
		}
		
		final Set<String> refConcepts = IMMUNIZATION_CONCEPTS.stream().map(this::getConceptFromMapping)
		        .filter(Optional::isPresent).map(Optional::get).map(BaseOpenmrsObject::getUuid).collect(Collectors.toSet());
		
		// filtering the obs' concepts that are immunization concepts (but there could be others)
		List<String> obsConcepts = obs.getGroupMembers().stream().map(o -> o.getConcept().getUuid())
		        .filter(refConcepts::contains).collect(Collectors.toList());
		
		// each immunization concept should define only one obs of the group
		obsConcepts.forEach(uuid -> {
			if (refConcepts.contains(uuid)) {
				refConcepts.remove(uuid);
			} else {
				throw createImmunizationRequestValidationError("The immunization obs member defined by concept with UUID '"
				        + uuid + "' is found multiple times in the immunization obs group.");
			}
		});
		
		// Validate.isTrue(refConcepts.size() == 0);
	}
	
	/**
	 * @param obs An obs group
	 * @return A mapping from CIEL reference terms to obs of all obs group members
	 */
	public Map<String, Obs> getObsMembersMap(Obs obs) {
		if (obs == null || !obs.isObsGrouping() || !obs.hasGroupMembers()) {
			return Collections.emptyMap();
		}
		
		Map<Concept, String> concepts = IMMUNIZATION_CONCEPTS.stream()
		        .collect(Collectors.toMap(this::concept, refTerm -> refTerm));
		
		return obs.getGroupMembers().stream().filter(o -> concepts.containsKey(o.getConcept()))
		        .collect(Collectors.toMap(o -> concepts.get(o.getConcept()), o -> o));
	}
	
	private Optional<Concept> getConceptFromMapping(String refTerm) {
		if (StringUtils.isBlank(refTerm)) {
			return Optional.empty();
		}
		
		String[] mapping = refTerm.split(":", 2);
		
		if (mapping.length < 2) {
			return Optional.empty();
		}
		
		Concept result = conceptService.getConceptByMapping(mapping[1], mapping[0]);
		
		if (result == null) {
			return Optional.empty();
		}
		
		return Optional.of(result);
	}
}
