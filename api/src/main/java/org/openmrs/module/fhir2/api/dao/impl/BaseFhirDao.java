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
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyIn;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.ValidationException;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a base class for FHIR2 Dao objects providing default implementations for the
 * {@link FhirDao} interface. It extends {@link BaseDao} so that the criteria helpers used there
 * will be available to all subclasses. In general, Dao objects implementing this class will simply
 * need to provide implementation(s) for search functionality
 *
 * @param <T> the {@link OpenmrsObject} managed by this Dao
 */
@Transactional
@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao implements FhirDao<T> {
	
	protected final TypeToken<T> typeToken;
	
	private final boolean isRetireable;
	
	private final boolean isVoidable;
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	protected BaseFhirDao() {
		typeToken = new TypeToken<T>(getClass()) {
			
		};
		
		this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
		this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public T get(String uuid) {
		T result = (T) sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType()).add(eq("uuid", uuid))
		        .uniqueResult();
		
		if (result == null) {
			return null;
		}
		
		if (isVoidable || isRetireable) {
			if (isVoided(result) || isRetired(result)) {
				throw new ResourceGoneException(uuid);
			}
		}
		
		return deproxyObject(result);
	}
	
	@Override
	public T createOrUpdate(T newEntry) {
		if (newEntry.getUuid() == null) {
			newEntry.setUuid(UUID.randomUUID().toString());
		}
		
		// TODO Improve these messages
		try {
			ValidateUtil.validate(newEntry);
		}
		catch (ValidationException e) {
			throw new UnprocessableEntityException(e.getMessage(), e);
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(newEntry);
		
		return newEntry;
	}
	
	@Override
	public T delete(String uuid) {
		T existing = get(uuid);
		if (existing == null) {
			return null;
		}
		
		if (isVoidable) {
			existing = voidObject(existing);
		} else if (isRetireable) {
			existing = retireObject(existing);
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(existing);
		
		return existing;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getSearchResultUuids(SearchParameterMap theParams) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType());
		
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(typeToken.getRawType());
		Criteria detachedExecutableCriteria = detachedCriteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		
		if (isVoidable) {
			handleVoidable(detachedExecutableCriteria);
		} else if (isRetireable) {
			handleRetireable(detachedExecutableCriteria);
		}
		
		setupSearchParams(detachedExecutableCriteria, theParams);
		handleSort(detachedExecutableCriteria, theParams.getSortSpec());
		
		detachedCriteria.setProjection(Projections.property("uuid"));
		
		criteria.add(propertyIn("uuid", detachedCriteria));
		criteria.setProjection(Projections.groupProperty("uuid"));
		
		return criteria.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult) {
		List<String> selectedResources = matchingResourceUuids.subList(firstResult, lastResult);
		
		List<T> results = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType())
		        .add(in("uuid", selectedResources)).list();
		
		results.sort(Comparator.comparingInt(r -> selectedResources.indexOf(r.getUuid())));
		
		return results.stream().map(this::deproxyObject).collect(Collectors.toList());
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
		// @formatter:off
		return Optional.of(or(toCriteriaArray(handleDateRange("dateChanged", param), Optional.of(
		    and(toCriteriaArray(Stream.of(Optional.of(isNull("dateChanged")), handleDateRange("dateCreated", param))))))));
		// @formatter:on
	}
	
	// Implementation of handleLastUpdated for "immutable" types, that is, those that cannot be changed
	protected Optional<Criterion> handleLastUpdatedImmutable(DateRangeParam param) {
		return handleDateRange("dateCreated", param);
	}
	
	/**
	 * This provides a default implementation for dealing with voidable objects. By default, voided
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteria the criteria object representing the current search
	 */
	protected void handleVoidable(Criteria criteria) {
		criteria.add(eq("voided", false));
	}
	
	/**
	 * This provides a default implementation for dealing with retireable objects. By default, retired
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteria the criteria object representing the current search
	 */
	protected void handleRetireable(Criteria criteria) {
		criteria.add(eq("retired", false));
	}
	
	/**
	 * Determines whether the object is voided
	 *
	 * @param object an object implementing the Voidable interface
	 * @return true if the object is voided, false otherwise
	 */
	protected boolean isVoided(T object) {
		return object instanceof Voidable && ((Voidable) object).getVoided();
	}
	
	/**
	 * Determines whether the object is retired
	 *
	 * @param object an object implementing the Retireable interface
	 * @return true if the object is retired, false otherwise
	 */
	protected boolean isRetired(T object) {
		return object instanceof Retireable && ((Retireable) object).getRetired();
	}
	
	/**
	 * Voids the given object
	 *
	 * @param object the object implementing the Voidable interface
	 * @return the same object voided
	 */
	protected T voidObject(T object) {
		Voidable v = (Voidable) object;
		v.setVoided(true);
		v.setVoidReason("Voided via FHIR API");
		return object;
	}
	
	/**
	 * Retires the given object
	 *
	 * @param object the object implementing the Retireable interface
	 * @return the same object retired
	 */
	protected T retireObject(T object) {
		Retireable r = (Retireable) object;
		r.setRetired(true);
		r.setRetireReason("Retired via FHIR API");
		return object;
	}
	
	/**
	 * This is intended to be overridden by subclasses to implement any special handling they might
	 * require
	 *
	 * @param criteria the criteria object representing this search
	 * @param theParams the parameters for this search
	 */
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		
	}
	
	protected T deproxyObject(T result) {
		if (result instanceof HibernateProxy) {
			Hibernate.initialize(result);
			@SuppressWarnings("unchecked")
			T theResult = (T) ((HibernateProxy) result).getHibernateLazyInitializer().getImplementation();
			return theResult;
		}
		
		return result;
	}
}
