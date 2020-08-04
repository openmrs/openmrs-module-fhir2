/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.mappings;

import javax.validation.constraints.NotNull;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObservationCategoryMap extends BaseMapping {
	
	public ObservationCategoryMap() {
		super("observationCategoryMap.properties");
	}
	
	public String getCategory(@NotNull String conceptClassUuid) {
		Collection<String> categories = getKey(conceptClassUuid);
		if (categories.isEmpty()) {
			return null;
		}
		if (categories.size() > 1) {
			log.warn("Multiple categories found for concept " + conceptClassUuid);
		}
		return categories.iterator().next();
	}
	
	public String getConceptClassUuid(@NotNull String category) {
		return getValue(category).orElse(null);
	}
}
