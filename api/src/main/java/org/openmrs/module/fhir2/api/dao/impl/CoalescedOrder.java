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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * Provides Hibernate Criteria API ordering using the SQL COALESCE() function to merge multiple
 * properties
 */
public class CoalescedOrder extends Order {
	
	private final String firstProperty;
	
	private final String secondProperty;
	
	protected CoalescedOrder(String firstProperty, String secondProperty, boolean ascending) {
		super(firstProperty, ascending);
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
	}
	
	public static Order asc(String firstPropertyName, String secondPropertyName) {
		return new CoalescedOrder(firstPropertyName, secondPropertyName, true);
	}
	
	public static Order desc(String firstPropertyName, String secondPropertyName) {
		return new CoalescedOrder(firstPropertyName, secondPropertyName, false);
	}
	
	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
		final Type type = criteriaQuery.getTypeUsingProjection(criteria, firstProperty);
		final SessionFactoryImplementor factory = criteriaQuery.getFactory();
		
		final SQLFunction coalesceFunction = factory.getDialect().getFunctions().get("coalesce");
		
		if (coalesceFunction == null) {
			throw new IllegalStateException("Cannot use coalesced ordering on a database that does not support it");
		}
		
		final String[] firstPropertyColumns = criteriaQuery.getColumnsUsingProjection(criteria, firstProperty);
		final String[] secondPropertyColumns = criteriaQuery.getColumnsUsingProjection(criteria, secondProperty);
		
		final List<String> properties = new ArrayList<>(firstPropertyColumns.length + secondPropertyColumns.length);
		Collections.addAll(properties, firstPropertyColumns);
		Collections.addAll(properties, secondPropertyColumns);
		
		return factory.getDialect().renderOrderByElement(coalesceFunction.render(type, properties, factory), null,
		    isAscending() ? "asc" : "desc", factory.getSettings().getDefaultNullPrecedence());
	}
	
	@Override
	public String toString() {
		return "coalesce(" + firstProperty + ", " + secondProperty + ") " + (isAscending() ? "asc" : "desc");
	}
}
