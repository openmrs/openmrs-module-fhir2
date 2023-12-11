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
public abstract class BaseFhirDao<T extends OpenmrsObject & Auditable> extends BaseDao<T> implements FhirDao<T> {

    private final boolean isRetireable;

    private final boolean isVoidable;

    private final boolean isImmutable;

    @Autowired
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    @Qualifier("sessionFactory")
    protected SessionFactory sessionFactory;

    @SuppressWarnings({"UnstableApiUsage"})
    protected BaseFhirDao() {
        this.isRetireable = Retireable.class.isAssignableFrom(typeToken.getRawType());
        this.isVoidable = Voidable.class.isAssignableFrom(typeToken.getRawType());
        this.isImmutable = Order.class.isAssignableFrom(typeToken.getRawType())
                || Obs.class.isAssignableFrom(typeToken.getRawType());
    }

    @Override
    @Transactional(readOnly = true)
    public T get(@Nonnull String uuid) {
        OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext();

        criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
        criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), uuid));

        // try-catch phrase is a workaround for https://github.com/jakartaee/persistence/issues/298
        try {
            return deproxyResult(criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> get(@Nonnull Collection<String> uuids) {
        OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext();

        criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
        criteriaContext.addPredicate(criteriaContext.getRoot().get("uuid").in(uuids));

        if (isVoidable) {
            handleVoidable(criteriaContext);
        } else if (isRetireable) {
            handleRetireable(criteriaContext);
        }

        return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery())
                .getResultList().stream().filter(Objects::nonNull).map(this::deproxyResult).collect(Collectors.toList());
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

    protected OpenmrsFhirCriteriaContext<T> getSearchResultCriteria(SearchParameterMap theParams) {
        OpenmrsFhirCriteriaContext<T> criteriaContext = createCriteriaContext();

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
        OpenmrsFhirCriteriaContext<T> criteriaContext = getSearchResultCriteria(theParams);

        handleSort(criteriaContext, theParams.getSortSpec());
        criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("id")));
        
        criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setFirstResult(
                theParams.getFromIndex());
        if (theParams.getToIndex() != Integer.MAX_VALUE) {
            int maxResults = theParams.getToIndex() - theParams.getFromIndex();
            criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).setMaxResults(maxResults);
        }

        List<T> results;
        if (hasDistinctResults()) {
            results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
        } else {
            
            OpenmrsFhirCriteriaContext<Long> longOpenmrsFhirCriteriaContext = createLongCriteriaContext();
            longOpenmrsFhirCriteriaContext.getCriteriaQuery().subquery(Long.class).select(longOpenmrsFhirCriteriaContext.getCriteriaBuilder().countDistinct(longOpenmrsFhirCriteriaContext.getRoot().get("id")));
            
            longOpenmrsFhirCriteriaContext.getCriteriaQuery().select(longOpenmrsFhirCriteriaContext.getRoot())
                    .where(longOpenmrsFhirCriteriaContext.getCriteriaBuilder().in(longOpenmrsFhirCriteriaContext.getRoot().get("id")).value(longOpenmrsFhirCriteriaContext.getCriteriaQuery().subquery(Long.class)));

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
            
            longOpenmrsFhirCriteriaContext.getCriteriaQuery().select(longOpenmrsFhirCriteriaContext.getRoot())
                    .where(longOpenmrsFhirCriteriaContext.getCriteriaBuilder().in(longOpenmrsFhirCriteriaContext.getRoot().get("id")));
            // Need to reapply ordering
            handleSort(criteriaContext, theParams.getSortSpec());
            criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("id")));

            results = criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
        }
        return results.stream().map(this::deproxyResult).collect(Collectors.toList());
    }


    @Override
    public int getSearchResultsCount(@Nonnull SearchParameterMap theParams) {
        OpenmrsFhirCriteriaContext<T> criteriaContext = getSearchResultCriteria(theParams);

        applyExactTotal(criteriaContext, theParams);

        if (hasDistinctResults()) {
            OpenmrsFhirCriteriaContext<Long> criteria = createLongCriteriaContext();
            criteria.getCriteriaQuery().select(criteria.getCriteriaBuilder().count(criteria.getRoot()));
            return criteria.getEntityManager().createQuery(criteria.getCriteriaQuery()).getSingleResult().intValue();
        } else {
            OpenmrsFhirCriteriaContext<Long> criteria = createLongCriteriaContext();
            criteria.getCriteriaQuery().select(criteria.getCriteriaBuilder().countDistinct(criteria.getRoot().get("id")));
            return criteria.getEntityManager().createQuery(criteria.getCriteriaQuery()).getSingleResult().intValue();
        }
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
    protected Optional<Predicate> handleLastUpdated(DateRangeParam param) {
        if (isImmutable) {
            return handleLastUpdatedImmutable(param);
        }

        return handleLastUpdatedMutable(param);
    }

    protected Optional<Predicate> handleLastUpdatedMutable(DateRangeParam param) {
        // @formatter:off
		return Optional.of(createCriteriaContext().getCriteriaBuilder().or(toCriteriaArray(handleDateRange("dateChanged", param), Optional.of(
                createCriteriaContext().getCriteriaBuilder().and(toCriteriaArray(Stream.of(Optional.of(createCriteriaContext().getCriteriaBuilder().isNull(createCriteriaContext().getRoot().get("dateChanged"))), handleDateRange("dateCreated", param))))))));
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
     * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
     */
    protected void handleVoidable(OpenmrsFhirCriteriaContext<T> criteriaContext) {
        criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("voided"), true));
    }

    /**
     * This provides a default implementation for dealing with retireable objects. By default, retired objects are excluded
     * from searches, but not from get
     *
     * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
     */
    protected void handleRetireable(OpenmrsFhirCriteriaContext<T> criteriaContext) {
        criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"), true));
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
     * @param theParams       the parameters for this search
     */
    protected void setupSearchParams(OpenmrsFhirCriteriaContext<T> criteriaContext, SearchParameterMap theParams) {

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

    @SuppressWarnings("unchecked")
    protected void applyExactTotal(OpenmrsFhirCriteriaContext<T> criteriaContext, SearchParameterMap theParams) {
        createCriteriaContext();
        List<PropParam<?>> exactTotal = theParams.getParameters(EXACT_TOTAL_SEARCH_PARAMETER);
        if (!exactTotal.isEmpty()) {
            PropParam<Boolean> propParam = (PropParam<Boolean>) exactTotal.get(0);
            if (propParam.getParam()) {
                criteriaContext.getEntityManager()
                        .createQuery(criteriaContext.getCriteriaQuery())
                        .setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
            }
        } else {
            criteriaContext.getEntityManager()
                    .createQuery(criteriaContext.getCriteriaQuery())
                    .setHint(HINT_CACHEABLE, "true");
            criteriaContext.getEntityManager()
                    .createQuery(criteriaContext.getCriteriaQuery())
                    .setHint("org.hibernate.cacheRegion", COUNT_QUERY_CACHE);
        }
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
