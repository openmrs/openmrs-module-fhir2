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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

/**
 * {@code OpenmrsFhirCriteriaContext} is a holder object for building criteria queries in the FHIR2
 * DAO API. It is provided as a convenience since the old Hibernate Criteria API allowed us to
 * simply pass a Criteria object around, but the JPA2 Criteria API requires us to pass several
 * different classes around. <br/>
 * <br/>
 * Criteria queries are built up mostly by calling methods on this class to add various joins,
 * predicates, and sort orders which are built into a {@link CriteriaQuery<U>} by calling
 * {@link #finalizeQuery()}. {@link #finalizeQuery()} should only be called once the full query has
 * been built and only just before running the query if possible. <br/>
 * <br/>
 * The type {@code T} indicates the type of object that is the "root" of the query. For most
 * queries, the type {@code U}, which is the expected type of the result, will be the same as
 * {@code T}; however, for some queries, like those that count results, {@code U} will have a
 * different type.
 *
 * @param <T> The root type for the query
 * @param <U> The type for the result of the query
 */
public class OpenmrsFhirCriteriaContext<T, U> extends BaseFhirCriteriaHolder<T> {
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final EntityManager entityManager;
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final CriteriaQuery<U> criteriaQuery;
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final List<Order> orders = new ArrayList<>();
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final List<T> results = new ArrayList<>();
	
	public OpenmrsFhirCriteriaContext(@Nonnull EntityManager entityManager, @NonNull CriteriaBuilder criteriaBuilder,
	    @Nonnull CriteriaQuery<U> criteriaQuery, @NonNull Root<T> root) {
		super(criteriaBuilder, root);
		this.criteriaQuery = criteriaQuery;
		this.entityManager = entityManager;
	}
	
	/**
	 * This function creates a new subquery for this query. Since the return type for this function is
	 * not specified, it is assumed to be an {@link Integer}, since most subqueries will be used to
	 * correlate objects by id. <br/>
	 * Note that by default this subquery is not in any way correlated with the main query.
	 *
	 * @param fromType The root type for the new subquery
	 * @return A {@link OpenmrsFhirCriteriaSubquery} to hold the state of the subquery
	 * @param <V> The root type of the new subquery
	 */
	public <V> OpenmrsFhirCriteriaSubquery<V, Integer> addSubquery(Class<V> fromType) {
		return addSubquery(fromType, Integer.class);
	}
	
	/**
	 * This function creates a new subquery for this query, with the specified root type and return
	 * type. <br/>
	 * Note that by default the returned subquery is not in any way correlated with the main query.
	 *
	 * @param fromType The root type for the new subquery
	 * @param resultType The type of object this subquery returns
	 * @return A {@link OpenmrsFhirCriteriaSubquery} to hold the state of the subquery
	 * @param <V> The root type of the new subquery
	 * @param <X> The return type of the new subquery
	 */
	public <V, X> OpenmrsFhirCriteriaSubquery<V, X> addSubquery(Class<V> fromType, Class<X> resultType) {
		Subquery<X> subquery = getCriteriaQuery().subquery(resultType);
		return new OpenmrsFhirCriteriaSubquery<>(getCriteriaBuilder(), subquery, subquery.from(fromType));
	}
	
	/**
	 * This function adds a new predicate to the list of predicates being applied to the query under
	 * construction.
	 *
	 * @param predicate The {@link Predicate} to add
	 * @return The current context to facilitate chaining
	 */
	@Override
	public OpenmrsFhirCriteriaContext<T, U> addPredicate(Predicate predicate) {
		return (OpenmrsFhirCriteriaContext<T, U>) super.addPredicate(predicate);
	}
	
	public OpenmrsFhirCriteriaContext<T, U> addOrder(Order order) {
		orders.add(order);
		return this;
	}
	
	public OpenmrsFhirCriteriaContext<T, U> addResults(T result) {
		results.add(result);
		return this;
	}
	
	public CriteriaQuery<U> finalizeQuery() {
		CriteriaQuery<U> cq = getCriteriaQuery();
		if (!getPredicates().isEmpty()) {
			cq = cq.where(getPredicates().toArray(new Predicate[0]));
		}
		
		if (!orders.isEmpty()) {
			cq = cq.orderBy(orders);
		}
		
		return cq;
	}
	
	/**
	 * Finalizes a query that selects only the ID property, optionally including sort columns in the
	 * projection to enable sorting with DISTINCT. This is used for the two-query approach where we
	 * first get matching IDs, then fetch full objects.
	 * <p/>
	 * This method always returns results as Object[] to maintain consistency, with the ID as the first
	 * element. When sort orders are provided, additional sort expressions are included in the SELECT
	 * clause, which is necessary for databases to sort DISTINCT results correctly.
	 *
	 * @param idProperty The name of the ID property to select
	 * @return A finalized {@link CriteriaQuery} selecting ID and sort columns as Object[]
	 */
	public CriteriaQuery<U> finalizeIdQuery(String idProperty) {
		CriteriaQuery<U> query = getCriteriaQuery().where(getPredicates().toArray(new Predicate[0])).distinct(true);
		
		List<Selection<?>> selections = new ArrayList<>();
		selections.add(getRoot().get(idProperty));
		
		// If there are sort orders, include sort expressions in the selection
		// for DISTINCT to work properly with ORDER BY
		if (!orders.isEmpty()) {
			for (Order order : orders) {
				selections.add(order.getExpression());
			}
			query.orderBy(orders);
		}
		
		// Always use multiselect to return Object[] for consistency
		query.multiselect(selections);
		
		return query;
	}
	
	public CriteriaQuery<U> finalizeWrapperQuery(String idProperty, Collection<Integer> ids) {
		return getCriteriaQuery().where(getRoot().get(idProperty).in(ids)).orderBy(orders);
	}
}
