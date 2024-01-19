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
 * {@code OpenmrsFhirCriteriaContext} is a holder object for building criteria queries in the FHIR2 DAO API. It is
 * provided as a convenience since the old Hibernate Criteria API allowed us to simply pass a Criteria object around,
 * but the JPA2 Criteria API requires us to pass several different classes around.
 * <br/><br/>
 * Criteria queries are built up mostly by calling methods on this class to add various joins, predicates, and sort orders
 * which are built into a {@link CriteriaQuery<U>} by calling {@link #finalizeQuery()}. {@link #finalizeQuery()} should
 * only be called once the full query has been built and only just before running the query if possible.
 * <br/><br/>
 * The type {@code T} indicates the type of object that is the "root" of the query. For most queries, the type {@code U},
 * which is the expected type of the result, will be the same as {@code T}; however, for some queries, like those that
 * count results, {@code U} will have a different type.
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
	
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias) {
		return addJoin(attributeName, alias, JoinType.INNER);
	}
	
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias, @Nonnull JoinType joinType) {
		return addJoin(getRoot(), attributeName, alias, joinType);
	}
	
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias) {
		return addJoin(from, attributeName, alias, JoinType.INNER);
	}
	
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        @Nonnull JoinType joinType) {
		return addJoin(from, attributeName, alias, joinType, null);
	}
	
	public Join<?, ?> addJoin(From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        Function<From<?, ?>, Predicate> onGenerator) {
		return addJoin(from, attributeName, alias, JoinType.INNER, null);
	}
	
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
	
	public boolean hasAlias(String alias) {
		return aliases.containsKey(alias);
	}
	
	public CriteriaQuery<U> finalizeQuery() {
		return criteriaQuery.where(predicates.toArray(new Predicate[0])).orderBy(orders);
	}
}
