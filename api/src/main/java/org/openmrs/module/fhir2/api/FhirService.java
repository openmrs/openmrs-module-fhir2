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
 * Common contract for the {@code Service} layer of every FHIR resource type the module exposes.
 * Concrete services translate between the FHIR resource type {@code T} (an R4 model class such as
 * {@code Patient} or {@code Encounter}) and the underlying OpenMRS domain object via the
 * {@code DAO} and {@code Translator} layers, and provide the standard FHIR CRUD + search lifecycle
 * for that resource.
 * <p>
 * Resource-specific services typically extend this interface to add resource-specific search
 * operations (e.g. {@code FhirEncounterService.searchForEncounters}), and the standard
 * implementation extends {@link org.openmrs.module.fhir2.api.impl.BaseFhirService} which provides a
 * generic implementation of all the methods declared here.
 *
 * @param <T> the FHIR resource type this service handles (R4 model class, e.g. {@code Patient})
 */
public interface FhirService<T extends IAnyResource> {
	
	/**
	 * Reads a resource by its UUID. Throws {@link ResourceNotFoundException} when no matching resource
	 * exists and {@link ResourceGoneException} when the underlying OpenMRS object has been voided or
	 * retired (HTTP 410 Gone semantics).
	 *
	 * @param uuid the unique identifier of the resource to read, never {@code null}
	 * @return the FHIR resource translated from its OpenMRS backing
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no row with the given UUID exists in the backing store
	 * @throws ResourceGoneException if the underlying OpenMRS object exists but is voided or retired
	 */
	T get(@Nonnull String uuid);
	
	/**
	 * Reads multiple resources by their UUIDs in a single call. UUIDs that don't correspond to existing
	 * resources are silently dropped — the returned list has at most one entry per input UUID but may
	 * be shorter (or empty) when some inputs aren't found. Order of the returned list is not guaranteed
	 * to match the input order.
	 *
	 * @param uuids the UUIDs to read, never {@code null}; may be empty
	 * @return the FHIR resources for the UUIDs that resolved, in any order
	 */
	List<T> get(@Nonnull Collection<String> uuids);
	
	/**
	 * Determines whether a resource with the given UUID exists for this service. Differs from
	 * {@link #get(String)} in that it does not need to translate the resource and should not throw if
	 * the resource cannot be found.
	 * <p/>
	 * This is generally intended to be used for cases where a quick "does this exist" check makes
	 * sense.
	 *
	 * @param uuid the UUID to probe, never {@code null}
	 * @return {@code true} if a row for the UUID exists in the backing store
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
	 * Persists a new resource. The implementation translates the FHIR resource to its OpenMRS domain
	 * object, validates it, assigns a UUID if the caller didn't supply one, and saves through the
	 * corresponding DAO. The returned resource is the translation of the saved OpenMRS object — it
	 * reflects any server-assigned identifiers, defaults, or other server-side state.
	 *
	 * @param newResource the resource to create, never {@code null}
	 * @return the persisted resource as it now appears, with any server-assigned fields populated
	 * @throws InvalidRequestException if {@code newResource} is {@code null} or otherwise malformed for
	 *             this resource type
	 * @throws UnprocessableEntityException if the translated OpenMRS object fails validation
	 */
	T create(@Nonnull T newResource);
	
	/**
	 * Updates the resource at {@code uuid} with the contents of {@code updatedResource}. The resource's
	 * {@code id} must equal {@code uuid}; mismatches are rejected with {@link InvalidRequestException}.
	 * Equivalent to {@link #update(String, IAnyResource, RequestDetails, boolean) update(uuid,
	 * updatedResource, null, false)}.
	 *
	 * @param uuid the unique identifier of the resource to update, never {@code null}
	 * @param updatedResource the new resource state, never {@code null}; its {@code id} must equal
	 *            {@code uuid}
	 * @return the updated resource as it now appears in the database
	 * @throws InvalidRequestException if any required argument is {@code null} or the resource's id
	 *             doesn't match {@code uuid}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 */
	T update(@Nonnull String uuid, @Nonnull T updatedResource);
	
	/**
	 * Updates the specified resource if it exists, otherwise creates a new one when the implementation
	 * supports the operation and {@code createIfNotExists} is {@code true}. This is the entry point
	 * used by the HAPI {@code @Update} resource-provider methods, which need to communicate back to the
	 * framework whether the request resulted in a create or an update — done via
	 * {@code requestDetails.getUserData().put(USER_DATA_KEY_OUTCOME_CREATED, true)}.
	 * <p>
	 * Whether {@code createIfNotExists} actually creates depends on the implementation. The standard
	 * {@link org.openmrs.module.fhir2.api.impl.BaseFhirService} implementation only honours it when the
	 * underlying OpenMRS type is {@link org.openmrs.OpenmrsMetadata}; for other types this method
	 * behaves like {@link #update(String, IAnyResource)} and returns 404 for unknown UUIDs even when
	 * {@code createIfNotExists} is {@code true}.
	 *
	 * @param uuid the unique identifier of the resource
	 * @param updatedResource the resource to update
	 * @param requestDetails the HAPI request details, used to communicate the create-vs-update outcome
	 *            back to the resource provider
	 * @param createIfNotExists if {@code true}, create the resource when it does not yet exist and the
	 *            implementation supports creation under this UUID
	 * @return the updated or created resource
	 * @throws InvalidRequestException if any required argument is {@code null} or the resource's id
	 *             doesn't match {@code uuid}
	 * @throws ResourceNotFoundException if the resource doesn't exist and {@code createIfNotExists} is
	 *             {@code false} (or unsupported)
	 */
	T update(@Nonnull String uuid, @Nonnull T updatedResource, RequestDetails requestDetails, boolean createIfNotExists);
	
	/**
	 * Applies a JSON or XML patch document to the resource at {@code uuid} and saves the result. The
	 * implementation reads the existing resource, applies the patch in-memory, then persists the
	 * patched form through the same translator/DAO pipeline {@code update} uses.
	 * <p>
	 * For JSON-format patches, the {@code Content-Type} header on {@code requestDetails} disambiguates
	 * between RFC 6902 {@code application/json-patch+json} and RFC 7396
	 * {@code application/merge-patch+json}.
	 *
	 * @param uuid the unique identifier of the resource to patch, never {@code null}
	 * @param patchType the patch syntax in use (JSON or XML)
	 * @param body the patch document, never {@code null}
	 * @param requestDetails the HAPI request details (used to read the {@code Content-Type} header for
	 *            JSON patches)
	 * @return the patched resource as it now appears in the database
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 */
	T patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body, RequestDetails requestDetails);
	
	/**
	 * Deletes the resource at {@code uuid}. For OpenMRS-backed resources this typically performs a
	 * soft-delete (void or retire), preserving the row in the database with a flag set — subsequent
	 * reads of the same UUID will throw {@link ResourceGoneException}.
	 *
	 * @param uuid the unique identifier of the resource to delete, never {@code null}
	 * @throws InvalidRequestException if {@code uuid} is {@code null}
	 * @throws ResourceNotFoundException if no resource with the given UUID exists
	 */
	void delete(@Nonnull String uuid);
}
