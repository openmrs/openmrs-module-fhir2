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

import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationConcepts;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationGroupingConcept;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.openmrs.Concept;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.FhirActivator;

@Setter(AccessLevel.PACKAGE)
public class BaseImmunizationTranslator {
	
	private ConceptService conceptService;
	
	public BaseImmunizationTranslator(ConceptService conceptService) {
		this.conceptService = conceptService;
	}
	
	public ConceptService getConceptService() {
		return conceptService;
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
		
		Arrays.asList(immunizationConcepts).stream().forEach(refTerm -> {
			Obs o = new Obs();
			o.setConcept(concept(refTerm));
			obs.addGroupMember(o);
		});
		
		return obs;
	}
	
	public Provider getAdministeringProvider(Obs obs) throws IllegalArgumentException {
		EncounterRole role = FhirActivator.getAdministeringEncounterRoleOrCreateIfMissing();
		return obs.getEncounter().getProvidersByRole(role).stream().findFirst()
		        .orElseThrow(() -> new IllegalArgumentException(new IllegalArgumentException(
		                "The immunization obs group should involve a single encounter provider with the role: "
		                        + role.getName() + ".")));
	}
	
	public void validateImmunizationObsGroup(Obs obs) throws IllegalArgumentException {
		
		if (concept(immunizationGroupingConcept).getUuid() != obs.getConcept().getUuid()) {
			throw new IllegalArgumentException("The immunization obs group should be defined by a concept mapped as same as "
			        + immunizationGroupingConcept + ".");
		}
		
		final Set<String> refConcepts = Sets.newHashSet(immunizationConcepts);
		// filtering the obs' concepts that are immunization concepts (but there could be others)
		List<String> obsConcepts = obs.getGroupMembers().stream().map(o -> o.getConcept().getUuid())
		        .filter(uuid -> refConcepts.contains(uuid)).collect(Collectors.toList());
		
		// each immunization concept should define only one obs of the group
		obsConcepts.stream().forEach(uuid -> {
			if (refConcepts.contains(uuid)) {
				refConcepts.remove(uuid);
			} else {
				throw new IllegalArgumentException("The immunization obs member defined by concept " + uuid
				        + " is found multiple times in the immunization obs group.");
			}
		});
	}
	
	/**
	 * @param obs An obs group
	 * @return A map CIEL reference term to obs of all obs group members
	 */
	public Map<String, Obs> getObsMembersMap(Obs obs) {
		Map<String, Obs> members = new HashMap<String, Obs>();
		obs.getGroupMembers().stream().forEach(o -> {
			Arrays.asList(immunizationConcepts).stream().forEach(refTerm -> {
				if (concept(refTerm) == o.getConcept()) {
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
