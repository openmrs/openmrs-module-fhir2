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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenmrsFhirCriteriaSubquery<Q> {
	
	@Getter
	@NonNull
	private final CriteriaBuilder criteriaBuilder;
	
	@Getter
	@NonNull
	Subquery<Q> subquery;
	
	@Getter
	@NonNull
	Root<Q> root;
	
	private final List<Predicate> predicates = new ArrayList<>();
	
	public OpenmrsFhirCriteriaSubquery<Q> addPredicate(Predicate predicate) {
		predicates.add(predicate);
		return this;
	}
	
	public Subquery<Q> finalizeQuery() {
		return subquery.where(predicates.toArray(new Predicate[0]));
	}
}
