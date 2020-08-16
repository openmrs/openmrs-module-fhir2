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

import static org.openmrs.module.fhir2.FhirConstants.ENCOUNTER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT;
import static org.openmrs.module.fhir2.FhirConstants.PRACTITIONER;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
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
import org.openmrs.api.context.Context;
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
	
	public static final String ciel984 = immunizationConcepts[0];
	
	public static final String ciel1410 = immunizationConcepts[1];
	
	public static final String ciel1418 = immunizationConcepts[2];
	
	public static final String ciel1419 = immunizationConcepts[3];
	
	public static final String ciel1420 = immunizationConcepts[4];
	
	public static final String ciel165907 = immunizationConcepts[5];
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Visit> visitReferenceTranslator;
	
	@Autowired
	private ProviderService providerService;
	
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
		
		//      Provider provider = practitionerReferenceTranslator.toOpenmrsType(performer.getActor());
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
			newEncounter.setEncounterDatetime(openMrsImmunization.getObsDatetime());
		}
		
		openMrsImmunization.setPerson(patient);
		openMrsImmunization.setLocation(location);
		openMrsImmunization.setEncounter(encounter.orElse(newEncounter));
		openMrsImmunization.getGroupMembers().stream().forEach(obs -> {
			obs.setPerson(patient);
			obs.setLocation(location);
			obs.setEncounter(encounter.orElse(newEncounter));
		});
		
		Map<String, Obs> members = getObsMembersMap(openMrsImmunization);
		
		Coding coding = fhirImmunization.getVaccineCode().getCoding().stream()
		        .filter(code -> StringUtils.isEmpty(code.getSystem())).reduce((code1, code2) -> {
			        throw new IllegalArgumentException("Multiple system-less coding found for the immunization's vaccine: "
			                + code1.getCode() + " and " + code2.getCode()
			                + ". No unique system concept could be identified as the coded answer.");
		        }).get();
		members.get(ciel984).setValueCoded(getConceptService().getConceptByUuid(coding.getCode()));
		
		members.get(ciel1410).setValueDatetime(fhirImmunization.getOccurrenceDateTimeType().getValue());
		
		if (CollectionUtils.size(fhirImmunization.getProtocolApplied()) != 1) {
			throw new IllegalArgumentException(
			        "Either no protocol applied was found or multiple protocols applied were found. Only strictly one protocol is currently supported for each immunization.");
		}
		ImmunizationProtocolAppliedComponent protocolApplied = fhirImmunization.getProtocolApplied().get(0);
		members.get(ciel1418).setValueNumeric(protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue());
		
		members.get(ciel1419).setValueText(fhirImmunization.getManufacturer().getDisplay());
		
		members.get(ciel1420).setValueText(fhirImmunization.getLotNumber());
		
		members.get(ciel165907).setValueDatetime(fhirImmunization.getExpirationDate());
		
		return openMrsImmunization;
	}
	
	@Override
	public Immunization toFhirResource(Obs openMrsImmunization) {
		validateImmunizationObsGroup(openMrsImmunization);
		
		Immunization immunization = new Immunization();
		immunization.setId(openMrsImmunization.getUuid());
		immunization.setStatus(ImmunizationStatus.COMPLETED);
		immunization.setPatient(
		    new Reference().setType(PATIENT).setReference(PATIENT + "/" + openMrsImmunization.getPerson().getUuid()));
		immunization.setEncounter(new Reference().setType(ENCOUNTER)
		        .setReference(ENCOUNTER + "/" + openMrsImmunization.getEncounter().getVisit().getUuid()));
		immunization.setPerformer(Arrays.asList(new ImmunizationPerformerComponent(new Reference().setType(PRACTITIONER)
		        .setReference(PRACTITIONER + "/" + getAdministeringProvider(openMrsImmunization).getUuid()))));
		
		Map<String, Obs> members = getObsMembersMap(openMrsImmunization);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(members.get(ciel984).getValueCoded().getUuid());
		coding.setDisplay(members.get(ciel984).getValueCoded().getName(Context.getLocale()).getName());
		codeableConcept.addCoding(coding);
		immunization.setVaccineCode(codeableConcept);
		immunization.setOccurrence(new DateTimeType(members.get(ciel1410).getValueDatetime()));
		immunization.addProtocolApplied(new ImmunizationProtocolAppliedComponent(
		        new PositiveIntType((long) members.get(ciel1418).getValueNumeric().doubleValue())));
		immunization.setManufacturer(new Reference().setDisplay(members.get(ciel1419).getValueText()));
		immunization.setLotNumber(members.get(ciel1420).getValueText());
		immunization.setExpirationDate(members.get(ciel165907).getValueDatetime());
		
		return immunization;
	}
	
	@Override
	public Concept getOpenmrsImmunizationConcept() {
		return concept(immunizationGroupingConcept);
	}
	
}
