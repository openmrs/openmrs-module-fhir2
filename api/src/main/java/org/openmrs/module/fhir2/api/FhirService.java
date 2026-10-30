/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IAnyResource;

/**
 * Common CRUD contract for the service of each FHIR resource type. Resource-specific services
 * extend it with their search operations (e.g. {@code FhirEncounterService.searchForEncounters}).
 * <p>
 * Implementations backed by a single OpenMRS domain object typically extend
 * {@link org.openmrs.module.fhir2.api.impl.BaseFhirService}; those that dispatch across the
 * handlers registered for a resource type extend
 * {@link org.openmrs.module.fhir2.api.impl.BaseCompositeFhirService}.
 *
 * @param <T> the FHIR R4 resource type this service handles
 */
public interface FhirService<T extends IAnyResource> {
	
	/**
	 * Reads a resource by its UUID.
	 *
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 * @throws ResourceGoneException if the underlying OpenMRS object exists but is voided or retired
	 */
	T get(@Nonnull String uuid);
	
	/**
	 * Reads multiple resources by their UUIDs. UUIDs that match no resource are silently dropped, and
	 * the result is not guaranteed to follow the input order.
	 */
	List<T> get(@Nonnull Collection<String> uuids);
	
	/**
	 * Returns whether this service holds a resource with the given UUID, without translating it or
	 * throwing when it is absent. A voided or retired object counts as existing.
	 * <p>
	 * The composite services use this to find which backing owns a UUID before a write, so
	 * implementations should not require the read privilege (see {@code FhirDao#exists(String)}). The
	 * default goes through {@link #get(String)} and so does require it.
	 */
	default boolean exists(@Nonnull String uuid) {
		try {
			get(uuid);
			return true;
		}
		catch (ResourceNotFoundException e) {
			return false;
		}
		catch (ResourceGoneException e) {
			return true;
		}
	}
	
	/**
	 * Persists a new resource, assigning a UUID if none is supplied.
	 *
	 * @return the persisted resource, including any server-assigned values
	 * @throws InvalidRequestException if {@code newResource} is {@code null} or otherwise malformed for
	 *             this resource type
	 * @throws UnprocessableEntityException if the translated OpenMRS object fails validation
	 */
	T create(@Nonnull T newResource);
	
	/**
	 * Updates the resource at {@code uuid}. Equivalent to
	 * {@link #update(String, IAnyResource, RequestDetails, boolean) update(uuid, updatedResource, null,
	 * false)}.
	 *
	 * @param updatedResource the new resource state; its {@code id} must equal {@code uuid}
	 * @return the updated resource as persisted
	 * @throws InvalidRequestException if any required argument is {@code null} or the resource's id
	 *             doesn't match {@code uuid}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists; implementations may
	 *             check existence before validating the id, in which case an unknown UUID is reported
	 *             as not found even when the body's id is also wrong
	 */
	T update(@Nonnull String uuid, @Nonnull T updatedResource);
	
	/**
	 * Updates the specified resource if it exists, otherwise creates it when {@code createIfNotExists}
	 * is {@code true} and the implementation supports it. An implementation that creates must record
	 * this by putting {@link org.openmrs.module.fhir2.FhirConstants#USER_DATA_KEY_OUTCOME_CREATED} into
	 * {@code requestDetails.getUserData()}, so the resource provider can report a create.
	 * <p>
	 * {@link org.openmrs.module.fhir2.api.impl.BaseFhirService} only creates when the underlying
	 * OpenMRS type is {@link org.openmrs.OpenmrsMetadata}; for other types an unknown UUID is not found
	 * even when {@code createIfNotExists} is {@code true}.
	 *
	 * @param requestDetails the HAPI request details, which carry the create-vs-update outcome back to
	 *            the resource provider
	 * @return the updated or created resource
	 * @throws InvalidRequestException if any required argument is {@code null} or the resource's id
	 *             doesn't match {@code uuid}
	 * @throws ResourceNotFoundException if the resource doesn't exist and {@code createIfNotExists} is
	 *             {@code false} (or unsupported)
	 */
	T update(@Nonnull String uuid, @Nonnull T updatedResource, RequestDetails requestDetails, boolean createIfNotExists);
	
	/**
	 * Applies a JSON or XML patch document to the resource at {@code uuid} and saves the result. For
	 * JSON patches, the {@code Content-Type} header on {@code requestDetails} selects between RFC 6902
	 * {@code application/json-patch+json} and RFC 7396 {@code application/merge-patch+json}.
	 *
	 * @return the patched resource as persisted
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 */
	T patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body, RequestDetails requestDetails);
	
	/**
	 * Deletes the resource at {@code uuid}. OpenMRS-backed resources are typically voided or retired
	 * rather than removed, so later reads of the UUID throw {@link ResourceGoneException}.
	 *
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 */
	void delete(@Nonnull String uuid);
}
