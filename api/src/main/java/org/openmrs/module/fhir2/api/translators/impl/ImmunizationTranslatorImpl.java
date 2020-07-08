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

import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ImmunizationTranslatorImpl implements ImmunizationTranslator {
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	public Obs newImmunizationObsGroup(ConceptService cs) {
		Obs obs = new Obs();
		
		Concept concept = cs.getConceptByMapping("1421", "CIEL"); // immunization history
		if (concept == null) {
			throw new IllegalStateException(" ++++ TO DO ++++ PUT SOMETHING USEFUL HERE");
		}
		obs.setConcept(concept);
		
		Arrays.asList(new String[] { "CIEL:984", "CIEL:1410", "CIEL:1418", "CIEL:1419", "CIEL:1420" }).stream()
		        .forEach(refTerm -> {
			        String[] mapping = refTerm.split(":");
			        Concept c = cs.getConceptByMapping(mapping[1], mapping[0]);
			        if (c == null) {
				        throw new IllegalStateException(" ++++ TO DO ++++ PUT SOMETHING USEFUL HERE");
			        }
			        Obs o = new Obs();
			        o.setConcept(c);
			        obs.addGroupMember(o);
		        });
		
		return obs;
	}
	
	@Override
	public Obs toOpenmrsType(Immunization fhirImmunization) {
		return this.toOpenmrsType(newImmunizationObsGroup(conceptService), fhirImmunization);
	}
	
	@Override
	public Obs toOpenmrsType(Obs openMrsImmunization, Immunization fhirImmunization) {
		
		Patient patient = patientReferenceTranslator.toOpenmrsType(fhirImmunization.getPatient());
		Location location = locationReferenceTranslator.toOpenmrsType(fhirImmunization.getLocation());
		List<ImmunizationPerformerComponent> performers = fhirImmunization.getPerformer();
		
		if (CollectionUtils.isEmpty(performers)) {
			throw new IllegalArgumentException(" ++++ TO DO ++++ PUT SOMETHING USEFUL HERE");
		}
		if (performers.size() != 1) {
			throw new IllegalArgumentException(" ++++ TO DO ++++ PUT SOMETHING USEFUL HERE");
		}
		ImmunizationPerformerComponent performer = performers.get(0);
		Provider provider = practitionerReferenceTranslator.toOpenmrsType(performer.getActor());
		
		openMrsImmunization.setPerson(patient);
		openMrsImmunization.setLocation(location);
		openMrsImmunization.getGroupMembers().stream().forEach(o -> {
			o.setPerson(patient);
			o.setLocation(location);
		});
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Immunization toFhirResource(Obs openMrsImmunization) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
