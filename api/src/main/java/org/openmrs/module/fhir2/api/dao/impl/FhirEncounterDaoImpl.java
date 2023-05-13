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

import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hl7.fhir.r4.model.Encounter.SP_DATE;
import static org.openmrs.module.fhir2.api.util.LastnOperationUtils.getTopNRankedIds;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.openmrs.Encounter;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.LastnResult;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class FhirEncounterDaoImpl extends BaseEncounterDao<Encounter> implements FhirEncounterDao {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		if (!theParams.getParameters(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER).isEmpty()) {
			Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(Encounter.class);
			
			setupSearchParams(criteria, theParams);
			
			criteria.setProjection(Projections.projectionList().add(property("uuid")).add(property("encounterDatetime")));
			
			@SuppressWarnings("unchecked")
			List<LastnResult<String>> results = ((List<Object[]>) criteria.list()).stream()
			        .map(array -> new LastnResult<String>(array)).collect(Collectors.toList());
			
			return getTopNRankedIds(results, getMaxParameter(theParams));
		}
		
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(Encounter.class);
		
		handleVoidable(criteria);
		
		setupSearchParams(criteria, theParams);
		handleSort(criteria, theParams.getSortSpec());
		
		criteria.setProjection(Projections.property("uuid"));
		
		@SuppressWarnings("unchecked")
		List<String> results = criteria.list();
		
		return results.stream().distinct().collect(Collectors.toList());
	}
	
	private int getMaxParameter(SearchParameterMap theParams) {
		return ((NumberParam) theParams.getParameters(FhirConstants.MAX_SEARCH_HANDLER).get(0).getParam()).getValue()
		        .intValue();
	}
	
	@Override
	protected void handleDate(Criteria criteria, DateRangeParam dateRangeParam) {
		handleDateRange("encounterDatetime", dateRangeParam).ifPresent(criteria::add);
	}
	
	@Override
	protected void handleEncounterType(Criteria criteria, TokenAndListParam tokenAndListParam) {
		handleAndListParam(tokenAndListParam, t -> Optional.of(eq("et.uuid", t.getValue())))
		        .ifPresent(t -> criteria.createAlias("encounterType", "et").add(t));
	}
	
	@Override
	protected void handleParticipant(Criteria criteria, ReferenceAndListParam referenceAndListParam) {
		criteria.createAlias("encounterProviders", "ep");
		handleParticipantReference(criteria, referenceAndListParam);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case SP_DATE:
				return "encounterDatetime";
			default:
				return null;
		}
	}
}
