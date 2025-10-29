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

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.springframework.stereotype.Component;

@Component
public class FhirPractitionerDaoImpl extends BasePractitionerDao<Provider> implements FhirPractitionerDao {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected <U> void handleIdentifier(OpenmrsFhirCriteriaContext<Provider, U> criteriaContext,
	        TokenAndListParam identifier) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), identifier,
		    param -> Optional.of(
		        criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("identifier"), param.getValue())))
		                .ifPresent(criteriaContext::addPredicate);
	}
	
	@Override
	public List<ProviderAttribute> getActiveAttributesByPractitionerAndAttributeTypeUuid(@Nonnull Provider provider,
	        @Nonnull String providerAttributeTypeUuid) {
		OpenmrsFhirCriteriaContext<ProviderAttribute, ProviderAttribute> criteriaContext = createCriteriaContext(
		    ProviderAttribute.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().join("provider").get("providerId"),
		        provider.getId()),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().join("attributeType").get("uuid"),
		        providerAttributeTypeUuid),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("voided"), false)));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList();
	}
}
