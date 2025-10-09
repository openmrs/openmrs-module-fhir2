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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirTask;
import org.springframework.stereotype.Component;

@Component
public class FhirTaskDaoImpl extends BaseFhirDao<FhirTask> implements FhirTaskDao {
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<FhirTask, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "basedOnReferences", "bo"));
					break;
				case FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "ownerReference", "o"));
					break;
				case FhirConstants.FOR_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "forReference", "f"));
					break;
				case FhirConstants.TASK_CODE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(code -> handleTaskCodeConcept(criteriaContext, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleStatus(criteriaContext, (TokenAndListParam) param.getParam())
					        .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	@Override
	public FhirTask createOrUpdate(@Nonnull FhirTask task) throws DAOException {
		// TODO: Refactor - and figure out why CascadeType.ALL does not take care of this.
		if (task.getOwnerReference() != null && task.getOwnerReference().getReference() != null) {
			getSessionFactory().getCurrentSession().saveOrUpdate(task.getOwnerReference());
		}
		
		getSessionFactory().getCurrentSession().saveOrUpdate(task);
		
		return task;
	}
	
	private Boolean validReferenceParam(ReferenceParam ref) {
		return (ref != null && ref.getIdPart() != null && ref.getResourceType() != null);
	}
	
	private <U> Optional<Predicate> handleStatus(OpenmrsFhirCriteriaContext<FhirTask, U> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), tokenAndListParam, token -> {
			if (token.getValue() != null) {
				try {
					return Optional.of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("status"),
					    FhirTask.TaskStatus.valueOf(token.getValue().toUpperCase())));
				}
				catch (IllegalArgumentException e) {
					return Optional.empty();
				}
			}
			
			return Optional.empty();
		});
	}
	
	private <U> void handleReference(OpenmrsFhirCriteriaContext<FhirTask, U> criteriaContext,
	        ReferenceAndListParam reference, String property, String alias) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), reference, param -> {
			if (validReferenceParam(param)) {
				Join<?, ?> taskAliasJoin = criteriaContext.addJoin(property, alias);
				
				List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
				predicateList.add(Optional
				        .of(criteriaContext.getCriteriaBuilder().equal(taskAliasJoin.get("reference"), param.getIdPart())));
				predicateList.add(Optional
				        .of(criteriaContext.getCriteriaBuilder().equal(taskAliasJoin.get("type"), param.getResourceType())));
				return Optional.of(criteriaContext.getCriteriaBuilder().and(toCriteriaArray(predicateList)));
			}
			
			return Optional.empty();
		}).ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleTaskCodeConcept(OpenmrsFhirCriteriaContext<FhirTask, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?, ?> join = criteriaContext.addJoin("taskCode", "tc");
			
			handleCodeableConcept(criteriaContext, code, join, "tcm", "tcrt").ifPresent(criteriaContext::addPredicate);
		}
	}
}
