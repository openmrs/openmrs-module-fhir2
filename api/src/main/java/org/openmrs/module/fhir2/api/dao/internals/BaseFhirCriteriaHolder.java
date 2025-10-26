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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is a common super-type for classes which attempt to allow building a query or subquery. This
 * allows us to pass a single object around, similar to what was done with the previous Hibernate
 * Criteria-based API.
 *
 * @param <V> The type of the root entity for this query
 */
@RequiredArgsConstructor
public abstract class BaseFhirCriteriaHolder<V> {
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final CriteriaBuilder criteriaBuilder;
	
	@Getter(onMethod = @__({ @Nonnull }))
	private final Root<V> root;
	
	@Getter(AccessLevel.PROTECTED)
	private final Map<String, Join<?, ?>> aliases = new LinkedHashMap<>();
	
	@Getter(AccessLevel.PROTECTED)
	private final List<Predicate> predicates = new ArrayList<>();
	
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
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join implicitly
	 * joins with the root using the specified attribute.
	 *
	 * @param attributeName The name of the attribute (from the root object) to join on
	 * @param alias The alias to use for this join
	 * @param onGenerator A {@link Function} that takes a {@link From} object and returns the predicate
	 *            for the on clause
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias,
	        @Nonnull Function<From<?, ?>, Predicate> onGenerator) {
		return addJoin(getRoot(), attributeName, alias, JoinType.INNER, onGenerator);
	}
	
	/**
	 * Adds a join to the query managed by this {@link OpenmrsFhirCriteriaContext}. This join implicitly
	 * joins with the root using the specified attribute.
	 *
	 * @param attributeName The name of the attribute (from the root object) to join on
	 * @param alias The alias to use for this join
	 * @param joinType The {@link JoinType} representing the type of join, i.e., {@code INNER},
	 *            {@code LEFT}, {@code RIGHT}
	 * @param onGenerator A {@link Function} that takes a {@link From} object and returns the predicate
	 *            for the on clause
	 * @return Returns the new {@link Join} that has been added to the criteria context
	 */
	public Join<?, ?> addJoin(@Nonnull String attributeName, @Nonnull String alias, @Nonnull JoinType joinType,
	        @Nonnull Function<From<?, ?>, Predicate> onGenerator) {
		return addJoin(getRoot(), attributeName, alias, joinType, onGenerator);
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
	public Join<?, ?> addJoin(@Nonnull From<?, ?> from, @Nonnull String attributeName, @Nonnull String alias,
	        @Nonnull Function<From<?, ?>, Predicate> onGenerator) {
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
		return Optional.<Join<?, ?>> ofNullable(aliases.get(alias)).orElseGet(() -> {
			Join<?, ?> newJoin = from.join(attributeName, joinType);
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
	
	public BaseFhirCriteriaHolder<V> addPredicate(Predicate predicate) {
		predicates.add(predicate);
		return this;
	}
	
	public Optional<Join<?, ?>> getJoin(String alias) {
		return Optional.ofNullable(aliases.get(alias));
	}
	
	public Optional<Join<?, ?>> getJoin(From<?, ?> alias) {
		return Optional.ofNullable(aliases.get(alias.getAlias()));
	}
	
}
