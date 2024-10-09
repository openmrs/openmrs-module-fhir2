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

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.or;
import static org.hl7.fhir.r4.model.Patient.SP_DEATH_DATE;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.sql.JoinType;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPatientDaoImpl extends BasePersonDao<Patient> implements FhirPatientDao {
	
	@Autowired
	private FhirGroupDao groupDao;
	
	@Override
	public Patient getPatientById(@Nonnull Integer id) {
		return (Patient) getSessionFactory().getCurrentSession().createCriteria(Patient.class).add(eq("patientId", id))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Patient> getPatientsByIds(@Nonnull Collection<Integer> ids) {
		return getSessionFactory().getCurrentSession().createCriteria(Patient.class).add(in("id", ids)).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid) {
		List<PatientIdentifierType> identifierTypes = (List<PatientIdentifierType>) getSessionFactory().getCurrentSession()
		        .createCriteria(PatientIdentifierType.class)
		        .add(or(and(eq("name", name), eq("retired", false)), eq("uuid", uuid))).list();
		
		if (identifierTypes.isEmpty()) {
			return null;
		} else {
			// favour uuid if one was supplied
			if (uuid != null) {
				try {
					return identifierTypes.stream().filter((idType) -> uuid.equals(idType.getUuid())).findFirst()
					        .orElse(identifierTypes.get(0));
				}
				catch (NoSuchElementException ignored) {}
			}
			
			return identifierTypes.get(0);
		}
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.QUERY_SEARCH_HANDLER:
					entry.getValue().forEach(query -> handlePatientQuery(criteria, (StringAndListParam) query.getParam()));
					break;
				case FhirConstants.NAME_SEARCH_HANDLER:
					handleNames(criteria, entry.getValue());
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    p -> handleGender(p.getPropertyName(), (TokenAndListParam) p.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(identifier -> handleIdentifier(criteria, (TokenAndListParam) identifier.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    b -> handleBoolean(b.getPropertyName(), (TokenAndListParam) b.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteria, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
				case FhirConstants.HAS_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleHasAndListParam(criteria, (HasAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleHasAndListParam(Criteria criteria, HasAndListParam hasAndListParam) {
		if (hasAndListParam != null) {
			List<String> groupIds = new ArrayList<>();
			hasAndListParam.getValuesAsQueryTokens().forEach(hasOrListParam -> {
				hasOrListParam.getValuesAsQueryTokens().forEach(hasParam -> {
					if (hasParam != null) {
						String paramValue = hasParam.getParameterValue();
						switch (hasParam.getTargetResourceType()) {
							case FhirConstants.GROUP:
								switch (hasParam.getReferenceFieldName()) {
									case FhirConstants.INCLUDE_MEMBER_PARAM:
										groupIds.add(paramValue);
										break;
								}
								break;
						}
					}
				});
			});
			
			if (!groupIds.isEmpty()) {
				verifyPatientInGroups(criteria, groupIds);
			}
		}
	}
	
	private void verifyPatientInGroups(Criteria criteria, List<String> groupIds) {
		Set<Integer> patientIds = new HashSet<>();
		groupIds.forEach(groupId -> patientIds.addAll(getGroupMemberIds(groupId)));
		
		criteria.add(in("patientId", patientIds.isEmpty() ? Collections.emptyList() : patientIds));
	}
	
	private List<Integer> getGroupMemberIds(String groupId) {
		Cohort cohort = groupDao.get(groupId);
		if (cohort != null) {
			return cohort.getMemberships().stream().map(CohortMembership::getPatientId).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	private void handlePatientQuery(Criteria criteria, @Nonnull StringAndListParam query) {
		if (query == null) {
			return;
		}
		
		if (lacksAlias(criteria, "pn")) {
			criteria.createAlias("names", "pn");
		}
		
		if (lacksAlias(criteria, "pi")) {
			criteria.createAlias("identifiers", "pi");
		}
		
		handleAndListParam(query, q -> {
			List<Optional<? extends Criterion>> criterionList = new ArrayList<>();
			
			for (String token : StringUtils.split(q.getValueNotNull(), " \t,")) {
				StringParam param = new StringParam(token).setContains(q.isContains()).setExact(q.isExact());
				criterionList.add(propertyLike("pn.givenName", param).map(c -> and(c, eq("pn.voided", false))));
				criterionList.add(propertyLike("pn.middleName", param).map(c -> and(c, eq("pn.voided", false))));
				criterionList.add(propertyLike("pn.familyName", param).map(c -> and(c, eq("pn.voided", false))));
			}
			
			criterionList.add(propertyLike("pi.identifier",
			    new StringParam(q.getValueNotNull()).setContains(q.isContains()).setExact(q.isExact()))
			            .map(c -> and(c, eq("pi.voided", false))));
			
			return Optional.of(or(toCriteriaArray(criterionList)));
		}).ifPresent(criteria::add);
	}
	
	protected void handleIdentifier(Criteria criteria, TokenAndListParam identifier) {
		if (identifier == null) {
			return;
		}
		
		criteria.createAlias("identifiers", "pi", JoinType.INNER_JOIN, eq("pi.voided", false));
		
		handleAndListParamBySystem(identifier, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(in("pi.identifier", tokensToList(tokens)));
			} else {
				if (lacksAlias(criteria, "pit")) {
					criteria.createAlias("pi.identifierType", "pit", JoinType.INNER_JOIN, eq("pit.retired", false));
				}
				
				return Optional.of(and(eq("pit.name", system), in("pi.identifier", tokensToList(tokens))));
			}
		}).ifPresent(criteria::add);
	}
	
	@Override
	protected String getSqlAlias() {
		return "this_1_";
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		if (SP_DEATH_DATE.equalsIgnoreCase(param)) {
			return "deathDate";
		}
		
		return super.paramToProp(param);
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
}
