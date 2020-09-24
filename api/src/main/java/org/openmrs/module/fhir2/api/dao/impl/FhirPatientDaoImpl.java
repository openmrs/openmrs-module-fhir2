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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.sql.JoinType;
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
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					handleNames(criteria, entry.getValue());
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    p -> handleGender(p.getPropertyName(), (TokenAndListParam) p.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.IDENTIFIER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(identifier -> handleIdentifier(criteria, (TokenAndListParam) identifier.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange(dateRangeParam.getPropertyName(),
					    (DateRangeParam) dateRangeParam.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    b -> handleBoolean(b.getPropertyName(), (TokenAndListParam) b.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteria, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	protected void handleIdentifier(Criteria criteria, TokenAndListParam identifier) {
		if (identifier == null) {
			return;
		}
		
		criteria.createAlias("identifiers", "pi", JoinType.INNER_JOIN, eq("pi.voided", false));
		
		handleAndListParamBySystem(identifier, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(in("pi.identifier", tokensToList(tokens)));
			} else {
				if (lacksAlias(criteria, "pit")) {
					criteria.createAlias("pi.identifierType", "pit");
				}
				
				return Optional.of(and(eq("pit.name", system), in("pi.identifier", tokensToList(tokens))));
			}
		}).ifPresent(criteria::add);
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
}
