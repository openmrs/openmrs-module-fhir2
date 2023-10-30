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

import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.openmrs.module.fhir2.FhirConstants.COUNT_QUERY_CACHE;
import static org.openmrs.module.fhir2.FhirConstants.EXACT_TOTAL_SEARCH_PARAMETER;

import javax.annotation.Nonnull;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
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
import org.openmrs.module.fhir2.api.search.param.PropParam;
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
	protected SessionFactory sessionFactory;
	
	@SuppressWarnings({ "UnstableApiUsage"})
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
	@SuppressWarnings("unchecked")
	public T get(@Nonnull String uuid) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) cb.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		cq.select(rt).where(cb.equal(rt.get("uuid"), uuid));
		TypedQuery<T> result = em.createQuery(cq);
		
		//try-catch phrase is a workaround for https://github.com/jakartaee/persistence/issues/298
		try {
			return deproxyResult(result.getSingleResult());
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<T> get(@Nonnull Collection<String> uuids) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) cb.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		cq.select(rt).where(cb.in(rt.in(uuids)));
		
		if (isVoidable) {
			handleVoidable(cb);
		} else if (isRetireable) {
			handleRetireable(cb);
		}
		
		TypedQuery<T> tq = em.createQuery(cq);
		List<T> results = tq.getResultList();
		
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
	
	@SuppressWarnings("unchecked")
	private CriteriaBuilder getSearchResultCriteria(SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		if (isVoidable) {
			handleVoidable(cb);
		} else if (isRetireable) {
			handleRetireable(cb);
		}
		
		setupSearchParams(cb, theParams);
		
		return cb;
	}
	
	/**
	 * Override to return false if the getSearchResults may return duplicate items that need to be
	 * removed from the results. Note that it has performance implications as it requires "select
	 * distinct" and 2 queries instead of 1 for getting the results.
	 * 
	 * @return See the above explanation
	 */
	public boolean hasDistinctResults() {
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		EntityManager manager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<T> root = (Root<T>) criteriaQuery.from(typeToken.getRawType());
		
		criteriaBuilder = getSearchResultCriteria(theParams);
		
		applyExactTotal(theParams, criteriaBuilder);
		
		if (hasDistinctResults()) {
			criteriaQuery.select(criteriaBuilder.count(root));
			TypedQuery<Long> query = manager.createQuery(criteriaQuery);
			return query.getSingleResult().intValue();
		} else {
			criteriaQuery.select(criteriaBuilder.countDistinct(root.get("id")));
			TypedQuery<Long> query = manager.createQuery(criteriaQuery);
			return query.getSingleResult().intValue();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void applyExactTotal(SearchParameterMap theParams, CriteriaBuilder criteriaBuilder) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaQuery<T> criteriaQuery = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		
		List<PropParam<?>> exactTotal = theParams.getParameters(EXACT_TOTAL_SEARCH_PARAMETER);
		if (!exactTotal.isEmpty()) {
			PropParam<Boolean> propParam = (PropParam<Boolean>) exactTotal.get(0);
			if (propParam.getParam()) {
				em.createQuery(criteriaQuery).setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
			}
		} else {
			em.createQuery(criteriaQuery).setHint(HINT_CACHEABLE, "true");
			em.createQuery(criteriaQuery).setHint("org.hibernate.cacheRegion", COUNT_QUERY_CACHE);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) cb.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		cb = getSearchResultCriteria(theParams);
		
		handleSort(cb, theParams.getSortSpec());
		cq.orderBy(cb.asc(rt.get("id")));
		
		em.createQuery(cq).setFirstResult(theParams.getFromIndex());
		if (theParams.getToIndex() != Integer.MAX_VALUE) {
			int maxResults = theParams.getToIndex() - theParams.getFromIndex();
			em.createQuery(cq).setMaxResults(maxResults);
		}
		
		List<T> results;
		if (hasDistinctResults()) {
			results = em.createQuery(cq).getResultList();
		} else {
			EntityManager projectionManager = sessionFactory.getCurrentSession();
			CriteriaBuilder projectionCriteriaBuilder = projectionManager.getCriteriaBuilder();
			CriteriaQuery<Integer> projectionCriteriaQuery = projectionCriteriaBuilder.createQuery(Integer.class);
			
			Root<Integer> projectionRoot = projectionCriteriaQuery.from(Integer.class);
			Subquery<Long> subquery = projectionCriteriaQuery.subquery(Long.class);
			
			subquery.select(projectionCriteriaBuilder.countDistinct(projectionRoot.get("id")));
			
			projectionCriteriaQuery.select(projectionRoot)
			        .where(projectionCriteriaBuilder.in(projectionRoot.get("id")).value(subquery));
			TypedQuery<Integer> projectionTypedQuery = projectionManager.createQuery(projectionCriteriaQuery);
			
			//TODO: gonna come back to it later
			//			handleSort(projectionCriteriaBuilder, theParams.getSortSpec(), this::paramToProps).ifPresent(
			//					orders -> orders.forEach(order -> projectionList.add(Projections.property(order.getPropertyName()))));
			//			criteria.setProjection(projectionList);
			//			List<Integer> ids = new ArrayList<>();
			//			if (projectionList.getLength() > 1) {
			//				for (Object[] o : ((List<Object[]>) criteria.list())) {
			//					ids.add((Integer) o[0]);
			//				}
			//			} else {
			//				ids = criteria.list();
			//			}
			
			// Use distinct ids from the original query to return entire objects
			EntityManager idsManager = sessionFactory.getCurrentSession();
			CriteriaBuilder idsCriteriaBuilder = idsManager.getCriteriaBuilder();
			CriteriaQuery<T> idsCriteriaQuery = (CriteriaQuery<T>) idsCriteriaBuilder.createQuery(typeToken.getRawType());
			Root<T> idsRoot = (Root<T>) idsCriteriaQuery.from(typeToken.getRawType());
			TypedQuery<T> idsTypedQuery = idsManager.createQuery(idsCriteriaQuery);
			
			idsCriteriaQuery.select(idsRoot).where(idsCriteriaBuilder.in(idsRoot.get("id")));
			// Need to reapply ordering
			handleSort(idsCriteriaBuilder, theParams.getSortSpec());
			idsCriteriaQuery.orderBy(idsCriteriaBuilder.asc(idsRoot.get("id")));
			
			results = idsTypedQuery.getResultList();
		}
		return results.stream().map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	protected Optional<Predicate> handleLastUpdated(DateRangeParam param) {
		if (isImmutable) {
			return handleLastUpdatedImmutable(param);
		}
		
		return handleLastUpdatedMutable(param);
	}
	
	@SuppressWarnings("unchecked")
	protected Optional<Predicate> handleLastUpdatedMutable(DateRangeParam param) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) cb.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		// @formatter:off
		return Optional.of(cb.or(toCriteriaArray(handleDateRange("dateChanged", param), Optional.of(
		    cb.and(toCriteriaArray(Stream.of(Optional.of(cb.isNull(rt.get("dateChanged"))), handleDateRange("dateCreated", param))))))));
		// @formatter:on
	}
	
	// Implementation of handleLastUpdated for "immutable" types, that is, those that cannot be changed
	protected Optional<Predicate> handleLastUpdatedImmutable(DateRangeParam param) {
		return handleDateRange("dateCreated", param);
	}
	
	/**
	 * This provides a default implementation for dealing with voidable objects. By default, voided
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteriaBuilder The JPA CriteriaBuilder to create predicates.
	 */
	@SuppressWarnings("unchecked")
	protected void handleVoidable(CriteriaBuilder criteriaBuilder) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		criteriaBuilder.and(criteriaBuilder.equal(rt.get("voided"), false));
	}
	
	/**
	 * This provides a default implementation for dealing with retireable objects. By default, retired objects are excluded
	 * from searches, but not from get
	 */
	@SuppressWarnings("unchecked")
	protected void handleRetireable(CriteriaBuilder criteriaBuilder) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		criteriaBuilder.and(criteriaBuilder.equal(rt.get("retired"), false));
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
	 * @param criteriaBuilder the criteria object representing this search
	 * @param theParams the parameters for this search
	 */
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<javax.persistence.criteria.Order> paramToProps(@Nonnull SortState sortState) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		String param = sortState.getParameter();
		
		if (FhirConstants.SP_LAST_UPDATED.equalsIgnoreCase(param)) {
			if (isImmutable) {
				switch (sortState.getSortOrder()) {
					case ASC:
						return Collections.singletonList(criteriaBuilder.asc(rt.get("dateCreated")));
					case DESC:
						return Collections.singletonList(criteriaBuilder.desc(rt.get("dateCreated")));
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
	
	@SuppressWarnings("unchecked")
	protected void createAlias(CriteriaBuilder criteriaBuilder, String referencedEntity, String alias) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
		Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
		
		rt.join(referencedEntity).alias(alias);
	}
}
