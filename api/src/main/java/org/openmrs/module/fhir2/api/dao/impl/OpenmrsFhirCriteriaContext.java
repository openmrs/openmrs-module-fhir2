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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class OpenmrsFhirCriteriaContext<T, U> {
	
	@Getter
	@NonNull
	private final EntityManager entityManager;
	
	@Getter
	@NonNull
	private final CriteriaBuilder criteriaBuilder;
	
	@Getter
	@NonNull
	private final CriteriaQuery<U> criteriaQuery;
	
	@Getter
	@NonNull
	private final Root<T> root;
	
	private final Map<String, Join<?, ?>> aliases = new LinkedHashMap<>();
	
	private final List<Predicate> predicates = new ArrayList<>();
	
	private final List<Order> orders = new ArrayList<>();
	
	@Getter
	private final List<T> results = new ArrayList<>();
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join implicitly
	 * joins with the root using the specified attribute.
	 *
	 * @param attributeName The name of the attribute (from the root object) to join on
	 * @param alias The alias to use for this join
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias) {
		return addJoin(attributeName, alias, JoinType.INNER);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join implicitly
	 * joins with the root using the specified attribute.
	 *
	 * @param attributeName The name of the attribute (from the root object) to join on
	 * @param alias The alias to use for this join
	 * @param joinType The {@link JoinType} representing the type of join, i.e., {@code INNER},
	 *            {@code LEFT}, {@code RIGHT}
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias, @Nonnull JoinType joinType) {
		return addJoin(getRoot(), attributeName, alias, joinType);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join is
	 * explicitly joined to the {@link From} object passed in, which should be either the root or
	 * another join.
	 *
	 * @param from The {@link From} object that represents the table to join with
	 * @param attributeName The name of the attribute (from the {@link From} object) to join on
	 * @param alias The alias to use for this join
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias) {
		return addJoin(from, attributeName, alias, JoinType.INNER);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join is
	 * explicitly joined to the {@link From} object passed in, which should be either the root or
	 * another join.
	 *
	 * @param from The {@link From} object that represents the table to join with
	 * @param attributeName The name of the attribute (from the {@link From} object) to join on
	 * @param alias The alias to use for this join
	 * @param joinType The {@link JoinType} representing the type of join, i.e., {@code INNER},
	 *            {@code LEFT}, {@code RIGHT}
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        @Nonnull JoinType joinType) {
		return addJoin(from, attributeName, alias, joinType, null);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join is
	 * explicitly joined to the {@link From} object passed in, which should be either the root or
	 * another join. This join is filtered using the resulting predicate returned from the
	 * {@param onGenerator} parameter.
	 *
	 * @param from The {@link From} object that represents the table to join with
	 * @param attributeName The name of the attribute (from the {@link From} object) to join on
	 * @param alias The alias to use for this join
	 * @param onGenerator A {@link Function} that takes a {@link From} object and returns the predicate
	 *            for the on clause
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        Function<From<?, ?>, Predicate> onGenerator) {
		return addJoin(from, attributeName, alias, JoinType.INNER, onGenerator);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join is
	 * explicitly joined to the {@link From} object passed in, which should be either the root or
	 * another join. This join is filtered using the resulting predicate returned from the
	 * {@param onGenerator} parameter.
	 *
	 * @param from The {@link From} object that represents the table to join with
	 * @param attributeName The name of the attribute (from the {@link From} object) to join on
	 * @param alias The alias to use for this join
	 * @param joinType The {@link JoinType} representing the type of join, i.e., {@code INNER},
	 *            {@code LEFT}, {@code RIGHT}
	 * @param onGenerator A {@link Function} that takes a {@link From} object and returns the predicate
	 *            for the on clause
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        @Nonnull JoinType joinType, Function<From<?, ?>, Predicate> onGenerator) {
		return Optional.ofNullable(aliases.get(alias)).orElseGet(() -> {
			Join newJoin = from.join(attributeName, joinType);
			newJoin.alias(alias);
			aliases.put(alias, newJoin);
			
			if (onGenerator != null) {
				Predicate onPredicate = onGenerator.apply(newJoin);
				if (onPredicate != null) {
					newJoin.on(onPredicate);
				}
			}
			
			return newJoin;
		});
	}
	
	public <V> OpenmrsFhirCriteriaSubquery<V, V> addSubquery(Class<V> type) {
		return addSubquery(type, type);
	}
	
	public <V, W> OpenmrsFhirCriteriaSubquery<V, W> addSubquery(Class<V> fromType, Class<W> resultType) {
		Subquery<W> subquery = criteriaQuery.subquery(resultType);
		return new OpenmrsFhirCriteriaSubquery<>(criteriaBuilder, subquery, subquery.from(fromType));
	}
	
	public OpenmrsFhirCriteriaContext<T, U> addPredicate(Predicate predicate) {
		predicates.add(predicate);
		return this;
	}
	
	public OpenmrsFhirCriteriaContext<T, U> addOrder(Order order) {
		orders.add(order);
		return this;
	}
	
	public OpenmrsFhirCriteriaContext<T, U> addResults(T result) {
		results.add(result);
		return this;
	}
	
	public Optional<Join<?, ?>> getJoin(String alias) {
		return Optional.ofNullable(aliases.get(alias));
	}
	
	public CriteriaQuery<U> finalizeQuery() {
		return criteriaQuery.where(predicates.toArray(new Predicate[0])).orderBy(orders);
	}
}
