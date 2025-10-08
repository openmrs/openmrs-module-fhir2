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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.ValidationException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.api.util.JsonPatchUtils;
import org.openmrs.module.fhir2.api.util.XmlPatchUtils;
import org.openmrs.validator.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFhirService<T extends IAnyResource, U extends OpenmrsObject & Auditable> implements FhirService<T> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected final Class<? super T> resourceClass;
	
	private boolean handlesOpenmrsMetadata;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @__({ @Autowired, @Qualifier("fhirR4") }))
	private FhirContext fhirContext;
	
	protected BaseFhirService() {
		// @formatter:off
		TypeToken<T> resourceTypeToken = new TypeToken<T>(getClass()) {};
		// @formatter:on
		
		this.resourceClass = resourceTypeToken.getRawType();
		TypeToken<U> openmrsTypeToken = new TypeToken<U>(getClass()) {};
		handlesOpenmrsMetadata = OpenmrsMetadata.class.isAssignableFrom(openmrsTypeToken.getRawType());
	}
	
	@Override
	public T get(@Nonnull String uuid) {
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
	public List<T> get(@Nonnull Collection<String> uuids) {
		OpenmrsFhirTranslator<U, T> translator = getTranslator();
		return getDao().get(uuids).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public T create(@Nonnull T newResource) {
		if (newResource == null) {
			throw new InvalidRequestException("A resource of type " + resourceClass.getSimpleName() + " must be supplied");
		}
		
		U openmrsObj = getTranslator().toOpenmrsType(newResource);
		
		validateObject(openmrsObj);
		
		if (openmrsObj.getUuid() == null) {
			openmrsObj.setUuid(FhirUtils.newUuid());
		}
		
		return getTranslator().toFhirResource(getDao().createOrUpdate(openmrsObj));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T update(@Nonnull String uuid, @Nonnull T updatedResource) {
		return update(uuid, updatedResource, null, false);
	}
	
	@Override
	public T update(@Nonnull String uuid, @Nonnull T updatedResource, RequestDetails requestDetails,
	        boolean createIfNotExists) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (updatedResource == null) {
			throw new InvalidRequestException("Resource cannot be null.");
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
			if (!handlesOpenmrsMetadata || !createIfNotExists) {
				throw resourceNotFound(uuid);
			}
			
			//We need to communicate to the resource provider whether this operation resulted in a creation or an
			//update but the return type provides no way, so we use the user data map on the request details object
			//for this purpose as the recommended way, please refer to the javadocs of RequestDetails.getUserData().
			requestDetails.getUserData().put(FhirConstants.USER_DATA_KEY_OUTCOME_CREATED, true);
		}
		
		return applyUpdate(existingObject, updatedResource);
	}
	
	@Override
	public T patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		if (uuid == null) {
			throw new InvalidRequestException("id cannot be null");
		}
		
		U existingObject = getDao().get(uuid);
		
		if (existingObject == null) {
			throw resourceNotFound(uuid);
		}
		
		OpenmrsFhirTranslator<U, T> translator = getTranslator();
		
		T existingFhirObject = translator.toFhirResource(existingObject);
		T updatedFhirObject = null;
		
		switch (patchType) {
			case JSON_PATCH:
				if (isJsonMergePatch(requestDetails)) {
					updatedFhirObject = JsonPatchUtils.applyJsonMergePatch(fhirContext, existingFhirObject, body);
				} else {
					updatedFhirObject = JsonPatchUtils.applyJsonPatch(fhirContext, existingFhirObject, body);
				}
				break;
			case XML_PATCH:
				updatedFhirObject = XmlPatchUtils.applyXmlPatch(fhirContext, existingFhirObject, body);
				break;
		}
		
		return applyUpdate(existingObject, updatedFhirObject);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		U openmrsObj = getDao().delete(uuid);
		
		if (openmrsObj == null) {
			throw resourceNotFound(uuid);
		}
	}
	
	protected T applyUpdate(U existingObject, T updatedResource) {
		OpenmrsFhirTranslator<U, T> translator = getTranslator();
		
		U updatedObject;
		if (translator instanceof UpdatableOpenmrsTranslator && existingObject != null) {
			UpdatableOpenmrsTranslator<U, T> updatableOpenmrsTranslator = (UpdatableOpenmrsTranslator<U, T>) translator;
			updatedObject = updatableOpenmrsTranslator.toOpenmrsType(existingObject, updatedResource);
		} else {
			updatedObject = translator.toOpenmrsType(updatedResource);
		}
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(getDao().createOrUpdate(updatedObject));
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
			log.error("Error occurred while validating {} object", resourceClass.getSimpleName(), e);
			throw new UnprocessableEntityException(e.getMessage(), e);
		}
	}
	
	/**
	 * checks the Content-Type header of the request to determine if it corresponds to a merge json
	 * patch
	 */
	protected Boolean isJsonMergePatch(RequestDetails requestDetails) {
		String contentType = requestDetails.getHeader(Constants.HEADER_CONTENT_TYPE);
		return contentType != null && contentType.equalsIgnoreCase("application/merge-patch+json");
	}
	
	protected ResourceNotFoundException resourceNotFound(String uuid) {
		return new ResourceNotFoundException(
		        "Resource of type " + resourceClass.getSimpleName() + " with ID " + uuid + " is not known");
	}
	
}
