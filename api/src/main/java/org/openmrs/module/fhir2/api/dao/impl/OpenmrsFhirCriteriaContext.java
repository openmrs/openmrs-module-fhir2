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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenmrsFhirCriteriaContext<T> {
	
	@Getter
	@NonNull
	private final EntityManager entityManager;
	
	@Getter
	@NonNull
	private final CriteriaBuilder criteriaBuilder;
	
	@Getter
	@NonNull
	private final CriteriaQuery<T> criteriaQuery;
	
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
		if (!aliases.containsKey(alias)) {
			Join<?, ?> join = from.join(attributeName, joinType);
			join.alias(alias);
			aliases.put(alias, join);
		}
		
		return aliases.get(alias);
	}
	
	public OpenmrsFhirCriteriaContext<T> addPredicate(Predicate predicate) {
		predicates.add(predicate);
		return this;
	}
	
	public OpenmrsFhirCriteriaContext<T> addOrder(Order order) {
		orders.add(order);
		return this;
	}
	
	public OpenmrsFhirCriteriaContext<T> addResults(T result) {
		results.add(result);
		return this;
	}
	
	public Optional<Join<?, ?>> getJoin(String alias) {
		return Optional.ofNullable(aliases.get(alias));
	}
	
	public boolean hasAlias(String alias) {
		return aliases.containsKey(alias);
	}
	
	public CriteriaQuery<T> finalizeQuery() {
		return criteriaQuery.where(predicates.toArray(new Predicate[0])).orderBy(orders);
	}
}
