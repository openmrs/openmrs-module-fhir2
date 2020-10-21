/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.openmrs.module.fhir2.FhirConstants.ADMINISTERING_ENCOUNTER_ROLE_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationConcepts;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationGroupingConcept;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.openmrs.Concept;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ImmunizationObsGroupHelper {
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private AdministrationService adminService;
	
	public EncounterType getImmunizationsEncounterType() {
		String uuid = adminService.getGlobalProperty(IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY);
		return Optional.of(encounterService.getEncounterTypeByUuid(uuid))
		        .orElseThrow(() -> new IllegalStateException("No immunizations encounter type is defined."));
	}
	
	public EncounterRole getAdministeringEncounterRole() {
		String uuid = adminService.getGlobalProperty(ADMINISTERING_ENCOUNTER_ROLE_PROPERTY);
		return Optional.of(encounterService.getEncounterRoleByUuid(uuid))
		        .orElseThrow(() -> new IllegalStateException("No immunizations administering encounter role is defined."));
	}
	
	public Concept concept(String refTerm) throws IllegalStateException, APIException {
		String[] mapping = refTerm.split(":");
		return Optional.of(conceptService.getConceptByMapping(mapping[1], mapping[0])).orElseThrow(
		    () -> new IllegalStateException(
		            "The reference term '" + refTerm + "' is either mapped to no concepts or to more than one concept."));
	}
	
	public Obs newImmunizationObsGroup() {
		Obs obs = new Obs();
		obs.setConcept(concept(immunizationGroupingConcept));
		obs.setObsDatetime(new Date());
		
		immunizationConcepts.stream().forEach(refTerm -> {
			Obs o = new Obs();
			o.setConcept(concept(refTerm));
			o.setObsDatetime(obs.getObsDatetime());
			obs.addGroupMember(o);
		});
		
		return obs;
	}
	
	public Provider getAdministeringProvider(Obs obs) throws IllegalArgumentException {
		EncounterRole role = getAdministeringEncounterRole();
		return obs.getEncounter().getProvidersByRole(role).stream().findFirst()
		        .orElseThrow(() -> new IllegalArgumentException(
		                "The immunization obs group should involve a single encounter provider with the role: "
		                        + role.getName() + "."));
	}
	
	public void validateImmunizationObsGroup(Obs obs) throws IllegalArgumentException {
		
		if (!concept(immunizationGroupingConcept).equals(obs.getConcept())) {
			throw new IllegalArgumentException("The immunization obs group should be defined by a concept mapped as same as "
			        + immunizationGroupingConcept + ".");
		}
		
		final Set<String> refConcepts = immunizationConcepts.stream()
		        .map(m -> conceptService.getConceptByMapping(m.split(":")[1], m.split(":")[0])).map(c -> c.getUuid())
		        .collect(Collectors.toSet());
		
		// filtering the obs' concepts that are immunization concepts (but there could be others)
		List<String> obsConcepts = obs.getGroupMembers().stream().map(o -> o.getConcept().getUuid())
		        .filter(uuid -> refConcepts.contains(uuid)).collect(Collectors.toList());
		
		Validate.notEmpty(obsConcepts);
		// each immunization concept should define only one obs of the group
		obsConcepts.stream().forEach(uuid -> {
			if (refConcepts.contains(uuid)) {
				refConcepts.remove(uuid);
			} else {
				throw new IllegalArgumentException("The immunization obs member defined by concept " + uuid
				        + " is found multiple times in the immunization obs group.");
			}
		});
		Validate.isTrue(refConcepts.size() == 0);
	}
	
	/**
	 * @param obs An obs group
	 * @return A mapping from CIEL reference terms to obs of all obs group members
	 */
	public Map<String, Obs> getObsMembersMap(Obs obs) {
		Map<String, Obs> members = new HashMap<String, Obs>();
		obs.getGroupMembers().stream().forEach(o -> {
			immunizationConcepts.stream().forEach(refTerm -> {
				if (o.getConcept().equals(concept(refTerm))) {
					members.put(refTerm, o);
				}
			});
		});
		return members;
	}
	
	public String getProviderUuid(ImmunizationPerformerComponent performer) {
		if (performer.getActor() == null) {
			return "";
		}
		String ref = Optional.of(performer.getActor().getReference()).orElse("/");
		return StringUtils.split(ref, "/")[1];
	}
	
}
