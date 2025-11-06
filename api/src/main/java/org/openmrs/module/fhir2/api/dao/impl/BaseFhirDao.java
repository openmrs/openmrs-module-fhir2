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

import static org.openmrs.module.fhir2.FhirConstants.COMMON_SEARCH_HANDLER;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import org.openmrs.module.fhir2.api.dao.internals.FhirSearchQueryHelper;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.dao.internals.SortState;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a base class for FHIR2 Dao objects providing default implementations for the
 * {@link FhirDao} interface. It includes a {@link FhirSearchQueryHelper} so that the criteria
 * helpers used there will be available to all subclasses.
 * <p/>
 * In general, objects extending this class will simply need to provide implementation(s) for search
 * functionality, i.e., {@link #setupSearchParams(OpenmrsFhirCriteriaContext, SearchParameterMap)},
 * and {@link #paramToProp(OpenmrsFhirCriteriaContext, String)}. See those functions for details.
 *
 * @param <T> the {@link Auditable} {@link OpenmrsObject} managed by this Dao
 */
@Transactional
@Slf4j
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao implements FhirDao<T> {
	
	@SuppressWarnings("UnstableApiUsage")
	protected final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = { @Autowired })
	private FhirSearchQueryHelper searchQueryHelper;
	
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
	@Transactional
	public T createOrUpdate(@Nonnull T object) {
		getSessionFactory().getCurrentSession().saveOrUpdate(object);
		
		return object;
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
		
		getSessionFactory().getCurrentSession().saveOrUpdate(existing);
		
		return existing;
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
	public List<T> getSearchResults(@Nonnull SearchParameterMap theParams) {
		List<T> results;
		
		if (hasDistinctResults()) {
			OpenmrsFhirCriteriaContext<T, T> criteriaContext = getSearchResultCriteria(theParams);
			String idProperty = getIdPropertyName(criteriaContext.getEntityManager());
			
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
			// For non-distinct results, use a two-query approach:
			// 1. Get sorted, paginated IDs with necessary join conditions
			// 2. Fetch full objects using those IDs
			
			@SuppressWarnings({ "UnstableApiUsage", "unchecked" })
			OpenmrsFhirCriteriaContext<T, Object[]> criteriaContext = getSearchResultCriteria(
			    createCriteriaContext((Class<T>) typeToken.getRawType(), Object[].class), theParams);
			
			String idProperty = getIdPropertyName(criteriaContext.getEntityManager());
			
			// Apply sorting to the ID query so we can paginate correctly
			handleSort(criteriaContext, theParams.getSortSpec());
			handleIdPropertyOrdering(criteriaContext, idProperty);
			
			CriteriaQuery<Object[]> query = criteriaContext.finalizeIdQuery(idProperty);
			
			// Apply pagination to the ID query
			TypedQuery<Object[]> idQuery = criteriaContext.getEntityManager().createQuery(query);
			idQuery.setFirstResult(theParams.getFromIndex());
			if (theParams.getToIndex() != Integer.MAX_VALUE && theParams.getToIndex() >= 0) {
				int maxResults = theParams.getToIndex() - theParams.getFromIndex();
				if (maxResults >= 0) {
					idQuery.setMaxResults(maxResults);
				}
			}
			
			List<Object[]> idResults = idQuery.getResultList();
			
			if (idResults == null || idResults.isEmpty()) {
				return Collections.emptyList();
			}
			
			// Extract IDs from the results
			// If there are sort orders, finalizeIdQuery returns Object[] with ID as first element
			// Otherwise, it returns just the ID
			List<Integer> ids = new ArrayList<>();
			for (Object[] row : idResults) {
				if (row != null && row.length > 0 && row[0] != null) {
					ids.add((Integer) row[0]);
				}
			}
			
			if (ids.isEmpty()) {
				return Collections.emptyList();
			}
			
			// Use the IDs to fetch full objects
			// We still need to sort the wrapper query to maintain the order, as IN() doesn't guarantee order
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
			        .countDistinct(criteriaContext.getRoot().get(getIdPropertyName(criteriaContext.getEntityManager()))));
		}
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult().intValue();
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
	
	/**
	 * This is an overrideable implementation to convert a result that may be a proxied Hibernate object
	 * into its underlying implementation.
	 * <p/>
	 * Overrides are most commonly needed where, e.g., "deep" properties of an object are usually needed
	 * to properly convert that object to FHIR, e.g., to ensure that the {@link org.openmrs.Concept}
	 * associated with an {@link org.openmrs.Obs} is also deproxied when the {@link org.openmrs.Obs} is
	 * deproxied.
	 * <p/>
	 * It is uncommon for subclasses to need this functionality
	 *
	 * @param result The Hibernate result object to deproxy
	 * @return The underlying implementation of the supplied object
	 * @see BaseDao#deproxyObject(Object)
	 */
	protected T deproxyResult(@Nonnull T result) {
		return deproxyObject(result);
	}
	
	/**
	 * Gets the name of the property annotated as the {@link javax.persistence.Id} for the persistent
	 * class this manages.
	 *
	 * @param entityManager The current entity manager
	 * @return The name of the id property for the domain object managed by this class
	 */
	@SuppressWarnings("UnstableApiUsage")
	protected String getIdPropertyName(@Nonnull EntityManager entityManager) {
		return getIdPropertyName(entityManager, typeToken.getRawType());
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
	
	protected <V, U> Optional<Predicate> handleCommonSearchParameters(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        List<PropParam<?>> theCommonParams) {
		List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
		
		for (PropParam<?> commonSearchParam : theCommonParams) {
			switch (commonSearchParam.getPropertyName()) {
				case FhirConstants.ID_PROPERTY:
					predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(),
					    (TokenAndListParam) commonSearchParam.getParam(), param -> Optional.of(criteriaContext
					            .getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), param.getValue()))));
					break;
				case FhirConstants.LAST_UPDATED_PROPERTY:
					predicateList.add(handleLastUpdated(criteriaContext, (DateRangeParam) commonSearchParam.getParam()));
					break;
			}
		}
		return Optional.of(criteriaContext.getCriteriaBuilder().and(toCriteriaArray(predicateList.stream())));
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
	
	/**
	 * This function should be overridden by implementations. It is used to return a criterion for
	 * _lastUpdated from resources where there are multiple properties to be considered.
	 *
	 * @param param the DateRangeParam used to query for _lastUpdated
	 * @return an optional criterion for the query
	 */
	protected <V, U> Optional<Predicate> handleLastUpdated(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        DateRangeParam param) {
		if (isImmutable) {
			return handleLastUpdatedImmutable(criteriaContext, param);
		}
		
		return handleLastUpdatedMutable(criteriaContext, param);
	}
	
	// Implementation of handleLastUpdated for "immutable" types, that is, those that cannot be changed
	protected <V, U> Optional<Predicate> handleLastUpdatedImmutable(
	        @Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, DateRangeParam param) {
		return getSearchQueryHelper().handleDateRange(criteriaContext, "dateCreated", param);
	}
	
	protected <V, U> Optional<Predicate> handleLastUpdatedMutable(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        DateRangeParam param) {
		// @formatter:off
		return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(getSearchQueryHelper().handleDateRange(criteriaContext,"dateChanged", param), Optional.of(
                criteriaContext.getCriteriaBuilder().and(toCriteriaArray(Stream.of(Optional.of(criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("dateChanged"))), getSearchQueryHelper().handleDateRange(criteriaContext,"dateCreated", param))))))));
		// @formatter:on
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
	 * Use this method to properly implement sorting for your query. Note that for this method to work,
	 * you must override one or more of: {@link #paramToProps(OpenmrsFhirCriteriaContext, SortState)},
	 * {@link #paramToProps(OpenmrsFhirCriteriaContext, String)}, or
	 * {@link #paramToProp(OpenmrsFhirCriteriaContext, String)}.
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 * @param sort the {@link SortSpec} which defines the sorting to be translated
	 */
	protected <V, U> void handleSort(OpenmrsFhirCriteriaContext<V, U> criteriaContext, SortSpec sort) {
		handleSort(criteriaContext, sort, this::paramToProps).ifPresent(l -> l.forEach(criteriaContext::addOrder));
	}
	
	protected <V, U> Optional<List<javax.persistence.criteria.Order>> handleSort(
	        OpenmrsFhirCriteriaContext<V, U> criteriaContext, SortSpec sort,
	        BiFunction<OpenmrsFhirCriteriaContext<V, U>, SortState<V>, Collection<javax.persistence.criteria.Order>> paramToProp) {
		
		List<javax.persistence.criteria.Order> orderings = new ArrayList<>();
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			SortState<V> state = SortState.<V> builder().context(criteriaContext).sortOrder(sortSpec.getOrder())
			        .parameter(sortSpec.getParamName().toLowerCase()).build();
			
			Collection<javax.persistence.criteria.Order> orders = paramToProp.apply(criteriaContext, state);
			if (orders != null) {
				orderings.addAll(orders);
			}
			
			sortSpec = sortSpec.getChain();
		}
		
		if (orderings.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.of(orderings);
	}
	
	/**
	 * Override to return false if the getSearchResults may return duplicate items that need to be
	 * removed from the results. Note that it has performance implications as it requires "select
	 * distinct" and 2 queries instead of 1 for getting the results.
	 * <p/>
	 * This is primarily needed for cases where one domain type extends another, similar to the
	 * relationship between {@link org.openmrs.Patient} and {@link org.openmrs.Person} where
	 * {@link org.openmrs.Patient} extends {@link org.openmrs.Person}.
	 *
	 * @return By default, <tt>true</tt>
	 */
	protected boolean hasDistinctResults() {
		return true;
	}
	
	/**
	 * This function is used to map FHIR parameter names to properties where there is only a single
	 * property.
	 *
	 * @param param the FHIR parameter to map
	 * @return the name of the corresponding property from the current query
	 */
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		if (DomainResource.SP_RES_ID.equals(param)) {
			return criteriaContext.getRoot().get("uuid");
		}
		
		return null;
	}
	
	/**
	 * This function should be overridden by implementations. It is used to map FHIR parameter names to
	 * their corresponding values in the query.
	 *
	 * @param sortState a {@link SortState} object describing the current sort state
	 * @return the corresponding ordering(s) needed for this property
	 */
	protected <V, U> Collection<javax.persistence.criteria.Order> paramToProps(
	        @Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull SortState<V> sortState) {
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
		
		Collection<Path<?>> prop = paramToProps(criteriaContext, sortState.getParameter());
		if (prop != null) {
			switch (sortState.getSortOrder()) {
				case ASC:
					return prop.stream().map(p -> criteriaContext.getCriteriaBuilder().asc(p)).collect(Collectors.toList());
				case DESC:
					return prop.stream().map(p -> criteriaContext.getCriteriaBuilder().desc(p)).collect(Collectors.toList());
			}
		}
		
		return null;
	}
	
	/**
	 * This function should be overridden by implementations. It is used to map FHIR parameter names to
	 * properties where there is only a single property.
	 *
	 * @param param the FHIR parameter to map
	 * @return the name of the corresponding property from the current query
	 */
	protected <V, U> Collection<Path<?>> paramToProps(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        @Nonnull String param) {
		Path<?> prop = paramToProp(criteriaContext, param);
		
		if (prop != null) {
			return Collections.singleton(prop);
		}
		
		return null;
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
	 * This is intended to be overridden by subclasses to implement the handling of specific search
	 * parameters.
	 * <p/>
	 * The {@link SearchParameterMap} provides an interface for tracking all the FHIR search parameters
	 * the user supplied for the current search query. It is the responsibility of this function to map
	 * all known parameters into JPA criteria {@link Predicate}s that are added to the current
	 * {@link OpenmrsFhirCriteriaContext}, so that they act as appropriate restrictions on the generated
	 * queries.
	 * <p/>
	 * The {@link BaseDao} and {@link FhirSearchQueryHelper} classes provide helpers for constructing
	 * such queries, with the {@link BaseDao} providing generic handling for HAPI's
	 * {@link ca.uhn.fhir.model.api.IQueryParameterAnd} and
	 * {@link ca.uhn.fhir.model.api.IQueryParameterOr} types. Meanwhile, the
	 * {@link FhirSearchQueryHelper} provides helpers for correctly constructing predicates for various
	 * HAPI FHIR types or common query elements, e.g., translating
	 * {@link ca.uhn.fhir.rest.param.TokenParam}s into queries against the OpenMRS Concept Dictionary.
	 * <p/>
	 * Generally, implementations of this class consist of a simple switch statement to handle each
	 * parameter in turn.
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 * @param theParams the parameters for this search
	 */
	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(param -> {
			switch (param.getKey()) {
				case COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, param.getValue()).ifPresent(criteriaContext::addPredicate);
			}
		});
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
}
