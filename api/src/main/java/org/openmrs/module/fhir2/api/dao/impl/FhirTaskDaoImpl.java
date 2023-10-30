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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirTask;
import org.springframework.stereotype.Component;

@Component
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class FhirTaskDaoImpl extends BaseFhirDao<FhirTask> implements FhirTaskDao {
	
	private List<Predicate> predicateList = new ArrayList<>();
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FhirTask> criteriaQuery = criteriaBuilder.createQuery(FhirTask.class);
		Root<FhirTask> root = criteriaQuery.from(FhirTask.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) param.getParam(), "basedOnReferences", "bo"));
					break;
				case FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) param.getParam(), "ownerReference", "o"));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleStatus((TokenAndListParam) param.getParam()).ifPresent(predicateList::add));
					criteriaQuery.where(predicateList.toArray(new Predicate[] {}));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicateList::add);
					criteriaQuery.where(predicateList.toArray(new Predicate[] {}));
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
	
	private Optional<Predicate> handleStatus(TokenAndListParam tokenAndListParam) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FhirTask> criteriaQuery = criteriaBuilder.createQuery(FhirTask.class);
		Root<FhirTask> root = criteriaQuery.from(FhirTask.class);
		
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				try {
					return Optional.of(criteriaBuilder.equal(root.get("status"),
					    FhirTask.TaskStatus.valueOf(token.getValue().toUpperCase())));
				}
				catch (IllegalArgumentException e) {
					return Optional.empty();
				}
			}
			
			return Optional.empty();
		});
	}
	
	private void handleReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam reference, String property,
	        String alias) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FhirTask> criteriaQuery = criteriaBuilder.createQuery(FhirTask.class);
		Root<FhirTask> root = criteriaQuery.from(FhirTask.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(reference, param -> {
			if (validReferenceParam(param)) {
				if (lacksAlias(finalCriteriaBuilder, alias)) {
					root.join(property).alias(alias);
				}
				
				List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
				predicateList.add(
				    Optional.of(finalCriteriaBuilder.equal(root.get(String.format("%s.reference", alias)), param.getIdPart())));
				predicateList.add(
				    Optional.of(finalCriteriaBuilder.equal(root.get(String.format("%s.type", alias)), param.getResourceType())));
				return Optional.of(finalCriteriaBuilder.and(toCriteriaArray(predicateList)));
			}
			
			return Optional.empty();
		}).ifPresent(predicateList::add);
		criteriaQuery.where(predicateList.toArray(new Predicate[] {}));
	}
}
