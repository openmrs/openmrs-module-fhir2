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

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Media;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.fhir2.api.translators.MediaTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MediaTranslatorImpl extends BaseReferenceHandlingTranslator implements MediaTranslator {

	@Override
	public Media toFhirResource(@Nonnull Obs data) {
		notNull(data, "The Openmrs Complex obs object should not be null");
		Person obsPerson = data.getPerson();
		Encounter encounter = data.getEncounter();
		Concept concept = data.getValueCoded();
		encounter.setLocation(data.getLocation());
		encounter.setDateCreated(data.getDateCreated());
		
		if (obsPerson != null) {
			if (obsPerson instanceof HibernateProxy) {
				obsPerson = HibernateUtil.getRealObjectFromProxy(obsPerson);
			}
			if (obsPerson instanceof Patient) {
				data.setPerson(obsPerson);
			}
		}
		data.setEncounter(encounter);
		data.setValueCoded(data.getValueCoded());
		
		Media media = new Media();
		media.setType(new CodeableConcept());
		media.setEncounter(null);
		media.setSubject(new Reference());
		media.setCreated(new DateType());
		
		return media;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Media resource) {
		notNull(resource, "The media resource should not be null");
		return toOpenmrsType(new Obs(), resource);
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource) {
		notNull(existingObject, "The existing object should not be null");
		notNull(resource, "The observation object should not be null");
		return existingObject;
	}
}
