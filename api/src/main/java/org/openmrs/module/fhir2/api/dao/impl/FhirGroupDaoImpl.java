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
import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirGroupDaoImpl extends BaseFhirDao<Cohort> implements FhirGroupDao {
	
	private static final String NAMES_ALIAS = "ps.names";
	
	private static final String PERSON_ALIAS = "cr.person";
	
	@Override
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<Cohort> criteriaContext, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			if (FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER.equals(entry.getKey())) {
				entry.getValue()
				        .forEach(param -> handleManagingEntity(criteriaContext, (ReferenceAndListParam) param.getParam()));
			}
		});
	}
	
	/*
	 *TODO
	 * Find a way to merge this logic into handleParticipantReference logic in the BaseDao class
	 * make it reusable
	 */
	protected void handleManagingEntity(OpenmrsFhirCriteriaContext<Cohort> criteriaContext, ReferenceAndListParam participantReference) {
		if (participantReference != null) {
			criteriaContext.getRoot().join("creator");
			handleAndListParam(participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						// Search by person (the person who created the cohort - creator) uuid
						case Practitioner.SP_RES_ID:
							if ((lacksAlias(criteriaContext, "ps"))) {
								criteriaContext.getRoot().join(PERSON_ALIAS);
							}
							return Optional.of(criteriaContext.getCriteriaBuilder().like(criteriaContext.getRoot().get("ps.uuid"), participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(criteriaContext, "ps") && (lacksAlias(criteriaContext, "pn")))) {
								criteriaContext.getRoot().join(PERSON_ALIAS);
								criteriaContext.getRoot().join(NAMES_ALIAS);
							}
							return Optional.of(criteriaContext.getCriteriaBuilder().like(criteriaContext.getRoot().get("pn.givenName"), participantToken.getValue()));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(criteriaContext, "ps") && (lacksAlias(criteriaContext, "pn")))) {
								criteriaContext.getRoot().join(PERSON_ALIAS);
								criteriaContext.getRoot().join(NAMES_ALIAS);
							}
							return Optional.of(criteriaContext.getCriteriaBuilder().like(criteriaContext.getRoot().get("pn.familyName"), participantToken.getValue()));
						case Practitioner.SP_NAME:
							if ((lacksAlias(criteriaContext, "ps") && (lacksAlias(criteriaContext, "pn")))) {
								criteriaContext.getRoot().join(PERSON_ALIAS);
								criteriaContext.getRoot().join(NAMES_ALIAS);
							}
							
							List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(criterionList)));
					}
				} else {
					// Search by creator uuid
					return Optional.of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("cr.uuid"), participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
}
