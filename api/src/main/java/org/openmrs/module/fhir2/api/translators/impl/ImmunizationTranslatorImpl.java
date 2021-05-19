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

import static org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper.createImmunizationRequestValidationError;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Coding;
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
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ImmunizationTranslatorImpl implements ImmunizationTranslator {
	
	public static final String IMMUNIZATION_GROUPING_CONCEPT = "CIEL:1421";
	
	public static final Set<String> IMMUNIZATION_CONCEPTS = ImmutableSet.of("CIEL:984", "CIEL:1410", "CIEL:1418",
	    "CIEL:1419", "CIEL:1420", "CIEL:165907");
	
	public static final String CIEL_984;
	
	public static final String CIEL_1410;
	
	public static final String CIEL_1418;
	
	public static final String CIEL_1419;
	
	public static final String CIEL_1420;
	
	public static final String CIEL_165907;
	
	static {
		final Iterator<String> conceptIterator = IMMUNIZATION_CONCEPTS.iterator();
		CIEL_984 = conceptIterator.next();
		CIEL_1410 = conceptIterator.next();
		CIEL_1418 = conceptIterator.next();
		CIEL_1419 = conceptIterator.next();
		CIEL_1420 = conceptIterator.next();
		CIEL_165907 = conceptIterator.next();
	}
	
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
	public Obs toOpenmrsType(@Nonnull Immunization fhirImmunization) {
		return toOpenmrsType(helper.newImmunizationObsGroup(), fhirImmunization);
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs openmrsImmunization, @Nonnull Immunization fhirImmunization) {
		if (openmrsImmunization == null) {
			return null;
		}
		
		if (fhirImmunization == null) {
			return openmrsImmunization;
		}
		
		Patient patient = patientReferenceTranslator.toOpenmrsType(fhirImmunization.getPatient());
		if (patient == null) {
			final String errMsg;
			if (fhirImmunization.getPatient().hasReference()) {
				errMsg = "Could not find patient matching " + fhirImmunization.getPatient().getReference();
			} else {
				errMsg = "No patient was specified for this request";
			}
			
			throw createImmunizationRequestValidationError(errMsg);
		}
		
		List<ImmunizationPerformerComponent> performers = fhirImmunization.getPerformer();
		Provider provider = null;
		if (performers.size() > 1) {
			throw createImmunizationRequestValidationError(
			    "More than one performer was specified. Only a single performer is currently supported for each immunization.");
		} else if (performers.size() == 1) {
			ImmunizationPerformerComponent performer = performers.get(0);
			if (performer != null && performer.hasActor()) {
				provider = practitionerReferenceTranslator.toOpenmrsType(performer.getActor());
			}
		}
		
		final Visit visit;
		if (fhirImmunization.hasEncounter()) {
			visit = visitReferenceTranslator.toOpenmrsType(fhirImmunization.getEncounter());
		} else {
			visit = null;
		}
		
		if (visit == null) {
			final String errMsg;
			if (fhirImmunization.getEncounter().hasReference()) {
				errMsg = "Could not find visit matching " + fhirImmunization.getEncounter().getReference();
			} else {
				errMsg = "No encounter was specified for this request";
			}
			
			throw createImmunizationRequestValidationError(errMsg);
		}
		
		Location location = visit.getLocation();
		
		if (!patient.equals(visit.getPatient())) {
			throw createImmunizationRequestValidationError(
			    "The visit '" + visit.getUuid() + "' does not belong to patient '" + patient.getUuid() + "'.");
		}
		
		EncounterType encounterType = helper.getImmunizationsEncounterType();
		
		// taking the visit's most recent immunization encounter
		Optional<Encounter> existingEncounter = visit.getEncounters().stream()
		        .filter(e -> encounterType.equals(e.getEncounterType()))
		        .max(Comparator.comparing(Encounter::getEncounterDatetime));
		
		final Provider encounterProvider = provider;
		Encounter encounter = existingEncounter.orElseGet(() -> {
			final EncounterRole encounterRole = helper.getAdministeringEncounterRole();
			final Encounter newEncounter = new Encounter();
			newEncounter.setVisit(visit);
			newEncounter.setLocation(location);
			newEncounter.setEncounterType(encounterType);
			newEncounter.setPatient(patient);
			if (encounterProvider != null) {
				newEncounter.setProvider(encounterRole, encounterProvider);
			}
			
			if (visit.getStopDatetime() != null) {
				newEncounter.setEncounterDatetime(visit.getStopDatetime());
			} else {
				newEncounter.setEncounterDatetime(openmrsImmunization.getObsDatetime());
			}
			
			return newEncounter;
		});
		
		openmrsImmunization.setPerson(patient);
		openmrsImmunization.setLocation(location);
		openmrsImmunization.setEncounter(encounter);
		openmrsImmunization.getGroupMembers().forEach(obs -> {
			obs.setPerson(patient);
			obs.setLocation(location);
			obs.setEncounter(encounter);
		});
		
		Map<String, Obs> members = helper.getObsMembersMap(openmrsImmunization);
		
		Coding coding = fhirImmunization.getVaccineCode().getCoding().stream()
		        .filter(code -> StringUtils.isEmpty(code.getSystem())).reduce((code1, code2) -> {
			        throw createImmunizationRequestValidationError(
			            "Multiple system-less coding found for the immunization's vaccine: " + code1.getCode() + " and "
			                    + code2.getCode() + ". No unique system concept could be identified as the coded answer.");
		        }).orElseThrow(() -> createImmunizationRequestValidationError(
		            "Could not find a valid coding could be identified for this immunization."));
		
		{
			Obs obs = members.get(CIEL_984);
			if (obs == null) {
				obs = helper.addNewObs(openmrsImmunization, CIEL_984);
				members.put(CIEL_984, obs);
				obs.setValueCoded(conceptService.getConceptByUuid(coding.getCode()));
			} else if (obs.getId() == null) {
				obs.setValueCoded(conceptService.getConceptByUuid(coding.getCode()));
			} else {
				Concept newValue = conceptService.getConceptByUuid(coding.getCode());
				Concept prevValue = obs.getValueCoded();
				
				if (!newValue.equals(prevValue)) {
					obs = helper.replaceObs(openmrsImmunization, obs);
					obs.setValueCoded(newValue);
				}
			}
		}
		
		{
			Obs obs = members.get(CIEL_1410);
			if (obs == null) {
				obs = helper.addNewObs(openmrsImmunization, CIEL_1410);
				members.put(CIEL_1410, obs);
				obs.setValueDatetime(fhirImmunization.getOccurrenceDateTimeType().getValue());
			} else if (obs.getId() == null) {
				obs.setValueDatetime(fhirImmunization.getOccurrenceDateTimeType().getValue());
			} else {
				Date newValue = fhirImmunization.getOccurrenceDateTimeType().getValue();
				Date prevValue = obs.getValueDatetime();
				
				if (!newValue.equals(prevValue)) {
					obs = helper.replaceObs(openmrsImmunization, obs);
					obs.setValueDatetime(newValue);
				}
			}
		}
		
		if (fhirImmunization.hasProtocolApplied()) {
			if (fhirImmunization.getProtocolApplied().size() != 1) {
				throw createImmunizationRequestValidationError(
				    "Either no protocol applied was found or multiple protocols applied were found. "
				            + "Only one protocol is currently supported for each immunization.");
			}
			
			ImmunizationProtocolAppliedComponent protocolApplied = fhirImmunization.getProtocolApplied().get(0);
			if (protocolApplied.hasDoseNumber()) {
				{
					Obs obs = members.get(CIEL_1418);
					if (obs == null) {
						obs = helper.addNewObs(openmrsImmunization, CIEL_1418);
						members.put(CIEL_1418, obs);
						obs.setValueNumeric(protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue());
					} else if (obs.getId() == null) {
						obs.setValueNumeric(protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue());
					} else {
						double newValue = protocolApplied.getDoseNumberPositiveIntType().getValue().doubleValue();
						Double updatedValue = obs.getValueNumeric();
						
						if (updatedValue != null && newValue != updatedValue) {
							obs = helper.replaceObs(openmrsImmunization, obs);
							obs.setValueNumeric(newValue);
						}
					}
				}
			}
		}
		
		if (fhirImmunization.hasManufacturer() && fhirImmunization.getManufacturer().hasDisplay()) {
			{
				Obs obs = members.get(CIEL_1419);
				if (obs == null) {
					obs = helper.addNewObs(openmrsImmunization, CIEL_1419);
					members.put(CIEL_1419, obs);
					obs.setValueText(fhirImmunization.getManufacturer().getDisplay());
				} else if (obs.getId() == null) {
					obs.setValueText(fhirImmunization.getManufacturer().getDisplay());
				} else {
					String newValue = fhirImmunization.getManufacturer().getDisplay();
					String prevValue = obs.getValueText();
					
					if (!newValue.equals(prevValue)) {
						obs = helper.replaceObs(openmrsImmunization, obs);
						obs.setValueText(newValue);
					}
				}
			}
		}
		
		if (fhirImmunization.hasLotNumber()) {
			{
				Obs obs = members.get(CIEL_1420);
				if (obs == null) {
					obs = helper.addNewObs(openmrsImmunization, CIEL_1420);
					members.put(CIEL_1420, obs);
					obs.setValueText(fhirImmunization.getLotNumber());
				} else if (obs.getId() == null) {
					obs.setValueText(fhirImmunization.getLotNumber());
				} else {
					String newValue = fhirImmunization.getLotNumber();
					String prevValue = obs.getValueText();
					
					if (!newValue.equals(prevValue)) {
						obs = helper.replaceObs(openmrsImmunization, obs);
						obs.setValueText(newValue);
					}
				}
			}
		}
		
		if (fhirImmunization.hasExpirationDate()) {
			{
				Obs obs = members.get(CIEL_165907);
				if (obs == null) {
					obs = helper.addNewObs(openmrsImmunization, CIEL_165907);
					members.put(CIEL_165907, obs);
					obs.setValueDate(fhirImmunization.getExpirationDate());
				} else if (obs.getId() == null) {
					obs.setValueDate(fhirImmunization.getExpirationDate());
				} else {
					Date newValue = fhirImmunization.getExpirationDate();
					Date prevValue = obs.getValueDate();
					
					if (!newValue.equals(prevValue)) {
						obs = helper.replaceObs(openmrsImmunization, obs);
						obs.setValueDate(newValue);
					}
				}
			}
		}
		
		return openmrsImmunization;
	}
	
	@Override
	public Immunization toFhirResource(@Nonnull Obs openmrsImmunization) {
		if (openmrsImmunization == null) {
			return null;
		}
		
		Immunization immunization = new Immunization();
		immunization.setId(openmrsImmunization.getUuid());
		immunization.setStatus(ImmunizationStatus.COMPLETED);
		immunization.setPatient(patientReferenceTranslator.toFhirResource(new Patient(openmrsImmunization.getPerson())));
		immunization.setEncounter(visitReferenceTranslator.toFhirResource(openmrsImmunization.getEncounter().getVisit()));
		immunization.setPerformer(Collections.singletonList(new ImmunizationPerformerComponent(
		        practitionerReferenceTranslator.toFhirResource(helper.getAdministeringProvider(openmrsImmunization)))));
		
		Map<String, Obs> members = helper.getObsMembersMap(openmrsImmunization);
		
		{
			Obs obs = members.get(CIEL_984);
			if (obs != null) {
				immunization.setVaccineCode(conceptTranslator.toFhirResource(obs.getValueCoded()));
			}
		}
		
		{
			Obs obs = members.get(CIEL_1410);
			if (obs != null) {
				immunization.setOccurrence(observationValueTranslator.toFhirResource(obs));
			}
		}
		
		{
			Obs obs = members.get(CIEL_1418);
			if (obs != null && obs.getValueNumeric() != null) {
				immunization.addProtocolApplied(new ImmunizationProtocolAppliedComponent(
				        new PositiveIntType((long) obs.getValueNumeric().doubleValue())));
			}
		}
		
		{
			Obs obs = members.get(CIEL_1419);
			if (obs != null) {
				immunization.setManufacturer(new Reference().setDisplay(obs.getValueText()));
			}
		}
		
		{
			Obs obs = members.get(CIEL_1420);
			if (obs != null) {
				immunization.setLotNumber(members.get(CIEL_1420).getValueText());
			}
		}
		
		{
			Obs obs = members.get(CIEL_165907);
			if (obs != null) {
				immunization.setExpirationDate(obs.getValueDate());
			}
		}
		return immunization;
	}
}
