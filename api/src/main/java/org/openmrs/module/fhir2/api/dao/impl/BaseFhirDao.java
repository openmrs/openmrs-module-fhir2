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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

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
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.DomainResource;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.Person;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.handler.RetireHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDao;
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
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao implements FhirDao<T> {
	
	@SuppressWarnings("UnstableApiUsage")
	protected final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};
	
	private final boolean isRetireable;
	
	private final boolean isVoidable;
	
	private final boolean isImmutable;
	
	@SuppressWarnings({ "UnstableApiUsage" })
	protected BaseFhirDao() {
		this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
		this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
		this.isImmutable = Order.class.isAssignableFrom(typeToken.getRawType())
		        || Obs.class.isAssignableFrom(typeToken.getRawType());
	}
	
	@Override
	@Transactional(readOnly = true)
	public T get(@Nonnull String uuid) {
		@SuppressWarnings({ "UnstableApiUsage" })
		OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext(typeToken.getRawType());
		
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
		@SuppressWarnings({ "UnstableApiUsage" })
		OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext(typeToken.getRawType());
		
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
	
	@SuppressWarnings("UnstableApiUsage")
	protected OpenmrsFhirCriteriaContext<T> getSearchResultCriteria(SearchParameterMap theParams) {
		OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext(typeToken.getRawType());
		
		if (isVoidable) {
			handleVoidable(criteriaContext);
		} else if (isRetireable) {
			handleRetireable(criteriaContext);
		}
		
		setupSearchParams(criteriaContext, theParams);
		
		return criteriaContext;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "UnstableApiUsage" })
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams) {
		OpenmrsFhirCriteriaContext<T> criteriaContext = getSearchResultCriteria(theParams);
		
		handleSort(criteriaContext, theParams.getSortSpec());
		
		String person = getIdPropertyName(Person.class);
		String encounter = getIdPropertyName(Encounter.class);
		String obs = getIdPropertyName(Obs.class);
		
		if (Person.class.isAssignableFrom(BaseOpenmrsObject.class)) {
			handleIdPropertyOrdering(criteriaContext, person);
		} else if (Encounter.class.isAssignableFrom(BaseOpenmrsObject.class)) {
			handleIdPropertyOrdering(criteriaContext, encounter);
		} else if (Obs.class.isAssignableFrom(BaseOpenmrsObject.class)) {
			handleIdPropertyOrdering(criteriaContext, obs);
		}
		
		criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery())
		        .setFirstResult(theParams.getFromIndex());
		if (theParams.getToIndex() != Integer.MAX_VALUE) {
			int maxResults = theParams.getToIndex() - theParams.getFromIndex();
			criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setMaxResults(maxResults);
		}
		
		List<T> results;
		if (hasDistinctResults()) {
			results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
		} else {
			EntityManager em = sessionFactory.getCurrentSession();
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<T> root = (Root<T>) criteriaQuery.from(typeToken.getRawType());
			
			List<Selection<?>> selectionList = new ArrayList<>();
			if (Person.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertySelection(selectionList, root, criteriaBuilder, person);
			} else if (Encounter.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertySelection(selectionList, root, criteriaBuilder, encounter);
			} else if (Obs.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertySelection(selectionList, root, criteriaBuilder, obs);
			}
			
			criteriaQuery.multiselect(selectionList);
			handleSort(criteriaContext, theParams.getSortSpec(), this::paramToProps).ifPresent(
			    orders -> orders.forEach(order -> selectionList.add(root.get(getPropertyName(order.getExpression())))));
			List<T> ids = new ArrayList<>();
			if (selectionList.size() > 1) {
				for (Object[] o : em.createQuery(criteriaQuery).getResultList()) {
					ids.add((T) o[0]);
				}
			} else {
				EntityManager manager = sessionFactory.getCurrentSession();
				CriteriaBuilder builder = manager.getCriteriaBuilder();
				CriteriaQuery<T> cq = (CriteriaQuery<T>) builder.createQuery(typeToken.getRawType());
				Root<T> rt = (Root<T>) cq.from(typeToken.getRawType());
				cq.select(rt);
				ids = manager.createQuery(cq).getResultList();
			}
			
			CriteriaQuery<T> idsCriteriaQuery = (CriteriaQuery<T>) criteriaBuilder.createQuery(typeToken.getRawType());
			Root<T> idsRoot = (Root<T>) idsCriteriaQuery.from(typeToken.getRawType());
			
			if (Person.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyInCondition(idsCriteriaQuery, idsRoot, ids, person);
			} else if (Encounter.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyInCondition(idsCriteriaQuery, idsRoot, ids, encounter);
			} else if (Obs.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyInCondition(idsCriteriaQuery, idsRoot, ids, obs);
			}
			
			results = em.createQuery(idsCriteriaQuery).getResultList();
			
			// Need to reapply ordering
			handleSort(criteriaContext, theParams.getSortSpec());
			
			if (Person.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyOrdering(criteriaBuilder, idsCriteriaQuery, idsRoot, person);
			} else if (Encounter.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyOrdering(criteriaBuilder, idsCriteriaQuery, idsRoot, encounter);
			} else if (Obs.class.isAssignableFrom(BaseOpenmrsObject.class)) {
				handleIdPropertyOrdering(criteriaBuilder, idsCriteriaQuery, idsRoot, obs);
			}
			
			results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
		}
		return results.stream().map(this::deproxyResult).collect(Collectors.toList());
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "UnstableApiUsage" })
	public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
		OpenmrsFhirCriteriaContext<T> criteriaContext = getSearchResultCriteria(theParams);
		applyExactTotal(criteriaContext, theParams);
		
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<T> root = (Root<T>) criteriaQuery.from(typeToken.getRawType());
		
		if (hasDistinctResults()) {
			criteriaQuery.select(criteriaBuilder.count(root));
		} else {
			//the id property differs across various openmrs entities
			if (Person.class.isAssignableFrom(typeToken.getRawType())) {
				criteriaQuery.select(criteriaBuilder.countDistinct(root.get("personId")));
			} else if (Encounter.class.isAssignableFrom(typeToken.getRawType())) {
				criteriaQuery.select(criteriaBuilder.countDistinct(root.get("encounterId")));
			} else if (Obs.class.isAssignableFrom(typeToken.getRawType())) {
				criteriaQuery.select(criteriaBuilder.countDistinct(root.get("obsId")));
			}
		}
		return em.createQuery(criteriaQuery).getSingleResult().intValue();
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
	
	protected void createAlias(OpenmrsFhirCriteriaContext<T> criteriaContext, String referencedEntity, String alias) {
		criteriaContext.getRoot().join(referencedEntity).alias(alias);
	}
	
	@Override
	protected <T> Optional<Predicate> handleLastUpdated(OpenmrsFhirCriteriaContext<T> criteriaContext,
	        DateRangeParam param) {
		if (isImmutable) {
			return handleLastUpdatedImmutable(criteriaContext, param);
		}
		
		return handleLastUpdatedMutable(criteriaContext, param);
	}
	
	protected <T> Optional<Predicate> handleLastUpdatedMutable(OpenmrsFhirCriteriaContext<T> criteriaContext,
	        DateRangeParam param) {
		// @formatter:off
		return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(handleDateRange(criteriaContext,"dateChanged", param), Optional.of(
                criteriaContext.getCriteriaBuilder().and(toCriteriaArray(Stream.of(Optional.of(criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("dateChanged"))), handleDateRange(criteriaContext,"dateCreated", param))))))));
		// @formatter:on
	}
	
	// Implementation of handleLastUpdated for "immutable" types, that is, those that cannot be changed
	protected <T> Optional<Predicate> handleLastUpdatedImmutable(OpenmrsFhirCriteriaContext<T> criteriaContext,
	        DateRangeParam param) {
		return handleDateRange(criteriaContext, "dateCreated", param);
	}
	
	/**
	 * This provides a default implementation for dealing with voidable objects. By default, voided
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 */
	protected void handleVoidable(OpenmrsFhirCriteriaContext<T> criteriaContext) {
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("voided"), true));
	}
	
	/**
	 * This provides a default implementation for dealing with retireable objects. By default, retired
	 * objects are excluded from searches, but not from get
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 */
	protected void handleRetireable(OpenmrsFhirCriteriaContext<T> criteriaContext) {
		criteriaContext
		        .addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"), true));
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
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 * @param theParams the parameters for this search
	 */
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<T> criteriaContext, SearchParameterMap theParams) {
		
	}
	
	@Override
	protected <V> Collection<javax.persistence.criteria.Order> paramToProps(OpenmrsFhirCriteriaContext<V> criteriaContext,
	        @Nonnull SortState sortState) {
		String param = sortState.getParameter();
		
		if (FhirConstants.SP_LAST_UPDATED.equalsIgnoreCase(param)) {
			if (isImmutable) {
				switch (sortState.getSortOrder()) {
					case ASC:
						return Collections.singletonList(
						    criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("dateCreated")));
					case DESC:
						return Collections.singletonList(
						    criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get("dateCreated")));
				}
			}
			
			switch (sortState.getSortOrder()) {
				case ASC:
					return Collections.singletonList(CoalescedOrder.asc("dateChanged", "dateCreated"));
				case DESC:
					return Collections.singletonList(CoalescedOrder.desc("dateChanged", "dateCreated"));
			}
		}
		
		return super.paramToProps(criteriaContext, sortState);
	}
	
	@Override
	protected <V> String paramToProp(OpenmrsFhirCriteriaContext<V> criteriaContext, @Nonnull String param) {
		if (DomainResource.SP_RES_ID.equals(param)) {
			return "uuid";
		}
		
		return super.paramToProp(criteriaContext, param);
	}
	
	@SuppressWarnings("unchecked")
	protected void applyExactTotal(OpenmrsFhirCriteriaContext<T> criteriaContext, SearchParameterMap theParams) {
		List<PropParam<?>> exactTotal = theParams.getParameters(EXACT_TOTAL_SEARCH_PARAMETER);
		if (!exactTotal.isEmpty()) {
			PropParam<Boolean> propParam = (PropParam<Boolean>) exactTotal.get(0);
			if (propParam.getParam()) {
				criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery())
				        .setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
			}
		} else {
			criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setHint(HINT_CACHEABLE,
			    "true");
			criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery())
			        .setHint("org.hibernate.cacheRegion", COUNT_QUERY_CACHE);
		}
	}
	
	/**
	 * Extracts the property name from the given JPA Criteria API expression.
	 *
	 * @param expression The JPA Criteria API expression, typically representing a property or
	 *            attribute.
	 * @return The property name extracted from the expression.
	 * @throws IllegalArgumentException If the expression is not of type {@code Path<?>}.
	 */
	public static String getPropertyName(Expression<?> expression) {
		if (expression instanceof Path<?>) {
			Path<?> path = (Path<?>) expression;
			return path.getJavaType().getSimpleName();
		} else {
			throw new IllegalArgumentException("Unsupported expression type: " + expression.getClass());
		}
	}
	
	/**
	 * Handles the ordering of the criteria based on the specified id property name.
	 *
	 * @param criteriaContext The criteria context containing the criteria builder and root.
	 * @param idPropertyName The name of the id property to be used for ordering.
	 */
	private void handleIdPropertyOrdering(OpenmrsFhirCriteriaContext<T> criteriaContext, String idPropertyName) {
		criteriaContext.getCriteriaQuery()
		        .orderBy(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get(idPropertyName)));
	}
	
	/**
	 * Handles the ordering of the criteria based on the specified id property name.
	 *
	 * @param criteriaBuilder The criteria builder to build expressions
	 * @param criteriaQuery The criteria query for the idPropertyName.
	 * @param idsRoot The root of the criteria query for the idPropertyName
	 * @param idPropertyName The name of the id property to be used for ordering.
	 */
	private void handleIdPropertyOrdering(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery, Root<T> idsRoot,
	        String idPropertyName) {
		criteriaQuery.orderBy(criteriaBuilder.asc(idsRoot.get(idPropertyName)));
	}
	
	/**
	 * Handles the selection list based on the specified id property name, adding both the property and
	 * its distinct count.
	 *
	 * @param selectionList The list of selections to be populated.
	 * @param root The root of the criteria query.
	 * @param criteriaBuilder The criteria builder to build selection expressions.
	 * @param idPropertyName The name of the id property to be used for selection.
	 */
	private void handleIdPropertySelection(List<Selection<?>> selectionList, Root<T> root, CriteriaBuilder criteriaBuilder,
	        String idPropertyName) {
		selectionList.add(root.get(idPropertyName));
		selectionList.add(criteriaBuilder.countDistinct(root.get(idPropertyName)));
	}
	
	/**
	 * Handles the "IN" condition in the criteria query based on the specified id property name and a
	 * list of ids.
	 *
	 * @param idsCriteriaQuery The criteria query for ids.
	 * @param idsRoot The root of the criteria query for ids.
	 * @param ids The list of ids to be used in the "IN" condition.
	 * @param idPropertyName The name of the id property to be used in the "IN" condition.
	 */
	private void handleIdPropertyInCondition(CriteriaQuery<T> idsCriteriaQuery, Root<T> idsRoot, List<T> ids,
	        String idPropertyName) {
		idsCriteriaQuery.where(idsRoot.get(idPropertyName).in(ids));
	}
	
	/**
	 * Determines the name of the id property for the given entity class. This method assumes a specific
	 * naming convention based on the entity type.
	 *
	 * @param openmrsEntityClass The openmrs class for which the id property name is requested.
	 * @return The name of the id property for the specified entity class.
	 * @throws IllegalArgumentException If the entity class is not supported or recognized.
	 */
	private String getIdPropertyName(Class<? extends BaseOpenmrsObject> openmrsEntityClass) {
		return Stream
		        .of(Pair.of(Person.class, "personId"), Pair.of(Encounter.class, "encounterId"), Pair.of(Obs.class, "obsId"))
		        .filter(pair -> pair.getLeft().isAssignableFrom(openmrsEntityClass)).findFirst().map(Pair::getRight)
		        .orElseThrow(() -> new IllegalArgumentException("Unsupported entity type: " + openmrsEntityClass.getName()));
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
}
