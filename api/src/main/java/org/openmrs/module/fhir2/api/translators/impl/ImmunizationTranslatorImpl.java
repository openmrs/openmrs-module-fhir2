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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.fhir2.FhirActivator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ImmunizationTranslatorImpl extends BaseImmunizationTranslator implements ImmunizationTranslator {
	
	@Autowired
	public ImmunizationTranslatorImpl(ConceptService conceptService) {
		super(conceptService);
	}
	
	public static final String immunizationGroupingConcept = "CIEL:1421";
	
	public static final String[] immunizationConcepts = { "CIEL:984", "CIEL:1410", "CIEL:1418", "CIEL:1419", "CIEL:1420",
	        "CIEL:165907" };
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	//	@Autowired
	//	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Visit> visitReferenceTranslator;
	
	@Autowired
	private ProviderService providerService;
	
	/**
	 * Sets the appropriate obs value based on the obs' concept and by looking at the FHIR immunization
	 * resource.
	 * 
	 * @param immunization The FHIR immunization resource
	 * @param obs The immunization obs group member.
	 */
	private void setObsValue(Immunization immunization, Obs obs) {
		
		if (concept("CIEL:984").getUuid() == obs.getConcept().getUuid()) {
			// the one system-less coding is the pointer to an OpenMRS concept UUID
			Coding coding = immunization.getVaccineCode().getCoding().stream()
			        .filter(code -> StringUtils.isEmpty(code.getSystem())).reduce((code1, code2) -> {
				        throw new IllegalArgumentException(
				                "Multiple system-less coding found for the immunization's vaccine: " + code1.getCode()
				                        + " and " + code2.getCode()
				                        + ". No unique system concept could be identified as the coded answer.");
			        }).get();
			obs.setValueCoded(getConceptService().getConceptByUuid(coding.getCode()));
		}
		
		if (concept("CIEL:1410").getUuid() == obs.getConcept().getUuid()) {
			obs.setValueDatetime(immunization.getOccurrenceDateTimeType().getValue());
		}
		
		if (concept("CIEL:1418").getUuid() == obs.getConcept().getUuid()) {
			if (CollectionUtils.size(immunization.getProtocolApplied()) != 1) {
				throw new IllegalArgumentException(
				        "Either no protocol applied was found or multiple protocols applied were found. Only strictly one protocol is currently supported for each immunization.");
			}
			ImmunizationProtocolAppliedComponent protocolApplied = immunization.getProtocolApplied().get(0);
			obs.setValueNumeric(protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue());
		}
		
		if (concept("CIEL:1419").getUuid() == obs.getConcept().getUuid()) {
			obs.setValueText(immunization.getManufacturer().getDisplay());
		}
		
		if (concept("CIEL:1420").getUuid() == obs.getConcept().getUuid()) {
			obs.setValueText(immunization.getLotNumber());
		}
		
		if (concept("CIEL:165907").getUuid() == obs.getConcept().getUuid()) {
			obs.setValueDatetime(immunization.getExpirationDate());
		}
	}
	
	@Override
	public Obs toOpenmrsType(Immunization fhirImmunization) {
		return toOpenmrsType(newImmunizationObsGroup(), fhirImmunization);
	}
	
	@Override
	public Obs toOpenmrsType(Obs openMrsImmunization, Immunization fhirImmunization) {
		
		if (openMrsImmunization.getId() != null) {
			validateImmunizationObsGroup(openMrsImmunization);
		}
		
		Patient patient = patientReferenceTranslator.toOpenmrsType(fhirImmunization.getPatient());
		List<ImmunizationPerformerComponent> performers = fhirImmunization.getPerformer();
		
		if (CollectionUtils.size(performers) != 1) {
			throw new IllegalArgumentException(
			        "Either no performer was found or multiple performers were found. Only strictly one performer is currently supported for each immunization.");
		}
		ImmunizationPerformerComponent performer = performers.get(0);
		
		//		Provider provider = practitionerReferenceTranslator.toOpenmrsType(performer.getActor());
		Provider provider = providerService.getProviderByUuid(getProviderUuid(performer));
		
		Visit visit = visitReferenceTranslator.toOpenmrsType(fhirImmunization.getEncounter());
		Location location = visit.getLocation();
		
		if (!patient.equals(visit.getPatient())) {
			throw new IllegalArgumentException(
			        "The visit '" + visit.getUuid() + "' does not belong to patient '" + patient.getUuid() + "'.");
		}
		
		EncounterType encounterType = FhirActivator.getImmunizationsEncounterTypeOrCreateIfMissing();
		EncounterRole encounterRole = FhirActivator.getAdministeringEncounterRoleOrCreateIfMissing();
		
		// taking the visit's most recent immunization encounter
		Optional<Encounter> encounter = visit.getEncounters().stream()
		        .filter(e -> encounterType.equals(e.getEncounterType()))
		        .max(Comparator.comparing(Encounter::getEncounterDatetime));
		
		final Encounter newEncounter = new Encounter();
		newEncounter.setVisit(visit);
		newEncounter.setLocation(location);
		newEncounter.setEncounterType(encounterType);
		newEncounter.setPatient(patient);
		newEncounter.setProvider(encounterRole, provider);
		if (visit != null && visit.getStopDatetime() != null) {
			newEncounter.setEncounterDatetime(visit.getStopDatetime());
		} else {
			newEncounter.setEncounterDatetime(new Date());
		}
		
		openMrsImmunization.setPerson(patient);
		openMrsImmunization.setLocation(location);
		openMrsImmunization.setEncounter(encounter.orElse(newEncounter));
		openMrsImmunization.setObsDatetime(new Date());
		openMrsImmunization.getGroupMembers().stream().forEach(obs -> {
			obs.setPerson(patient);
			obs.setLocation(location);
			obs.setEncounter(encounter.orElse(newEncounter));
			obs.setObsDatetime(openMrsImmunization.getObsDatetime());
			setObsValue(fhirImmunization, obs);
		});
		
		return openMrsImmunization;
	}
	
	@Override
	public Immunization toFhirResource(Obs openMrsImmunization) {
		Immunization immunization = new Immunization();
		immunization.setId(openMrsImmunization.getUuid());
		
		openMrsImmunization.getGroupMembers().forEach(obs -> {});
		
		// TODO Auto-generated method stub
		return immunization;
	}
	
}
