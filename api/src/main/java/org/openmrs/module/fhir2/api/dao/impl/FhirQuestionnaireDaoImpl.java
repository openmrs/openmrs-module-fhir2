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

import static org.hibernate.criterion.Restrictions.*;

import javax.annotation.Nonnull;
import javax.persistence.criteria.*;
import javax.persistence.criteria.CriteriaBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirQuestionnaireDaoImpl extends BaseFhirDao<Form> implements FhirQuestionnaireDao {
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Form getQuestionnaireById(@Nonnull Integer id) {
		return (Form) getSessionFactory().getCurrentSession().createCriteria(Form.class).add(eq("formId", id))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Form> getQuestionnairesByIds(@Nonnull Collection<Integer> ids) {
		return getSessionFactory().getCurrentSession().createCriteria(Form.class).add(in("id", ids)).list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Form> getSearchResults(@Nonnull SearchParameterMap theParams) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		// Create CriteriaBuilder
		CriteriaBuilder builder = session.getCriteriaBuilder();
		
		// Create CriteriaQuery for main query
		CriteriaQuery<Form> query = builder.createQuery(Form.class);
		Root<Form> formRoot = query.from(Form.class);
		formRoot.alias("f");
		
		// Create Subquery for FormResource
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<FormResource> resourceRoot = subquery.from(FormResource.class);
		resourceRoot.alias("fr");
		subquery.select(resourceRoot.get("formResourceId")); // Selecting formId to match in main query
		
		// Add predicates to the subquery
		List<Predicate> subqueryPredicates = new ArrayList<>();
		subqueryPredicates.add(builder.equal(resourceRoot.get("form").get("formId"), formRoot.get("formId")));
		subqueryPredicates.add(builder.equal(resourceRoot.get("name"), FhirConstants.FHIR_QUESTIONNAIRE_TYPE));
		subquery.where(builder.and(subqueryPredicates.toArray(new Predicate[0])));
		
		// Main query predicates
		List<Predicate> mainPredicates = new ArrayList<>();
		setupSearchParams(mainPredicates, builder, formRoot, theParams);
		mainPredicates.add(builder.equal(formRoot.get("retired"), false));
		mainPredicates.add(builder.exists(subquery));
		
		// Add predicates to the query
		query.select(formRoot).where(builder.and(mainPredicates.toArray(new Predicate[0])));
		
		List<Form> results = session.createQuery(query).getResultList();
		return results.stream().map(this::deproxyResult).collect(Collectors.toList());
	}
	
	protected void setupSearchParams(List<Predicate> predicates, CriteriaBuilder builder, Root<Form> root,
	        SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> {
						((StringAndListParam) param.getParam()).getValuesAsQueryTokens().stream()
						        .forEach(l -> l.getValuesAsQueryTokens().stream().forEach(v -> {
							        predicates.add(builder.equal(root.get("name"), v.getValue()));
						        }));
					});
					break;
			}
		});
	}
}
