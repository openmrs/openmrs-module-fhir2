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

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
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
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleReference(criteria, (ReferenceAndListParam) param.getParam(),
					    "basedOnReferences", "bo"));
					break;
				case FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleReference(criteria, (ReferenceAndListParam) param.getParam(), "ownerReference", "o"));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleStatus((TokenAndListParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
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
	
	private Optional<Criterion> handleStatus(TokenAndListParam tokenAndListParam) {
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				try {
					return Optional.of(eq("status", FhirTask.TaskStatus.valueOf(token.getValue().toUpperCase())));
				}
				catch (IllegalArgumentException e) {
					return Optional.empty();
				}
			}
			
			return Optional.empty();
		});
	}
	
	private void handleReference(Criteria criteria, ReferenceAndListParam reference, String property, String alias) {
		handleAndListParam(reference, param -> {
			if (validReferenceParam(param)) {
				if (lacksAlias(criteria, alias)) {
					criteria.createAlias(property, alias);
				}
				
				List<Optional<? extends Criterion>> criterionList = new ArrayList<>();
				criterionList.add(Optional.of(eq(String.format("%s.reference", alias), param.getIdPart())));
				criterionList.add(Optional.of(eq(String.format("%s.type", alias), param.getResourceType())));
				return Optional.of(and(toCriteriaArray(criterionList)));
			}
			
			return Optional.empty();
		}).ifPresent(criteria::add);
	}
}
