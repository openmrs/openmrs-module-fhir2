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

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.reflect.TypeToken;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFhirService<T extends IAnyResource, U extends OpenmrsObject & Auditable> implements FhirService<T> {
	
	protected final Class<? extends IResource> resourceClass;
	
	protected final TypeToken<T> resourceTypeToken;
	
	protected BaseFhirService() {
		resourceTypeToken = new TypeToken<T>(getClass()) {
			
		};
		
		// this doesn't seem like it should work but does?
		@SuppressWarnings("unchecked")
		Class<? extends IResource> resourceClass = (Class<? extends IResource>) resourceTypeToken.getRawType();
		
		this.resourceClass = resourceClass;
	}
	
	@Override
	public T get(String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		U openmrsObj = getDao().get(uuid);
		
		if (openmrsObj == null) {
			throw resourceNotFound(uuid);
		}
		
		return getTranslator().toFhirResource(openmrsObj);
	}
	
	@Override
	public T create(T newResource) {
		if (newResource == null) {
			throw new InvalidRequestException("A resource of type " + resourceClass.getSimpleName() + " must be supplied");
		}
		
		return getTranslator().toFhirResource(getDao().createOrUpdate(getTranslator().toOpenmrsType(newResource)));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T update(String uuid, T updatedResource) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (updatedResource.getId() == null) {
			throw new InvalidRequestException(
			        String.format("%s resource is missing id.", updatedResource.getClass().getSimpleName()));
		}
		
		if (!updatedResource.getId().equals(uuid)) {
			throw new InvalidRequestException(
			        String.format("%s id does not match resource id.", updatedResource.getClass().getSimpleName()));
		}
		
		U existingObject = getDao().get(uuid);
		
		if (existingObject == null) {
			throw resourceNotFound(uuid);
		}
		
		OpenmrsFhirTranslator<U, T> translator = getTranslator();
		
		if (translator instanceof UpdatableOpenmrsTranslator) {
			UpdatableOpenmrsTranslator<U, T> updatableOpenmrsTranslator = (UpdatableOpenmrsTranslator<U, T>) translator;
			return translator.toFhirResource(
			    getDao().createOrUpdate(updatableOpenmrsTranslator.toOpenmrsType(existingObject, updatedResource)));
		} else {
			return translator.toFhirResource(getDao().createOrUpdate(translator.toOpenmrsType(updatedResource)));
		}
	}
	
	@Override
	public T delete(String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		U openmrsObj = getDao().delete(uuid);
		
		if (openmrsObj == null) {
			throw resourceNotFound(uuid);
		}
		
		return getTranslator().toFhirResource(openmrsObj);
	}
	
	protected abstract FhirDao<U> getDao();
	
	protected abstract OpenmrsFhirTranslator<U, T> getTranslator();
	
	private ResourceNotFoundException resourceNotFound(String uuid) {
		return new ResourceNotFoundException(resourceClass, new IdDt(uuid));
	}
}
