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

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.or;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
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
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			if (FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER.equals(entry.getKey())) {
				entry.getValue().forEach(param -> handleManagingEntity(criteria, (ReferenceAndListParam) param.getParam()));
			}
		});
	}
	
	/*
	 *TODO
	 * Find a way to merge this logic into handleParticipantReference logic in the BaseDao class
	 * make it reusable
	 */
	protected void handleManagingEntity(Criteria criteria, ReferenceAndListParam participantReference) {
		if (participantReference != null) {
			criteria.createAlias("creator", "cr");
			
			handleAndListParam(participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						// Search by person (the person who created the cohort - creator) uuid
						case Practitioner.SP_RES_ID:
							if ((lacksAlias(criteria, "ps"))) {
								criteria.createAlias(PERSON_ALIAS, "ps");
							}
							return Optional.of(ilike("ps.uuid", participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias(PERSON_ALIAS, "ps").createAlias(NAMES_ALIAS, "pn");
							}
							return Optional.of(ilike("pn.givenName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias(PERSON_ALIAS, "ps").createAlias(NAMES_ALIAS, "pn");
							}
							return Optional.of(ilike("pn.familyName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_NAME:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias(PERSON_ALIAS, "ps").createAlias(NAMES_ALIAS, "pn");
							}
							
							List<Optional<? extends Criterion>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(or(toCriteriaArray(criterionList)));
					}
				} else {
					// Search by creator uuid
					return Optional.of(eq("cr.uuid", participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}
}
