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

import static org.openmrs.module.fhir2.FhirConstants.COUNT_QUERY_CACHE;
import static org.openmrs.module.fhir2.FhirConstants.EXACT_TOTAL_SEARCH_PARAMETER;

import javax.annotation.Nonnull;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
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
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
@Slf4j
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao implements FhirDao<T> {
	
	@SuppressWarnings("UnstableApiUsage")
	protected final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};
	
	private final boolean isRetireable;
	
	private final boolean isVoidable;
	
	private final boolean isImmutable;
	
	@SuppressWarnings("UnstableApiUsage")
	protected BaseFhirDao() {
		this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
		this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
		this.isImmutable = Order.class.isAssignableFrom(typeToken.getRawType())
		        || Obs.class.isAssignableFrom(typeToken.getRawType());
	}
	
	@Override
	@Transactional(readOnly = true)
	public T get(@Nonnull String uuid) {
		@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
		OpenmrsFhirCriteriaContext<T, T> criteriaContext = createCriteriaContext((Class<T>) typeToken.getRawType());
		
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), uuid));
		
		// try-catch phrase is a workaround for https://github.com/jakartaee/persistence/issues/298
		try {
			return deproxyResult(
			    criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult());
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<T> get(@Nonnull Collection<String> uuids) {
		@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
		OpenmrsFhirCriteriaContext<T, T> criteriaContext = createCriteriaContext((Class<T>) typeToken.getRawType());
		
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		criteriaContext.addPredicate(criteriaContext.getRoot().get("uuid").in(uuids));
		
		if (isVoidable) {
			handleVoidable(criteriaContext);
		} else if (isRetireable) {
			handleRetireable(criteriaContext);
		}
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList().stream()
		        .filter(Objects::nonNull).map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	@Transactional
	public T createOrUpdate(@Nonnull T newEntry) {
		sessionFactory.getCurrentSession().saveOrUpdate(newEntry);
		
		return newEntry;
	}
	
	@Override
	@Transactional
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
	
	@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
	protected OpenmrsFhirCriteriaContext<T, T> getSearchResultCriteria(SearchParameterMap theParams) {
		return getSearchResultCriteria(createCriteriaContext((Class<T>) typeToken.getRawType()), theParams);
	}
	
	protected <U> OpenmrsFhirCriteriaContext<T, U> getSearchResultCriteria(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        SearchParameterMap theParams) {
		if (isVoidable) {
			handleVoidable(criteriaContext);
		} else if (isRetireable) {
			handleRetireable(criteriaContext);
		}
		
		setupSearchParams(criteriaContext, theParams);
		
		return criteriaContext;
	}
	
	@Override
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams) {
		List<T> results;
		
		if (hasDistinctResults()) {
			OpenmrsFhirCriteriaContext<T, T> criteriaContext = getSearchResultCriteria(theParams);
			String idProperty = getIdPropertyName(criteriaContext);
			
			handleSort(criteriaContext, theParams.getSortSpec());
			handleIdPropertyOrdering(criteriaContext, idProperty);
			
			CriteriaQuery<T> criteriaQuery = criteriaContext.finalizeQuery();
			criteriaQuery.select(criteriaContext.getRoot());
			
			TypedQuery<T> executableQuery = criteriaContext.getEntityManager().createQuery(criteriaQuery);
			
			executableQuery.setFirstResult(theParams.getFromIndex());
			if (theParams.getToIndex() != Integer.MAX_VALUE && theParams.getToIndex() >= 0) {
				int maxResults = theParams.getToIndex() - theParams.getFromIndex();
				if (maxResults >= 0) {
					executableQuery.setMaxResults(maxResults);
				}
			}
			
			results = executableQuery.getResultList();
		} else {
			@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
			OpenmrsFhirCriteriaContext<T, Integer> criteriaContext = getSearchResultCriteria(
			    createCriteriaContext((Class<T>) typeToken.getRawType(), Integer.class), theParams);
			
			String idProperty = getIdPropertyName(criteriaContext);
			
			CriteriaQuery<Integer> query = criteriaContext.finalizeIdQuery(idProperty);
			
			List<Integer> ids = criteriaContext.getEntityManager().createQuery(query).getResultList();
			
			// Use distinct ids from the original query to return entire objects
			@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
			OpenmrsFhirCriteriaContext<T, T> wrapperQuery = createCriteriaContext((Class<T>) typeToken.getRawType());
			
			handleSort(wrapperQuery, theParams.getSortSpec());
			handleIdPropertyOrdering(wrapperQuery, idProperty);
			
			wrapperQuery.getCriteriaQuery().select(wrapperQuery.getRoot());
			
			results = wrapperQuery.getEntityManager().createQuery(wrapperQuery.finalizeWrapperQuery(idProperty, ids))
			        .getResultList();
		}
		
		return results.stream().map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
		OpenmrsFhirCriteriaContext<T, Long> criteriaContext = getSearchResultCriteria(
		    createCriteriaContext((Class<T>) typeToken.getRawType(), Long.class), theParams);
		
		applyExactTotal(criteriaContext, theParams);
		
		if (hasDistinctResults()) {
			criteriaContext.getCriteriaQuery().select(criteriaContext.getCriteriaBuilder().count(criteriaContext.getRoot()));
		} else {
			criteriaContext.getCriteriaQuery().select(criteriaContext.getCriteriaBuilder()
			        .countDistinct(criteriaContext.getRoot().get(getIdPropertyName(criteriaContext))));
		}
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult().intValue();
	}
	
	/**
	 * Override to return false if the getSearchResults may return duplicate items that need to be
	 * removed from the results. Note that it has performance implications as it requires "select
	 * distinct" and 2 queries instead of 1 for getting the results.
	 *
	 * @return See the above explanation
	 */
	protected boolean hasDistinctResults() {
		return true;
	}
	
	protected void applyExactTotal(SearchParameterMap theParams, Criteria criteria) {
		List<PropParam<Boolean>> exactTotal = theParams.getParameters(EXACT_TOTAL_SEARCH_PARAMETER);
		if (!exactTotal.isEmpty()) {
			PropParam<Boolean> propParam = exactTotal.get(0);
			if (propParam.getParam()) {
				criteria.setCacheMode(CacheMode.REFRESH);
			}
		} else {
			criteria.setCacheable(true);
			criteria.setCacheRegion(COUNT_QUERY_CACHE);
		}
	}
	
	@Override
	protected <T, U> Optional<Predicate> handleLastUpdated(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param) {
		if (isImmutable) {
			return handleLastUpdatedImmutable(criteriaContext, param);
		}
		
		return handleLastUpdatedMutable(criteriaContext, param);
	}
	
	protected <V, U> Optional<Predicate> handleLastUpdatedMutable(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        DateRangeParam param) {
		// @formatter:off
		return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(handleDateRange(criteriaContext,"dateChanged", param), Optional.of(
                criteriaContext.getCriteriaBuilder().and(toCriteriaArray(Stream.of(Optional.of(criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("dateChanged"))), handleDateRange(criteriaContext,"dateCreated", param))))))));
		// @formatter:on
	}
	
	// Implementation of handleLastUpdated for "immutable" types, that is, those that cannot be changed
	protected <V, U> Optional<Predicate> handleLastUpdatedImmutable(
	        @Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, DateRangeParam param) {
		return handleDateRange(criteriaContext, "dateCreated", param);
	}
	
	/**
	 * This provides a default implementation for dealing with voidable objects. By default, voided
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 */
	protected <U> void handleVoidable(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext) {
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("voided"), false));
	}
	
	/**
	 * This provides a default implementation for dealing with retireable objects. By default, retired
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 */
	protected <U> void handleRetireable(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext) {
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"), false));
	}
	
	/**
	 * Voids the given object
	 *
	 * @param object the object implementing the Voidable interface
	 * @return the same object voided
	 */
	protected T voidObject(@Nonnull T object) {
		RequiredDataAdvice.recursivelyHandle(VoidHandler.class, object, "Voided via FHIR API");
		return object;
	}
	
	/**
	 * Retires the given object
	 *
	 * @param object the object implementing the Retireable interface
	 * @return the same object retired
	 */
	protected T retireObject(@Nonnull T object) {
		RequiredDataAdvice.recursivelyHandle(RetireHandler.class, object, "Retired via FHIR API");
		return object;
	}
	
	/**
	 * This is intended to be overridden by subclasses to implement any special handling they might
	 * require
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 * @param theParams the parameters for this search
	 */
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		
	}
	
	@Override
	protected <V, U> Collection<javax.persistence.criteria.Order> paramToProps(
	        @Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull SortState<V, U> sortState) {
		String param = sortState.getParameter();
		CriteriaBuilder cb = criteriaContext.getCriteriaBuilder();
		
		if (FhirConstants.SP_LAST_UPDATED.equalsIgnoreCase(param)) {
			if (isImmutable) {
				switch (sortState.getSortOrder()) {
					case ASC:
						return Collections.singletonList(cb.asc(criteriaContext.getRoot().get("dateCreated")));
					case DESC:
						return Collections.singletonList(cb.desc(criteriaContext.getRoot().get("dateCreated")));
				}
			}
			
			Expression<?> coalescedAttributes = cb.coalesce(criteriaContext.getRoot().get("dateChanged"),
			    criteriaContext.getRoot().get("dateCreated"));
			switch (sortState.getSortOrder()) {
				case ASC:
					return Collections.singletonList(cb.asc(coalescedAttributes));
				case DESC:
					return Collections.singletonList(cb.desc(coalescedAttributes));
			}
		}
		
		return super.paramToProps(criteriaContext, sortState);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		if (DomainResource.SP_RES_ID.equals(param)) {
			return criteriaContext.getRoot().get("uuid");
		}
		
		return super.paramToProp(criteriaContext, param);
	}
	
	protected <V, U> void applyExactTotal(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        SearchParameterMap theParams) {
		List<PropParam<Boolean>> exactTotal = theParams.getParameters(EXACT_TOTAL_SEARCH_PARAMETER);
		
		EntityManager manager = criteriaContext.getEntityManager();
		if (!exactTotal.isEmpty()) {
			PropParam<Boolean> propParam = exactTotal.get(0);
			if (propParam.getParam()) {
				manager.setProperty("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
			}
		} else {
			manager.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.USE);
			manager.setProperty("javax.persistence.cache.region", COUNT_QUERY_CACHE);
		}
	}
	
	@SuppressWarnings("UnstableApiUsage")
	protected <V, U> String getIdPropertyName(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext) {
		return getIdPropertyName(criteriaContext.getEntityManager(), typeToken.getRawType());
	}
	
	/**
	 * Handles the ordering of the criteria based on the specified id property name.
	 *
	 * @param criteriaContext The criteria context containing the criteria builder and root.
	 * @param idPropertyName The name of the id property to be used for ordering.
	 */
	protected <V, U> void handleIdPropertyOrdering(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        String idPropertyName) {
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get(idPropertyName)));
	}
	
	protected static <V> V deproxyObject(@Nonnull V object) {
		if (object instanceof HibernateProxy) {
			Hibernate.initialize(object);
			@SuppressWarnings("unchecked")
			V theResult = (V) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
			return theResult;
		}
		
		return object;
	}
	
	protected T deproxyResult(@Nonnull T result) {
		return deproxyObject(result);
	}
}
