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

import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.module.fhir2.api.dao.FhirDao;
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
		typeToken = new TypeToken<T>(getClass()) {
			
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
}
