/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public abstract class BaseFhirService<T extends IAnyResource, U extends OpenmrsObject & Auditable> implements FhirService<T> {
	
	@Override
	public T get(String uuid) {
		OpenmrsFhirUpdatableTranslator<U, T> translator = getTranslator();
		return translator.toFhirResource(getDao().get(uuid));
	}
	
	@Override
	public T create(T newResource) {
		OpenmrsFhirUpdatableTranslator<U, T> translator = getTranslator();
		return translator.toFhirResource(getDao().createOrUpdate(translator.toOpenmrsType(newResource)));
	}
	
	@Override
	public T update(String uuid, T updatedResource) {
		
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (!updatedResource.getId().equals(uuid)) {
			throw new InvalidRequestException(
			        String.format("%s id does not match resource id.", updatedResource.getClass().getSimpleName()));
		}
		
		U existingObject = getDao().get(uuid);
		
		if (existingObject == null) {
			throw new MethodNotAllowedException("No object found to update");
		}
		
		OpenmrsFhirUpdatableTranslator<U, T> updatableTranslator = getTranslator();
		
		return updatableTranslator
		        .toFhirResource(getDao().createOrUpdate(updatableTranslator.toOpenmrsType(existingObject, updatedResource)));
	}
	
	@Override
	public T delete(String uuid) {
		OpenmrsFhirUpdatableTranslator<U, T> translator = getTranslator();
		return translator.toFhirResource(getDao().delete(uuid));
	}
	
	protected abstract FhirDao<U> getDao();
	
	protected abstract OpenmrsFhirUpdatableTranslator<U, T> getTranslator();
}
