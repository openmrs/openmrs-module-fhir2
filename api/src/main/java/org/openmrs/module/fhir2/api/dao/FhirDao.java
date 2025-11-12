/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Base interface for all FHIR DAO objects, which contains the standard functions expected by
 * classes extending the {@link org.openmrs.module.fhir2.api.impl.BaseFhirService}.
 */
public interface FhirDao<T extends OpenmrsObject & Auditable> extends FhirDaoAop, Serializable {
	
	/**
	 * Function to create a new instance of type {@link T} or update an existing instance if the
	 * matching instance already exists.
	 *
	 * @param object The object to either create or update
	 * @return The newly created or updated object
	 */
	T createOrUpdate(@Nonnull T object);
	
	/**
	 * This function performs a soft delete of the object of {@link T} matching teh supplied UUID.
	 *
	 * @param uuid The UUID of the object to delete
	 * @return The object deleted, if any. Otherwise, returns {@code null}.
	 */
	T delete(@Nonnull String uuid);
	
	/**
	 * Fetches a single object of type {@link T} by UUID
	 *
	 * @param uuid The UUID of the object to fetch
	 * @return The object of type {@link T} corresponding to this UUID or {@code null} if no such object
	 *         can be found
	 */
	T get(@Nonnull String uuid);
	
	/**
	 * Fetches one or more objects of type {@link T} by UUIDs <br/>
	 * Each UUID supplied to this function should be unique <br/>
	 * <strong>NB</strong> No attempt is made to ensure the order in which the results are returned
	 * matches the order in which they are supplied, so no order of returned results is guaranteed
	 *
	 * @param uuids A collection of unique UUIDs to search for
	 * @return A {@link List} of objects (if any) corresponding to the requested UUIDs
	 */
	List<T> get(@Nonnull Collection<String> uuids);
	
	/**
	 * Runs a search using the supplied {@link SearchParameterMap} and returns any objects that match
	 * the {@link SearchParameterMap}.
	 *
	 * @param theParams A {@link SearchParameterMap} defining any filters to apply
	 * @return A {@link List} of objects matching the supplied {@link SearchParameterMap}, if any.
	 *         Otherwise, returns an empty {@link List}.
	 */
	List<T> getSearchResults(@Nonnull SearchParameterMap theParams);
	
	/**
	 * Gets the count of objects that will match a search run with the supplied
	 * {@link SearchParameterMap}
	 *
	 * @param theParams A {@link SearchParameterMap} defining any filters to apply
	 * @return The number of objects matching the supplied {@link SearchParameterMap}
	 */
	int getSearchResultsCount(@Nonnull SearchParameterMap theParams);
	
}
