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

import static org.hl7.fhir.r4.model.Patient.SP_DEATH_DATE;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirPatientDaoImpl extends BasePersonDao<Patient> implements FhirPatientDao {
	
	@Override
	@Transactional(readOnly = true)
	public Patient getPatientById(@Nonnull Integer id) {
		OpenmrsFhirCriteriaContext<Patient, Patient> criteriaContext = createCriteriaContext(Patient.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot())
		        .where(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("patientId"), id));
		
		TypedQuery<Patient> query = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery());
		return query.getResultList().stream().findFirst().orElse(null);
	}
	
	@Override
	public List<Patient> getPatientsByIds(@Nonnull Collection<Integer> ids) {
		OpenmrsFhirCriteriaContext<Patient, Patient> criteriaContext = createCriteriaContext(Patient.class);
		
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		criteriaContext.getCriteriaQuery().where(criteriaContext.getRoot().get("id").in(ids));
		return criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
	}
	
	@Override
	public PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid) {
		OpenmrsFhirCriteriaContext<PatientIdentifierType, PatientIdentifierType> criteriaContext = createCriteriaContext(
		    PatientIdentifierType.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot())
		        .where(
		            criteriaContext
		                    .getCriteriaBuilder().or(
		                        criteriaContext.getCriteriaBuilder().and(
		                            criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("name"), name),
		                            criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"),
		                                false)),
		                        criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), uuid)));
		List<PatientIdentifierType> identifierTypes = criteriaContext.getEntityManager()
		        .createQuery(criteriaContext.getCriteriaQuery()).getResultList();
		
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
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Patient, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.QUERY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(query -> handlePatientQuery(criteriaContext, (StringAndListParam) query.getParam()));
					break;
				case FhirConstants.NAME_SEARCH_HANDLER:
					handleNames(criteriaContext, entry.getValue());
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(p -> handleGender(criteriaContext, getPersonProperty(criteriaContext),
					            FhirConstants.GENDER_PROPERTY, (TokenAndListParam) p.getParam())
					                    .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    identifier -> handleIdentifier(criteriaContext, (TokenAndListParam) identifier.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(dateRangeParam -> handleDateRange(criteriaContext, dateRangeParam.getPropertyName(),
					            (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    b -> handleBoolean(criteriaContext, b.getPropertyName(), (TokenAndListParam) b.getParam())
					            .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaContext, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
				case FhirConstants.HAS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleHasAndListParam(criteriaContext, (HasAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected <U> void handleHasAndListParam(OpenmrsFhirCriteriaContext<Patient, U> criteriaContext,
	        HasAndListParam hasAndListParam) {
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
										switch (hasParam.getParameterName()) {
											case "id":
												groupIds.add(paramValue);
										}
										break;
								}
								break;
						}
					}
				});
			});
			
			if (!groupIds.isEmpty()) {
				verifyPatientInGroups(criteriaContext, groupIds);
			}
		}
	}
	
	private <U> void verifyPatientInGroups(OpenmrsFhirCriteriaContext<Patient, U> criteriaContext, List<String> groupIds) {
		Set<Integer> patientIds = new HashSet<>();
		groupIds.forEach(groupId -> patientIds.addAll(getGroupMemberIds(groupId)));
		
		criteriaContext.addPredicate(criteriaContext.getRoot().get("patientId").in(patientIds));
	}
	
	private List<Integer> getGroupMemberIds(String groupId) {
		OpenmrsFhirCriteriaContext<CohortMembership, Integer> criteriaContext = createCriteriaContext(CohortMembership.class,
		    Integer.class);
		CriteriaBuilder cb = criteriaContext.getCriteriaBuilder();
		
		criteriaContext.addPredicate(cb.equal(criteriaContext.getRoot().get("cohort").get("uuid"), groupId));
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot().get("patientId"));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList();
	}
	
	private <U> void handlePatientQuery(OpenmrsFhirCriteriaContext<Patient, U> criteriaContext,
	        @Nonnull StringAndListParam query) {
		if (query == null) {
			return;
		}
		Join<?, ?> personNameJoin = criteriaContext.addJoin("names", "pn");
		Join<?, ?> identifiersJoin = criteriaContext.addJoin("identifiers", "pi");
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(personNameJoin.get("voided"), false));
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(identifiersJoin.get("voided"), false));
		
		handleAndListParam(criteriaContext.getCriteriaBuilder(), query, q -> {
			List<Optional<? extends Predicate>> arrayList = new ArrayList<>();
			
			for (String token : StringUtils.split(q.getValueNotNull(), " \t,")) {
				StringParam param = new StringParam(token).setContains(q.isContains()).setExact(q.isExact());
				arrayList.add(propertyLike(criteriaContext, personNameJoin, "givenName", param));
				arrayList.add(propertyLike(criteriaContext, personNameJoin, "middleName", param));
				arrayList.add(propertyLike(criteriaContext, personNameJoin, "familyName", param));
			}
			
			arrayList.add(propertyLike(criteriaContext, identifiersJoin, "identifier",
			    new StringParam(q.getValueNotNull()).setContains(q.isContains()).setExact(q.isExact())));
			
			return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(arrayList)));
		}).ifPresent(criteriaContext::addPredicate);
	}
	
	protected <U> void handleIdentifier(OpenmrsFhirCriteriaContext<Patient, U> criteriaContext,
	        TokenAndListParam identifier) {
		if (identifier == null) {
			return;
		}
		
		Join<?, ?> identifiersJoin = criteriaContext.addJoin("identifiers", "pi");
		criteriaContext.getCriteriaBuilder().equal(identifiersJoin.get("voided"), false);
		
		handleAndListParamBySystem(criteriaContext.getCriteriaBuilder(), identifier, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().in(identifiersJoin.get("identifier")).value(tokensToList(tokens)));
			} else {
				Join<?, ?> identifiersIdentifierTypeJoin = criteriaContext.addJoin(identifiersJoin, "identifierType", "pit");
				criteriaContext.getCriteriaBuilder().equal(identifiersIdentifierTypeJoin.get("retired"), false);
				
				return Optional.of(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder().equal(identifiersIdentifierTypeJoin.get("name"), system),
				    criteriaContext.getCriteriaBuilder().in(identifiersJoin.get("identifier")).value(tokensToList(tokens))));
			}
		}).ifPresent(criteriaContext::addPredicate);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @NonNull String param) {
		if (SP_DEATH_DATE.equalsIgnoreCase(param)) {
			return criteriaContext.getRoot().get("deathDate");
		}
		return super.paramToProp(criteriaContext, param);
	}
	
	@Override
	protected <V, U> String getIdPropertyName(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext) {
		// since a patient is-a person, the id returned by default is "personId", but this is not actually
		// a property of patient, so the selection fails
		return "patientId";
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
}
