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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createLocationReferenceByUuid;

import javax.annotation.Nonnull;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationBasedOnReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationCategoryTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationEffectiveDatetimeTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceRangeTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.util.ObsGroupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservationTranslatorImpl implements ObservationTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationStatusTranslator observationStatusTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationValueTranslator observationValueTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationCategoryTranslator categoryTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationInterpretationTranslator interpretationTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationReferenceRangeTranslator referenceRangeTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationBasedOnReferenceTranslator basedOnReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObservationEffectiveDatetimeTranslator datetimeTranslator;
	
	@Autowired
	private ObsGroupHelper obsGroupHelper;
	
	@Override
	public Observation toFhirResource(@Nonnull Obs observation) {
		notNull(observation, "The Obs object should not be null");
		
		Observation obs = new Observation();
		obs.setId(observation.getUuid());
		obs.setStatus(observationStatusTranslator.toFhirResource(observation));
		
		obs.setEncounter(encounterReferenceTranslator.toFhirResource(observation.getEncounter()));
		
		Person obsPerson = observation.getPerson();
		if (obsPerson != null) {
			if (obsPerson instanceof HibernateProxy) {
				obsPerson = HibernateUtil.getRealObjectFromProxy(obsPerson);
			}
			
			if (obsPerson instanceof Patient) {
				obs.setSubject(patientReferenceTranslator.toFhirResource((Patient) obsPerson));
			}
		}
		
		obs.setCode(conceptTranslator.toFhirResource(observation.getConcept()));
		obs.addCategory(categoryTranslator.toFhirResource(observation.getConcept()));
		
		if (observation.isObsGrouping()) {
			for (Obs groupObs : observation.getGroupMembers()) {
				if (!groupObs.getVoided()) {
					obs.addHasMember(observationReferenceTranslator.toFhirResource(groupObs));
				}
			}
		}
		if (!obs.hasHasMember()) {
			obs.setValue(observationValueTranslator.toFhirResource(observation));
		}
		
		obs.addInterpretation(interpretationTranslator.toFhirResource(observation));
		
		if (observation.getValueNumeric() != null) {
			Concept concept = observation.getConcept();
			if (concept instanceof ConceptNumeric) {
				obs.setReferenceRange(referenceRangeTranslator.toFhirResource(observation));
			}
		}
		
		if (observation.getValueText() != null && StringUtils.equals(observation.getComment(), "org.openmrs.Location")) {
			obs.addExtension(FhirConstants.OPENMRS_FHIR_EXT_OBS_LOCATION_VALUE,
			    createLocationReferenceByUuid(observation.getValueText()));
		}
		
		obs.setIssued(observation.getDateCreated());
		obs.setEffective(datetimeTranslator.toFhirResource(observation));
		obs.addBasedOn(basedOnReferenceTranslator.toFhirResource(observation.getOrder()));
		
		obs.getMeta().setLastUpdated(getLastUpdated(observation));
		obs.getMeta().setVersionId(getVersionId(observation));
		
		return obs;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Observation fhirObservation) {
		notNull(fhirObservation, "The Observation object should not be null");
		return toOpenmrsType(new Obs(), fhirObservation);
	}
	
	@Override
	public Obs toOpenmrsType(Obs existingObs, Observation observation, Supplier<Obs> groupedObsFactory) {
		notNull(existingObs, "The existing Obs object should not be null");
		notNull(observation, "The Observation object should not be null");
		
		observationStatusTranslator.toOpenmrsType(existingObs, observation.getStatus());
		
		existingObs.setEncounter(encounterReferenceTranslator.toOpenmrsType(observation.getEncounter()));
		existingObs.setPerson(patientReferenceTranslator.toOpenmrsType(observation.getSubject()));
		
		existingObs.setConcept(conceptTranslator.toOpenmrsType(observation.getCode()));
		
		for (Reference reference : observation.getHasMember()) {
			Obs childObservation = observationReferenceTranslator.toOpenmrsType(reference);
			if (childObservation.getObsGroup() == null) {
				obsGroupHelper.voidAndAddToGroupNewObservation(existingObs, childObservation);
			} else {
				existingObs.addGroupMember(childObservation);
			}
		}
		
		if (observation.hasValue()) {
			observationValueTranslator.toOpenmrsType(existingObs, observation.getValue());
		}
		
		if (!observation.getInterpretation().isEmpty()) {
			interpretationTranslator.toOpenmrsType(existingObs, observation.getInterpretation().get(0));
		}
		
		datetimeTranslator.toOpenmrsType(existingObs, observation.getEffectiveDateTimeType());
		
		if (observation.hasBasedOn()) {
			existingObs.setOrder(basedOnReferenceTranslator.toOpenmrsType(observation.getBasedOn().get(0)));
		}
		
		return existingObs;
	}
}
