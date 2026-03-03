/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import javax.annotation.Nonnull;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.Join;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.model.FhirEncounterClassMap;
import org.springframework.stereotype.Component;

/**
 * This class handles mapping between OpenMRS {@link org.openmrs.Location} objects and the type of
 * FHIR Encounter Class that would happen at that location.
 *
 * @see FhirEncounterClassMap
 */
@Component
@Slf4j
public class FhirEncounterClassMapDaoImpl extends BaseDao {
	
	public String getFhirClass(@Nonnull String locationUuid) {
		OpenmrsFhirCriteriaContext<FhirEncounterClassMap, String> criteriaContext = createCriteriaContext(
		    FhirEncounterClassMap.class, String.class);
		
		Join<?, ?> locationJoin = criteriaContext.addJoin("location", "l");
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(locationJoin.get("uuid"), locationUuid));
		
		try {
			List<String> resultsList = criteriaContext.getEntityManager()
			        .createQuery(criteriaContext.finalizeQuery().select(criteriaContext.getRoot().get("encounterClass")))
			        .getResultList();
			
			if (resultsList != null && resultsList.size() > 1) {
				log.error("Encounter type for location '{}' is not unique", locationUuid);
				return null;
			}
			
			return resultsList != null && !resultsList.isEmpty() ? resultsList.get(0) : null;
		}
		catch (PersistenceException e) {
			log.error("Exception caught while trying to load encounter type for location '{}'", locationUuid);
		}
		
		return null;
	}
}
