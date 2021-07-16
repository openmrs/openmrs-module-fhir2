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
 * Base interface for all FHIR DAO objects
 */
public interface FhirDao<T extends OpenmrsObject & Auditable> extends Serializable {
	
	T get(@Nonnull String uuid);
	
	List<T> get(@Nonnull Collection<String> uuids);
	
	T createOrUpdate(@Nonnull T newEntry);
	
	T delete(@Nonnull String uuid);
	
	List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams);
	
	List<T> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> resourceUuids);
}
