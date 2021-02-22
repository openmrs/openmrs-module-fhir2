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

import static org.openmrs.module.fhir2.api.util.FhirUtils.createExceptionErrorOperationOutcome;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ImmunizationTranslatorImpl implements ImmunizationTranslator {
	
	public static final String immunizationGroupingConcept = "CIEL:1421";
	
	public static final List<String> immunizationConcepts = Collections.unmodifiableList(
	    Arrays.asList(new String[] { "CIEL:984", "CIEL:1410", "CIEL:1418", "CIEL:1419", "CIEL:1420", "CIEL:165907" }));
	
	public static final String ciel984 = immunizationConcepts.get(0);
	
	public static final String ciel1410 = immunizationConcepts.get(1);
	
	public static final String ciel1418 = immunizationConcepts.get(2);
	
	public static final String ciel1419 = immunizationConcepts.get(3);
	
	public static final String ciel1420 = immunizationConcepts.get(4);
	
	public static final String ciel165907 = immunizationConcepts.get(5);
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ImmunizationObsGroupHelper helper;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Visit> visitReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Autowired
	private ObservationValueTranslator observationValueTranslator;
	
	@Override
	public Obs toOpenmrsType(Immunization fhirImmunization) {
		return toOpenmrsType(helper.newImmunizationObsGroup(), fhirImmunization);
	}
	
	@Override
	public Obs toOpenmrsType(Obs openMrsImmunization, Immunization fhirImmunization) {
		
		if (openMrsImmunization.getId() != null) {
			helper.validateImmunizationObsGroup(openMrsImmunization);
		}
		
		Patient patient = patientReferenceTranslator.toOpenmrsType(fhirImmunization.getPatient());
		List<ImmunizationPerformerComponent> performers = fhirImmunization.getPerformer();
		
		if (CollectionUtils.size(performers) != 1) {
			String errMsg = "Either no performer was found or multiple performers were found. Only strictly one performer is currently supported for each immunization.";
			throw new InvalidRequestException(errMsg, createExceptionErrorOperationOutcome(errMsg));
		}
		ImmunizationPerformerComponent performer = performers.get(0);
		
		Provider provider = practitionerReferenceTranslator.toOpenmrsType(performer.getActor());
		
		Visit visit = visitReferenceTranslator.toOpenmrsType(fhirImmunization.getEncounter());
		Location location = visit.getLocation();
		
		if (!patient.equals(visit.getPatient())) {
			String errMsg = "The visit '" + visit.getUuid() + "' does not belong to patient '" + patient.getUuid() + "'.";
			throw new InvalidRequestException(errMsg, createExceptionErrorOperationOutcome(errMsg));
		}
		
		EncounterType encounterType = helper.getImmunizationsEncounterType();
		EncounterRole encounterRole = helper.getAdministeringEncounterRole();
		
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
		
		Map<String, Obs> members = helper.getObsMembersMap(openMrsImmunization);
		
		Coding coding = fhirImmunization.getVaccineCode().getCoding().stream()
		        .filter(code -> StringUtils.isEmpty(code.getSystem())).reduce((code1, code2) -> {
			        String errMsg = "Multiple system-less coding found for the immunization's vaccine: " + code1.getCode()
			                + " and " + code2.getCode()
			                + ". No unique system concept could be identified as the coded answer.";
			        throw new InvalidRequestException(errMsg, createExceptionErrorOperationOutcome(errMsg));
		        }).get();
		members.get(ciel984).setValueCoded(conceptService.getConceptByUuid(coding.getCode()));
		
		members.get(ciel1410).setValueDatetime(fhirImmunization.getOccurrenceDateTimeType().getValue());
		
		if (CollectionUtils.size(fhirImmunization.getProtocolApplied()) != 1) {
			String errMsg = "Either no protocol applied was found or multiple protocols applied were found. Only strictly one protocol is currently supported for each immunization.";
			throw new InvalidRequestException(errMsg, createExceptionErrorOperationOutcome(errMsg));
		}
		ImmunizationProtocolAppliedComponent protocolApplied = fhirImmunization.getProtocolApplied().get(0);
		members.get(ciel1418).setValueNumeric(protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue());
		
		members.get(ciel1419).setValueText(fhirImmunization.getManufacturer().getDisplay());
		
		members.get(ciel1420).setValueText(fhirImmunization.getLotNumber());
		
		members.get(ciel165907).setValueDatetime(fhirImmunization.getExpirationDate());
		
		return openMrsImmunization;
	}
	
	@Override
	public Immunization toFhirResource(@Nonnull Obs openMrsImmunization) {
		helper.validateImmunizationObsGroup(openMrsImmunization);
		
		Immunization immunization = new Immunization();
		immunization.setId(openMrsImmunization.getUuid());
		immunization.setStatus(ImmunizationStatus.COMPLETED);
		immunization.setPatient(patientReferenceTranslator.toFhirResource(new Patient(openMrsImmunization.getPerson())));
		immunization.setEncounter(visitReferenceTranslator.toFhirResource(openMrsImmunization.getEncounter().getVisit()));
		immunization.setPerformer(Arrays.asList(new ImmunizationPerformerComponent(
		        practitionerReferenceTranslator.toFhirResource(helper.getAdministeringProvider(openMrsImmunization)))));
		
		Map<String, Obs> members = helper.getObsMembersMap(openMrsImmunization);
		
		immunization.setVaccineCode(conceptTranslator.toFhirResource(members.get(ciel984).getValueCoded()));
		immunization.setOccurrence(observationValueTranslator.toFhirResource(members.get(ciel1410)));
		immunization.addProtocolApplied(new ImmunizationProtocolAppliedComponent(
		        new PositiveIntType((long) members.get(ciel1418).getValueNumeric().doubleValue())));
		immunization.setManufacturer(new Reference().setDisplay(members.get(ciel1419).getValueText()));
		immunization.setLotNumber(members.get(ciel1420).getValueText());
		immunization.setExpirationDate(members.get(ciel165907).getValueDatetime());
		
		return immunization;
	}
}
