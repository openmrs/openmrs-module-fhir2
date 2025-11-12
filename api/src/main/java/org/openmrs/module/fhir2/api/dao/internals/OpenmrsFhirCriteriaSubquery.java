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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * {@code OpenmrsFhirCriteriaSubquery} is a holder object for creating subqueries for query defined
 * by a {@link OpenmrsFhirCriteriaContext}. Like a {@link OpenmrsFhirCriteriaContext}, it has its
 * own set of predicates, joins, and its own root type. <br/>
 * Unlike {@link OpenmrsFhirCriteriaContext}, it has an optional {@link Expression} that is used to
 * define the query projection. This is included because it is expected that a subquery will be used
 * to partially filter a main query.
 * <br/>
 * <strong>Thread Safety:</strong> This class is <em>not</em> thread-safe. Instances maintain
 * mutable state (predicates, joins, projection) and should be used within a single request context.
 * Do not share instances across threads.
 *
 * @param <V> The root type for the query
 * @param <U> The type for the result of the query
 */
public class OpenmrsFhirCriteriaSubquery<V, U> extends BaseFhirCriteriaHolder<V> {
	
	@Getter(onMethod = @__({ @Nonnull }))
	Subquery<U> subquery;
	
	/**
	 * The projection expression for this subquery. This defines what the subquery will return (e.g., an
	 * ID field, a COUNT, or another expression).
	 * <br/>
	 * If no projection is set, the subquery must have its select clause set explicitly before
	 * finalization. The projection will be applied when {@link #finalizeQuery()} is called.
	 */
	@Setter
	Expression<U> projection = null;
	
	public OpenmrsFhirCriteriaSubquery(@NonNull CriteriaBuilder criteriaBuilder, @Nonnull Subquery<U> subquery,
	    @NonNull Root<V> root) {
		super(criteriaBuilder, root);
		this.subquery = subquery;
	}
	
	/**
	 * Adds a new predicate to the list of predicates being applied to the subquery under construction.
	 * <br/>
	 * This method overrides the parent to return the more specific {@link OpenmrsFhirCriteriaSubquery}
	 * type for method chaining.
	 *
	 * @param predicate The {@link Predicate} to add
	 * @return The current subquery context to facilitate chaining
	 */
	@Override
	public OpenmrsFhirCriteriaSubquery<V, U> addPredicate(Predicate predicate) {
		return (OpenmrsFhirCriteriaSubquery<V, U>) super.addPredicate(predicate);
	}
	
	/**
	 * Finalizes the subquery by applying the projection (if set) and all accumulated predicates. This
	 * should be called once the subquery has been fully constructed and is ready to be used within the
	 * parent query.
	 * <br/>
	 * If a projection has been set via {@link #setProjection(Expression)}, it will be applied as the
	 * SELECT clause. Otherwise, the subquery's SELECT clause should have been set explicitly.
	 *
	 * @return The finalized {@link Subquery} with SELECT and WHERE clauses applied
	 */
	public Subquery<U> finalizeQuery() {
		if (projection != null) {
			subquery = subquery.select(projection);
		}
		
		if (!getPredicates().isEmpty()) {
			subquery = subquery.where(getPredicates().toArray(new Predicate[0]));
		}
		
		return subquery;
	}
}
