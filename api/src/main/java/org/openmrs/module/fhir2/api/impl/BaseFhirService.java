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
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.common.reflect.TypeToken;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.ValidationException;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;
import org.openmrs.validator.ValidateUtil;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFhirService<T extends IAnyResource, U extends OpenmrsObject & Auditable> implements FhirService<T> {
	
	protected final Class<? super T> resourceClass;
	
	protected BaseFhirService() {
		// @formatter:off
		TypeToken<T> resourceTypeToken = new TypeToken<T>(getClass()) {};
		// @formatter:on
		
		this.resourceClass = resourceTypeToken.getRawType();
	}
	
	@Override
	public T get(String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		U openmrsObj = getDao().get(uuid);
		
		if (openmrsObj == null) {
			throw resourceNotFound(uuid);
		} else if (isVoided(openmrsObj) || isRetired(openmrsObj)) {
			throw new ResourceGoneException(
			        "Resource of type " + resourceClass.getSimpleName() + " with ID " + uuid + " is gone/deleted");
		}
		
		return getTranslator().toFhirResource(openmrsObj);
	}
	
	@Override
	public T create(T newResource) {
		if (newResource == null) {
			throw new InvalidRequestException("A resource of type " + resourceClass.getSimpleName() + " must be supplied");
		}
		
		U openmrsObj = getTranslator().toOpenmrsType(newResource);
		
		validateObject(openmrsObj);
		
		return getTranslator().toFhirResource(getDao().createOrUpdate(openmrsObj));
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
		
		if (!updatedResource.getIdElement().getIdPart().equals(uuid)) {
			throw new InvalidRequestException(
			        String.format("%s id does not match resource id.", updatedResource.getClass().getSimpleName()));
		}
		
		U existingObject = getDao().get(uuid);
		
		if (existingObject == null) {
			throw resourceNotFound(uuid);
		}
		
		OpenmrsFhirTranslator<U, T> translator = getTranslator();
		
		U updatedObject;
		if (translator instanceof UpdatableOpenmrsTranslator) {
			UpdatableOpenmrsTranslator<U, T> updatableOpenmrsTranslator = (UpdatableOpenmrsTranslator<U, T>) translator;
			updatedObject = updatableOpenmrsTranslator.toOpenmrsType(existingObject, updatedResource);
		} else {
			updatedObject = translator.toOpenmrsType(updatedResource);
		}
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(getDao().createOrUpdate(updatedObject));
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
	
	/**
	 * Overridden by subclasses to return the appropriate {@link FhirDao} object for the OpenMRS object
	 * serviced by this class
	 *
	 * @return a {@link FhirDao}
	 */
	protected abstract FhirDao<U> getDao();
	
	/**
	 * Overridden by subclasses to return the appropriate {@link OpenmrsFhirTranslator} to convert
	 * between FHIR resources and OpenMRS objects for this class
	 *
	 * @return a {@link OpenmrsFhirTranslator}
	 */
	protected abstract OpenmrsFhirTranslator<U, T> getTranslator();
	
	/**
	 * Determines whether the object is voided
	 *
	 * @param object an object implementing the Voidable interface
	 * @return true if the object is voided, false otherwise
	 */
	protected boolean isVoided(U object) {
		return object instanceof Voidable && ((Voidable) object).getVoided();
	}
	
	/**
	 * Determines whether the object is retired
	 *
	 * @param object an object implementing the Retireable interface
	 * @return true if the object is retired, false otherwise
	 */
	protected boolean isRetired(U object) {
		return object instanceof Retireable && ((Retireable) object).getRetired();
	}
	
	/**
	 * Run before creates or updates to validate that the object meets OpenMRS's business rules and
	 * attempts to provide an appropriate error when it cannot
	 *
	 * @param object the object to validate
	 */
	protected void validateObject(U object) {
		// TODO Improve these messages
		try {
			ValidateUtil.validate(object);
		}
		catch (ValidationException e) {
			throw new UnprocessableEntityException(e.getMessage(), e);
		}
	}
	
	private ResourceNotFoundException resourceNotFound(String uuid) {
		return new ResourceNotFoundException(
		        "Resource of type " + resourceClass.getSimpleName() + " with ID " + uuid + " is not known");
	}
}
