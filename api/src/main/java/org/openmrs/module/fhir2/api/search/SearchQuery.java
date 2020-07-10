/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;

/**
 * Generic search Interface
 *
 * @param <O> openMrs generic DAO Class Implementation
 * @param <T> FHIR generic translator Class
 */
public interface SearchQuery<T extends OpenmrsObject & Auditable, U extends IBaseResource, O extends FhirDao<T>, V extends ToFhirTranslator<T, U>, W extends SearchQueryInclude<U>> {
	
	/**
	 * Gets query results
	 *
	 * @param theParams search params.
	 * @param dao generic dao
	 * @param translator generic translator
	 * @return IBundleProvider
	 */
	IBundleProvider getQueryResults(@NotNull SearchParameterMap theParams, O dao, V translator, W searchQueryInclude);
}
