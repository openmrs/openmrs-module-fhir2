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

import static org.hibernate.criterion.Restrictions.eq;

import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a base class for FHIR2 Dao objects providing default implementations for the
 * {@link FhirDao} interface. It extends {@link BaseDaoImpl} so that the criteria helpers used there
 * will be available to all subclasses. In general, Dao objects implementing this class will simply
 * need to provide implementation(s) for search functionality
 *
 * @param <T> the {@link OpenmrsObject} managed by this Dao
 */
@Transactional
public abstract class BaseFhirDaoImpl<T extends OpenmrsObject & Auditable> extends BaseDaoImpl implements FhirDao<T> {
	
	private final TypeToken<T> typeToken;
	
	@Autowired
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	protected BaseFhirDaoImpl() {
		typeToken = new TypeToken<T>() {
			
		};
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public T get(String uuid) {
		return (T) sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType()).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T createOrUpdate(T newEntry) {
		sessionFactory.getCurrentSession().saveOrUpdate(newEntry);
		return newEntry;
	}
	
	@Override
	public T delete(String uuid) {
		T existing = get(uuid);
		if (existing == null) {
			return null;
		}
		
		if (existing instanceof Voidable) {
			Voidable existingVoidable = (Voidable) existing;
			existingVoidable.setVoided(true);
			existingVoidable.setVoidReason("Voided via FHIR API");
		} else if (existing instanceof Retireable) {
			Retireable existingRetireable = (Retireable) existing;
			existingRetireable.setRetired(true);
			existingRetireable.setRetireReason("Retired via FHIR API");
		}
		
		sessionFactory.getCurrentSession().save(existing);
		
		return existing;
	}
	
	/**
	 * This method should be overridden by DAO implementations.
	 *
	 * @param theParams search parameters
	 * @return {@link org.hibernate.Criteria}
	 */
	@Override
	public Criteria search(SearchParameterMap theParams) {
		
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(getModelClazz().getClass());
		
		theParams.getAndParams(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER).forEach(encounterReference -> {
			handleEncounterReference("e", (ReferenceAndListParam) encounterReference.getParam())
			        .ifPresent(c -> criteria.createAlias("encounter", "e").add(c));
		});
		
		theParams.getAndParams(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER).forEach(patientReference -> {
			handlePatientReference(criteria, (ReferenceAndListParam) patientReference.getParam(), "person");
		});
		
		theParams.getAndParams(FhirConstants.CODED_SEARCH_HANDLER).forEach(code -> {
			handleCodedConcept(criteria, (TokenAndListParam) code.getParam());
		});
		
		theParams.getAndParams(FhirConstants.VALUE_CODED_SEARCH_HANDLER).forEach(valueCoded -> {
			handleValueCodedConcept(criteria, (TokenAndListParam) valueCoded.getParam());
		});
		
		theParams.getAndParams(FhirConstants.DATE_RANGE_SEARCH_HANDLER).forEach(dateRangeParam -> {
			handleDateRange(dateRangeParam.getPropertyName(), (DateRangeParam) dateRangeParam.getParam());
		});
		
		theParams.getReferenceParams(FhirConstants.HAS_MEMBER_SEARCH_HANDLER).forEach(hasMemberReference -> {
			handleHasMemberReference(criteria, hasMemberReference.getParam());
		});
		
		theParams.getAndParams(FhirConstants.QUANTITY_SEARCH_HANDLER).forEach(quantity -> {
			handleQuantity(quantity.getPropertyName(), (QuantityAndListParam) quantity.getParam());
		});
		
		theParams.getAndParams(FhirConstants.VALUE_STRING_SEARCH_HANDLER).forEach(string -> {
			handleValueStringParam(string.getPropertyName(), (StringAndListParam) string.getParam());
		});
		
		handleSort(criteria, theParams.getSortSpec());
		
		return criteria;
	}
	
	private void handleHasMemberReference(Criteria criteria, ReferenceParam hasMemberReference) {
		if (hasMemberReference != null) {
			criteria.createAlias("groupMembers", "gm");
			
			switch (hasMemberReference.getChain()) {
				case Observation.SP_CODE:
					TokenAndListParam code = new TokenAndListParam()
					        .addAnd(new TokenParam().setValue(hasMemberReference.getValue()));
					criteria.createAlias("gm.concept", "c");
					handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
					break;
				case "":
					criteria.add(eq("gm.uuid", hasMemberReference.getIdPart()));
					break;
			}
		}
	}
	
	private void handleValueStringParam(@NotNull String propertyName, StringAndListParam valueStringParam) {
		if (valueStringParam == null) {
			return;
		}
		handleAndListParam(valueStringParam, v -> propertyLike(propertyName, v.getValue()));
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("concept", "c");
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private void handleValueCodedConcept(Criteria criteria, TokenAndListParam valueConcept) {
		if (valueConcept != null) {
			criteria.createAlias("valueCoded", "c");
			handleCodeableConcept(criteria, valueConcept, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
}
