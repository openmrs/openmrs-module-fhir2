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

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.DomainResource;
import org.openmrs.Auditable;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.util.FhirUtils;
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
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao implements FhirDao<T> {
	
	@SuppressWarnings("UnstableApiUsage")
	protected final TypeToken<T> typeToken;
	
	private final boolean isRetireable;
	
	private final boolean isVoidable;
	
	private final boolean isImmutable;
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@SuppressWarnings("UnstableApiUsage")
	protected BaseFhirDao() {
		// @formatter:off
		typeToken = new TypeToken<T>(getClass()) {};
		// @formatter:on
		
		this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
		this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
		this.isImmutable = Order.class.isAssignableFrom(typeToken.getRawType())
		        || Obs.class.isAssignableFrom(typeToken.getRawType());
	}
	
	@Override
	@Transactional(readOnly = true)
	public T get(@Nonnull String uuid) {
		@SuppressWarnings("unchecked")
		T result = (T) sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType()).add(eq("uuid", uuid))
		        .uniqueResult();
		
		if (result == null) {
			return null;
		}
		
		return deproxyObject(result);
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<T> get(Collection<String> uuids) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType());
		criteria.add(in("uuid", uuids));
		
		if (isVoidable) {
			handleVoidable(criteria);
		} else if (isRetireable) {
			handleRetireable(criteria);
		}
		
		List<T> results = criteria.list();
		
		return results.stream().filter(Objects::nonNull).map(this::deproxyObject).collect(Collectors.toList());
	}
	
	@Override
	public T createOrUpdate(@Nonnull T newEntry) {
		if (newEntry.getUuid() == null) {
			newEntry.setUuid(FhirUtils.newUuid());
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(newEntry);
		
		return newEntry;
	}
	
	@Override
	public T delete(@Nonnull String uuid) {
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
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType());
		
		if (isVoidable) {
			handleVoidable(criteria);
		} else if (isRetireable) {
			handleRetireable(criteria);
		}
		
		setupSearchParams(criteria, theParams);
		handleSort(criteria, theParams.getSortSpec());
		
		criteria.setProjection(Projections.property("uuid"));
		
		@SuppressWarnings("unchecked")
		List<String> results = criteria.list();
		
		return results.stream().distinct().collect(Collectors.toList());
	}
	
	@Override
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> matchingResourceUuids,
	        int firstResult, int lastResult) {
		List<String> selectedResources = matchingResourceUuids.subList(firstResult, lastResult);
		
		@SuppressWarnings("unchecked")
		List<T> results = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType())
		        .add(in("uuid", selectedResources)).list();
		
		results.sort(Comparator.comparingInt(r -> selectedResources.indexOf(r.getUuid())));
		
		return results.stream().map(this::deproxyObject).collect(Collectors.toList());
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
		if (isImmutable) {
			return handleLastUpdatedImmutable(param);
		}
		
		return handleLastUpdatedMutable(param);
	}
	
	protected Optional<Criterion> handleLastUpdatedMutable(DateRangeParam param) {
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
	
	@Override
	protected Collection<org.hibernate.criterion.Order> paramToProps(@Nonnull SortState sortState) {
		String param = sortState.getParameter();
		
		if (FhirConstants.SP_LAST_UPDATED.equalsIgnoreCase(param)) {
			if (isImmutable) {
				switch (sortState.getSortOrder()) {
					case ASC:
						return Collections.singletonList(org.hibernate.criterion.Order.asc("dateCreated"));
					case DESC:
						return Collections.singletonList(org.hibernate.criterion.Order.desc("dateCreated"));
				}
			}
			
			switch (sortState.getSortOrder()) {
				case ASC:
					return Collections.singletonList(CoalescedOrder.asc("dateChanged", "dateCreated"));
				case DESC:
					return Collections.singletonList(CoalescedOrder.desc("dateChanged", "dateCreated"));
			}
		}
		
		return super.paramToProps(sortState);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		if (DomainResource.SP_RES_ID.equals(param)) {
			return "uuid";
		}
		
		return super.paramToProp(param);
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
