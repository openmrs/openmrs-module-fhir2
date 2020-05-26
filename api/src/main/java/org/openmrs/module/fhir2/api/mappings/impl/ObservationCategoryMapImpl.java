/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.mappings.impl;

import javax.validation.constraints.NotNull;

import org.openmrs.module.fhir2.api.mappings.ObservationCategoryMap;
import org.springframework.stereotype.Component;

@Component
public class ObservationCategoryMapImpl extends BaseMapping implements ObservationCategoryMap {
	
	public ObservationCategoryMapImpl() {
		super("observationCategoryMap.properties");
	}
	
	@Override
	public String getCategory(@NotNull String conceptClassUuid) {
		return getFhir(conceptClassUuid).orElse(null);
	}
	
	@Override
	public String getConceptClassUuid(@NotNull String category) {
		return getOpenmrs(category).orElse(null);
	}
}
