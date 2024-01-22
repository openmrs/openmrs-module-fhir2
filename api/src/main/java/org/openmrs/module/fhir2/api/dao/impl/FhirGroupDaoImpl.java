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

import javax.persistence.criteria.Join;
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
	
	private static final String NAMES_ALIAS = "names";
	
	private static final String PERSON_ALIAS = "person";
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Cohort,U> criteriaContext, SearchParameterMap theParams) {
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
	protected <U> void handleManagingEntity(OpenmrsFhirCriteriaContext<Cohort,U> criteriaContext,
	        ReferenceAndListParam participantReference) {
		if (participantReference != null) {
			Join<?, ?> creatorJoin = criteriaContext.addJoin("creator", "cr");
			handleAndListParam(criteriaContext.getCriteriaBuilder(), participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						// Search by person (the person who created the cohort - creator) uuid
						case Practitioner.SP_RES_ID: {
							Join<?, ?> creatorPersonJoin = criteriaContext.addJoin(creatorJoin, PERSON_ALIAS, "ps");
							return Optional.of(criteriaContext.getCriteriaBuilder().like(creatorPersonJoin.get("uuid"),
							    participantToken.getValue()));
						}
						case Practitioner.SP_GIVEN: {
							Join<?, ?> creatorPersonJoin = criteriaContext.addJoin(creatorJoin, PERSON_ALIAS, "ps");
							Join<?, ?> creatorPersonNameJoin = criteriaContext.addJoin(creatorPersonJoin, NAMES_ALIAS, "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(creatorPersonNameJoin.get("givenName"), participantToken.getValue()));
						}
						case Practitioner.SP_FAMILY: {
							Join<?, ?> creatorPersonJoin = criteriaContext.addJoin(creatorJoin, PERSON_ALIAS, "ps");
							Join<?, ?> creatorPersonNameJoin = criteriaContext.addJoin(creatorPersonJoin, NAMES_ALIAS, "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(creatorPersonNameJoin.get("familyName"), participantToken.getValue()));
						}
						case Practitioner.SP_NAME: {
							List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
							Join<?, ?> creatorPersonJoin = criteriaContext.addJoin(creatorJoin, PERSON_ALIAS, "ps");
							Join<?, ?> creatorPersonNameJoin = criteriaContext.addJoin(creatorPersonJoin, NAMES_ALIAS, "pn");
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								criterionList.add(propertyLike(criteriaContext, creatorPersonNameJoin, "givenName", token));
								criterionList.add(propertyLike(criteriaContext, creatorPersonNameJoin, "middleName", token));
								criterionList.add(propertyLike(criteriaContext, creatorPersonNameJoin, "familyName", token));
							}
							
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(criterionList)));
						}
					}
				} else {
					// Search by creator uuid
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(creatorJoin.get("uuid"), participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
}
