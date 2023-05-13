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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.DomainResource;
import org.openmrs.Auditable;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.handler.RetireHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
		
		return deproxyResult(result);
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<T> get(@Nonnull Collection<String> uuids) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType());
		criteria.add(in("uuid", uuids));
		
		if (isVoidable) {
			handleVoidable(criteria);
		} else if (isRetireable) {
			handleRetireable(criteria);
		}
		
		List<T> results = criteria.list();
		
		return results.stream().filter(Objects::nonNull).map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	public T createOrUpdate(@Nonnull T newEntry) {
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
	
	private Criteria getSearchResultCriteria(SearchParameterMap theParams) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType());
		
		if (isVoidable) {
			handleVoidable(criteria);
		} else if (isRetireable) {
			handleRetireable(criteria);
		}
		
		setupSearchParams(criteria, theParams);
		
		return criteria;
	}
	
	/**
	 * Override to return false if the getSearchResults may return duplicate items that need to be
	 * removed from the results. Note that it has performance implications as it requires "select
	 * distinct" and 2 queries instead of 1 for getting the results.
	 * 
	 * @return
	 */
	public boolean hasDistinctResults() {
		return true;
	}
	
	@Override
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		Criteria criteria = getSearchResultCriteria(theParams);
		if (hasDistinctResults()) {
			return ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		} else {
			return ((Long) criteria.setProjection(Projections.countDistinct("id")).uniqueResult()).intValue();
		}
	}
	
	@Override
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams) {
		Criteria criteria = getSearchResultCriteria(theParams);
		
		handleSort(criteria, theParams.getSortSpec());
		criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
		
		criteria.setFirstResult(theParams.getFromIndex());
		if (theParams.getToIndex() != Integer.MAX_VALUE) {
			int maxResults = theParams.getToIndex() - theParams.getFromIndex();
			criteria.setMaxResults(maxResults);
		}
		
		List<T> results;
		if (hasDistinctResults()) {
			results = criteria.list();
		} else {
			ProjectionList projectionList = Projections.projectionList();
			projectionList.add(Projections.distinct(Projections.projectionList().add(Projections.property("id"))));
			// Sort parameters must be included in projections
			handleSort(criteria, theParams.getSortSpec(), this::paramToProps).ifPresent(orders -> orders.forEach(order -> {
				projectionList.add(Projections.property(order.getPropertyName()));
			}));
			criteria.setProjection(projectionList);
			List<Integer> ids = new ArrayList<>();
			if (projectionList.getLength() > 1) {
				for (Object[] o : ((List<Object[]>) criteria.list())) {
					ids.add((Integer) o[0]);
				}
			} else {
				ids = criteria.list();
			}
			
			// Use distinct ids from the original query to return entire objects
			Criteria idsCriteria = sessionFactory.getCurrentSession().createCriteria(typeToken.getRawType())
			        .add(in("id", ids));
			// Need to reapply ordering
			handleSort(idsCriteria, theParams.getSortSpec());
			idsCriteria.addOrder(org.hibernate.criterion.Order.asc("id"));
			
			results = idsCriteria.list();
		}
		return results.stream().map(this::deproxyResult).collect(Collectors.toList());
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
		RequiredDataAdvice.recursivelyHandle(VoidHandler.class, object, "Voided via FHIR API");
		return object;
	}
	
	/**
	 * Retires the given object
	 *
	 * @param object the object implementing the Retireable interface
	 * @return the same object retired
	 */
	protected T retireObject(T object) {
		RequiredDataAdvice.recursivelyHandle(RetireHandler.class, object, "Retired via FHIR API");
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
	
	protected static <V> V deproxyObject(V object) {
		if (object instanceof HibernateProxy) {
			Hibernate.initialize(object);
			@SuppressWarnings("unchecked")
			V theResult = (V) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
			return theResult;
		}
		
		return object;
	}
	
	protected T deproxyResult(T result) {
		return deproxyObject(result);
	}
	
	protected Criteria createAlias(Criteria criteria, String referencedEntity, String alias) {
		return criteria.createAlias(referencedEntity, alias);
	}
}
