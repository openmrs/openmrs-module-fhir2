/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.internals;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This object is used internally during sorting to track information about the current sort
 * operation, including the parameter, the order (whether ascending or descending) and the
 * {@link BaseFhirCriteriaHolder<T>} for the query being sorted.
 */
@Data
@Builder
@EqualsAndHashCode
public final class SortState<T> {
	
	private final BaseFhirCriteriaHolder<T> context;
	
	private final SortOrderEnum sortOrder;
	
	private final String parameter;
}
