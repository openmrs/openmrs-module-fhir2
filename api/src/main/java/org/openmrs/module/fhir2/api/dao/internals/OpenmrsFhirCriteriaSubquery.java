/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.internals;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * {@code OpenmrsFhirCriteriaSubquery} is a holder object for creating subqueries for query defined by a
 * {@link OpenmrsFhirCriteriaContext}. Like a {@link OpenmrsFhirCriteriaContext}, it has its own set of
 * predicates, joins, and its own root type.
 * <br/>
 * Unlike {@link OpenmrsFhirCriteriaContext}, it has an optional {@link Expression} that is used to define
 * the query projection. This is included because it is expected that a subquery will be used to partially
 * filter a main query.
 *
 * @param <V> The root type for the query
 * @param <U> The type for the result of the query
 */
public class OpenmrsFhirCriteriaSubquery<V, U> extends BaseFhirCriteriaHolder<V, U> {
	
	@Getter(onMethod = @__({ @Nonnull }))
	Subquery<U> subquery;
	
	@Setter
	Expression<U> projection = null;
	
	public OpenmrsFhirCriteriaSubquery(@NonNull CriteriaBuilder criteriaBuilder, @Nonnull Subquery<U> subquery,
	    @NonNull Root<V> root) {
		super(criteriaBuilder, root);
		this.subquery = subquery;
	}
	
	@Override
	public OpenmrsFhirCriteriaSubquery<V, U> addPredicate(Predicate predicate) {
		return (OpenmrsFhirCriteriaSubquery<V, U>) super.addPredicate(predicate);
	}
	
	public Subquery<U> finalizeQuery() {
		if (projection != null) {
			subquery = subquery.select(projection);
		}
		
		return subquery.where(getPredicates().toArray(new Predicate[0]));
	}
}
