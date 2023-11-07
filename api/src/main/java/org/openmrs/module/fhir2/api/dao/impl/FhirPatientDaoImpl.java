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

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.or;
import static org.hl7.fhir.r4.model.Patient.SP_DEATH_DATE;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPatientDaoImpl extends BasePersonDao<Patient> implements FhirPatientDao {
	
	@Override
	public Patient getPatientById(@Nonnull Integer id) {
		return (Patient) getSessionFactory().getCurrentSession().createCriteria(Patient.class).add(eq("patientId", id))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Patient> getPatientsByIds(@Nonnull Collection<Integer> ids) {
		return getSessionFactory().getCurrentSession().createCriteria(Patient.class).add(in("id", ids)).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid) {
		List<PatientIdentifierType> identifierTypes = (List<PatientIdentifierType>) getSessionFactory().getCurrentSession()
		        .createCriteria(PatientIdentifierType.class)
		        .add(or(and(eq("name", name), eq("retired", false)), eq("uuid", uuid))).list();
		
		if (identifierTypes.isEmpty()) {
			return null;
		} else {
			// favour uuid if one was supplied
			if (uuid != null) {
				try {
					return identifierTypes.stream().filter((idType) -> uuid.equals(idType.getUuid())).findFirst()
					        .orElse(identifierTypes.get(0));
				}
				catch (NoSuchElementException ignored) {}
			}
			
			return identifierTypes.get(0);
		}
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.QUERY_SEARCH_HANDLER:
					entry.getValue().forEach(query -> handlePatientQuery(criteriaBuilder, (StringAndListParam) query.getParam()));
					break;
				case FhirConstants.NAME_SEARCH_HANDLER:
					handleNames(criteriaBuilder, entry.getValue());
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    p -> handleGender(p.getPropertyName(), (TokenAndListParam) p.getParam()).ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(identifier -> handleIdentifier(criteriaBuilder, (TokenAndListParam) identifier.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    b -> handleBoolean(b.getPropertyName(), (TokenAndListParam) b.getParam()).ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaBuilder, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
	}
	
	private void handlePatientQuery(CriteriaBuilder criteriaBuilder, @Nonnull StringAndListParam query) {
		if (query == null) {
			return;
		}
		
		if (lacksAlias(criteriaBuilder, "pn")) {
			root.join("names").alias("pn");
		}
		
		if (lacksAlias(criteriaBuilder, "pi")) {
			root.join("identifiers").alias("pi");
		}
		
		handleAndListParam(query, q -> {
			List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
			
			for (String token : StringUtils.split(q.getValueNotNull(), " \t,")) {
				StringParam param = new StringParam(token).setContains(q.isContains()).setExact(q.isExact());
				criterionList.add(propertyLike("pn.givenName", param).map(c -> criteriaBuilder.and(c, criteriaBuilder.equal(root.get("pn.voided"), false))));
				criterionList.add(propertyLike("pn.middleName", param).map(c -> criteriaBuilder.and(c, criteriaBuilder.equal(root.get("pn.voided"), false))));
				criterionList.add(propertyLike("pn.familyName", param).map(c -> criteriaBuilder.and(c, criteriaBuilder.equal(root.get("pn.voided"), false))));
			}
			
			criterionList.add(propertyLike("pi.identifier",
			    new StringParam(q.getValueNotNull()).setContains(q.isContains()).setExact(q.isExact()))
			            .map(c -> criteriaBuilder.and(c, criteriaBuilder.equal(root.get("pi.voided"), false))));
			
			return Optional.of(criteriaBuilder.or(toCriteriaArray(criterionList)));
		}).ifPresent(criteriaBuilder::and);
	}
	
	protected void handleIdentifier(CriteriaBuilder criteriaBuilder, TokenAndListParam identifier) {
		if (identifier == null) {
			return;
		}
		
		root.join("identifiers", javax.persistence.criteria.JoinType.INNER).alias("pi");
		criteriaBuilder.equal(root.get("pi.voided"), false);
		
		handleAndListParamBySystem(identifier, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(criteriaBuilder.in(root.get("pi.identifier")).value(tokensToList(tokens)));
			} else {
				if (lacksAlias(criteriaBuilder, "pit")) {
					root.join("pi.identifierType", javax.persistence.criteria.JoinType.INNER).alias("pit");
					criteriaBuilder.equal(root.get("pit.retired"), false);
				}

				return Optional.of(criteriaBuilder.and(criteriaBuilder.equal(root.get("pit.name"), system),
						criteriaBuilder.in(root.get("pi.identifier")).value(tokensToList(tokens))));
			}
		}).ifPresent(criteriaBuilder::and);
	}
	
	@Override
	protected String getSqlAlias() {
		return "this_1_";
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		if (SP_DEATH_DATE.equalsIgnoreCase(param)) {
			return "deathDate";
		}
		
		return super.paramToProp(param);
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
}
