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

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.model.FhirObservationCategoryMap;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FhirObservationCategoryMapDaoImpl extends BaseDao {
	
	public String getCategory(@Nonnull String conceptClassUuid) {
		OpenmrsFhirCriteriaContext<FhirObservationCategoryMap, String> criteriaContext = createCriteriaContext(
		    FhirObservationCategoryMap.class, String.class);
		Join<?, ?> conceptClassJoin = criteriaContext.addJoin("conceptClass", "ct");
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(conceptClassJoin.get("uuid"), conceptClassUuid));
		
		try {
			return criteriaContext.getEntityManager()
			        .createQuery(
			            criteriaContext.finalizeQuery().select(criteriaContext.getRoot().get("observationCategory")))
			        .getSingleResult();
		}
		catch (PersistenceException e) {
			log.error("Exception caught while trying to load category for concept class '{}'", conceptClassUuid, e);
		}
		
		return null;
	}
}
