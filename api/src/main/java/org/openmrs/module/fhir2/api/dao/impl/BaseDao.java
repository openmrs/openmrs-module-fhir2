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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ca.uhn.fhir.model.api.IQueryParameterAnd;
import ca.uhn.fhir.model.api.IQueryParameterOr;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.module.fhir2.api.dao.internals.BaseFhirCriteriaHolder;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>
 * A base class for OpenMRS FHIR2 Dao objects. It provides helpers to make generating complex
 * queries simpler.
 * </p>
 * <p>
 * For example, to create a query for people with the first name "Liam" and last name either
 * "Hemsworth" or "Neeson", the following code can be used: <pre>{@code
 *  StringAndListParam firstNames = new StringAndListParam().addAnd(new StringParam("Liam"));
 *  StringOrListParam lastNames = new StringOrListParam.addOr(new StringParam("Hemsworth), new StringParam("Neeson"));
 *  Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
 *  criteria.createAlias("names", "pn");
 *  Stream.of(
 *      handleAndParam(firstNames, name -> Optional.of(eq("pn.givenName", name))),
 *      handleOrParam(lastNames, name -> Optional.of(eq("pn.familyName", name))
 *  ).filter(Optional::isPresent).map(Optional::get).forEach(criteria::add);
 * }</pre>
 * </p>
 * <p>
 * This will generate a query that looks something like: <pre>{@code
 *  SELECT *
 *  FROM patient this_
 *      JOIN patient_name pn ON pn.person_id = this_.person_id
 *  WHERE pn.given_name = "Liam" AND (pn.family_name = "Hemsworth" or pn.family_name = "Neeson")
 * }</pre>
 * </p>
 * <p>
 * These methods can also be used to simplify the code to generate very complex queries. For
 * example, the following code allows grouping {@link TokenParam} representing
 * {@link org.hl7.fhir.r4.model.CodeableConcept}s into groups based on systems with correct AND / OR
 * logic: <pre>{@code
 *  Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 *  TokenAndListParam code = ...;
 *  handleAndListParamBySystem(code, (system, tokens) -> {
 *     if (system.isEmpty()) {
 *         return Optional.of(or(
 *             in("c.conceptId", tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList())),
 * 	               in("c.uuid", tokensToList(tokens))));
 *     } else {
 *         if (!containsAlias(criteria, "cm")) {
 *             criteria.createAlias("c.conceptMappings", "cm").createAlias("cm.conceptReferenceTerm", "crt");
 *         }
 *         DetachedCriteria conceptSourceCriteria = DetachedCriteria.forClass(FhirConceptSource.class).add(eq("url", system))
 *             .setProjection(property("conceptSource"));
 * 	       if (codes.size() > 1) {
 *            return Optional.of(and(propertyEq("crt.conceptSource", conceptSourceCriteria), in("crt.code", codes)));
 *         } else {
 *             return Optional.of(and(propertyEq("crt.conceptSource", conceptSourceCriteria), eq("crt.code", codes.get(0))));
 *         };
 *     }
 *  }).ifPresent(criteria::add);
 * }</pre>
 * </p>
 * <p>
 * This can generate queries that look something like: <pre>{@code
 *   SELECT *
 *   FROM obs this_
 *       JOIN concept c ON this_.concept_id = c.concept_id
 *       JOIN concept_reference_map cm on c.concept_id = cm.concept_id
 *       JOIN concept_reference_term crt on cm.concept_reference_term_id = crt.concept_reference_term_id
 *   WHERE ((
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code in (?, ?, ?)
 *   ) OR (
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code = ?
 *   )) AND (
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code in (?, ?, ?)
 *   );
 * }</pre>
 * </p>
 */
@Slf4j
public abstract class BaseDao {
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = { @Autowired, @Qualifier("sessionFactory") })
	private SessionFactory sessionFactory;
	
	/**
	 * If the supplied object is a {@link HibernateProxy} instead of the "normal" class, returns the
	 * "normal" class version of the object. If the supplied object is not a {@link HibernateProxy}, it
	 * is returned unchanged.
	 * <br/>
	 * Note that for objects that have collection properties is not guaranteed that these collections
	 * are materialized as they may be lazy-loaded on access.
	 *
	 * @param object The object to potentially convert to it's implementation
	 * @return A concrete implementation of an object of type {@link V}
	 * @param <V> The expected type of the object. Note that a {@link ClassCastException} may be thrown
	 *            if the underlying implementation of the proxied object does not match this type.
	 */
	protected static <V> V deproxyObject(@Nonnull V object) {
		if (object instanceof HibernateProxy) {
			Hibernate.initialize(object);
			@SuppressWarnings("unchecked")
			V theResult = (V) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
			return theResult;
		}
		
		return object;
	}
	
	/**
	 * Converts an {@link Iterable} to a {@link Stream} operated on in parallel
	 *
	 * @param iterable the iterable
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterable
	 */
	@SuppressWarnings("unused")
	protected static <T> Stream<T> parallelStream(Iterable<T> iterable) {
		return parallelStream(iterable.iterator());
	}
	
	/**
	 * Converts an {@link Iterator} to a {@link Stream} operated on in parallel
	 *
	 * @param iterator the iterator
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterator
	 */
	protected static <T> Stream<T> parallelStream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), true);
	}
	
	/**
	 * Converts an {@link Iterable} to a {@link Stream}
	 *
	 * @param iterable the iterable
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterable
	 */
	@SuppressWarnings("unused")
	protected static <T> Stream<T> stream(Iterable<T> iterable) {
		return stream(iterable.iterator());
	}
	
	/**
	 * Converts an {@link Iterator} to a {@link Stream}
	 *
	 * @param iterator the iterator
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterator
	 */
	protected static <T> Stream<T> stream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}
	
	/**
	 * This function creates a new {@link OpenmrsFhirCriteriaContext} for queries that select from the
	 * specified root type and return those objects as results.
	 *
	 * @param rootType An OpenMRS domain object that this criteria context queries
	 * @return An {@link OpenmrsFhirCriteriaContext} for constructing a new query
	 * @param <T> The root type and return type of queries run from the returned
	 *            {@link OpenmrsFhirCriteriaContext}
	 */
	protected <T> OpenmrsFhirCriteriaContext<T, T> createCriteriaContext(@Nonnull Class<T> rootType) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(rootType);
		Root<T> root = cq.from(rootType);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
	
	/**
	 * This function creates a new {@link OpenmrsFhirCriteriaContext} for queries that select from the
	 * specified root type and return a different type of result
	 *
	 * @param rootType An OpenMRS domain object that this criteria context queries
	 * @param resultType The type of results returned from this query
	 * @return An {@link OpenmrsFhirCriteriaContext} for constructing a new query
	 * @param <T> The root type of the returned {@link OpenmrsFhirCriteriaContext}
	 * @param <U> The rturn type of the returned {@link OpenmrsFhirCriteriaContext}
	 */
	protected <T, U> OpenmrsFhirCriteriaContext<T, U> createCriteriaContext(@Nonnull Class<T> rootType,
	        @Nonnull Class<U> resultType) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<U> cq = cb.createQuery(resultType);
		Root<T> root = cq.from(rootType);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
	
	/**
	 * Gets the name of the entity's id property. Note that this method relies on Hibernate's SPI for
	 * JPA, so while isn't using any internal implementation details, it is dependent on using
	 * Hibernate.
	 * <br/>
	 * This method also assumes that there <em>is</em> an identifiable property name for the instance,
	 * which is true for standard OpenMRS domain objects, but should be verified for domain objects not
	 * defined in core.
	 *
	 * @param entityManager The current entity manager
	 * @param clazz The persistent class to get the id from
	 * @return The name of the id property for this instance.
	 * @param <V> A persistent class
	 */
	protected <V> String getIdPropertyName(@Nonnull EntityManager entityManager, @Nonnull Class<V> clazz) {
		return ((MetamodelImplementor) entityManager.getEntityManagerFactory().getMetamodel()).entityPersister(clazz)
		        .getIdentifierPropertyName();
	}
	
	/**
	 * Attempts to provide a consistent interface to the {@link From} object representing either the
	 * {@link Root} object for the current criteria context or the {@link Join} object for the named
	 * alias.
	 *
	 * @param criteriaContext The {@link BaseFhirCriteriaHolder} to extract the {@link From} object from
	 * @param alias The name of the alias to extract from the criteria context or else a null or empty
	 *            string to return the root object
	 * @return Either the {@link Root} for this query or the {@link Join} that is aliased by the
	 *         provided name
	 * @param <T> The root type of the criteriaContext
	 */
	protected <T> From<?, ?> getRootOrJoin(@Nonnull BaseFhirCriteriaHolder<T> criteriaContext, @Nullable String alias) {
		if (alias == null || alias.isEmpty()) {
			return criteriaContext.getRoot();
		} else {
			return criteriaContext.getJoin(alias).orElseThrow(() -> new IllegalStateException(
			        "Tried to reference alias " + alias + " before creating a join with that name"));
		}
	}
	
	/**
	 * Attempts to provide a consistent interface to the {@link From} object representing either the
	 * {@link Root} object for the current criteria context or the {@link Join} object that matches the
	 * provided {@link From} object.
	 *
	 * @param criteriaContext The criteriaContext to extract the {@link From} object from
	 * @param alias The {@link From} alias to extract the join from
	 * @return Either the {@link Root} for this query or the {@link Join} that is aliased by the
	 *         provided name
	 * @param <T> The root type of the criteriaContext
	 * @param <U> The return type of the criteriaContext
	 */
	protected <T, U> From<?, ?> getRootOrJoin(OpenmrsFhirCriteriaContext<T, U> criteriaContext, From<?, ?> alias) {
		if (alias == null) {
			return criteriaContext.getRoot();
		} else {
			return criteriaContext.getJoin(alias).orElseThrow(() -> new IllegalStateException(
			        "Tried to reference alias " + alias + " before creating a join with that name"));
		}
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd<T>} which converts the
	 * {@link IQueryParameterAnd<T>} into a stream of {@link IQueryParameterOr<T>} objects.
	 *
	 * @param <T> the subtype of {@link IQueryParameterAnd} that this operates on
	 * @param <U> the subtype of {@link IQueryParameterType} that each {@link IQueryParameterOr} uses
	 * @return A stream of {@link IQueryParameterOr<T>}s contained in the {@link IQueryParameterAnd<T>}
	 */
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Stream<T> handleAndListParam(
	        IQueryParameterAnd<T> andListParameter) {
		return andListParameter.getValuesAsQueryTokens().stream();
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd<T>} which creates a criterion that
	 * represents the intersection of all the parameters contained<br/>
	 * <br/>
	 * This differs from {@link #handleAndListParamBy(CriteriaBuilder, IQueryParameterAnd, Function)} in
	 * that the handler is called for each parameter in the {@link IQueryParameterOr<U>}.
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param andListParam the {@link IQueryParameterAnd<T>} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Optional<Predicate>}
	 * @param <T> the subtype of {@link IQueryParameterAnd} that this operates on
	 * @param <U> the subtype of {@link IQueryParameterType} that each {@link IQueryParameterOr} uses
	 * @return the resulting criterion, which is the intersection of all the parameters
	 */
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParam(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam, Function<U, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(criteriaBuilder.and(toCriteriaArray(
		    handleAndListParam(andListParam).map(orListParam -> handleOrListParam(criteriaBuilder, orListParam, handler)))));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd<T>} which creates a criterion that
	 * represents the intersection of all the parameters contained<br/>
	 * <br/>
	 * This differs from {@link #handleAndListParam(CriteriaBuilder, IQueryParameterAnd, Function)} in
	 * that the handler is called during stream processing, which may make it more efficient.
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param andListParam the {@link IQueryParameterAnd<T>} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Optional<Predicate>}
	 * @param <T> the subtype of {@link IQueryParameterAnd} that this operates on
	 * @param <U> the subtype of {@link IQueryParameterType} that each {@link IQueryParameterOr} uses
	 * @return the resulting criterion, which is the intersection of all the parameters
	 */
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamAsStream(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        Function<U, Stream<Optional<Predicate>>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		Predicate[] predicates = toCriteriaArray(handleAndListParam(andListParam)
		        .map(orListParam -> handleOrListParamAsStream(criteriaBuilder, orListParam, handler)));
		
		if (predicates.length == 0) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and(predicates));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd<T>} which creates a criterion that
	 * represents the intersection of all the parameters contained<br/>
	 * <br/>
	 * This differs from {@link #handleAndListParam(CriteriaBuilder, IQueryParameterAnd, Function)} in
	 * that the handler is called with each {@link IQueryParameterOr<U>} rather than each individual
	 * parameter
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param andListParam the {@link IQueryParameterAnd<T>} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Optional<Predicate>}
	 * @param <T> the subtype of {@link IQueryParameterAnd} that this operates on
	 * @param <U> the subtype of {@link IQueryParameterType} that each {@link IQueryParameterOr} uses
	 * @return the resulting criterion, which is the intersection of all the parameters
	 */
	@SuppressWarnings("unused")
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamBy(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        Function<IQueryParameterOr<U>, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		Predicate[] predicates = toCriteriaArray(handleAndListParam(andListParam).map(handler));
		
		if (predicates.length == 0) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and(predicates));
	}
	
	/**
	 * Handler for a {@link IQueryParameterAnd} of {@link TokenParam}s where tokens should be grouped
	 * and handled according to the system they belong to This is useful for queries drawing their
	 * values from CodeableConcepts
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param andListParam the {@link IQueryParameterAnd} to handle
	 * @param systemTokenHandler a {@link BiFunction} taking the system and associated list of
	 *            {@link TokenParam}s and returning a {@link Criterion}
	 * @return a {@link Predicate} representing the intersection of all produced {@link Predicate}s
	 */
	protected <T extends IQueryParameterOr<TokenParam>> Optional<Predicate> handleAndListParamBySystem(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and(toCriteriaArray(handleAndListParam(andListParam)
		        .map(param -> handleOrListParamBySystem(criteriaBuilder, param, systemTokenHandler)))));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd<T>} which converts the
	 * {@link IQueryParameterOr<T>} into a stream of {@link IQueryParameterType<T>} objects.
	 *
	 * @param <T> the subtype of {@link IQueryParameterType} that each {@link IQueryParameterOr} uses
	 * @return A stream of {@link IQueryParameterType<T>}s contained in the {@link IQueryParameterOr<T>}
	 */
	protected <T extends IQueryParameterType> Stream<T> handleOrListParam(IQueryParameterOr<T> orListParameter) {
		return orListParameter.getValuesAsQueryTokens().stream();
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterOr} which creates a criterion that
	 * represents the union of all the parameters
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param orListParam the {@link IQueryParameterOr} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Criterion}
	 * @param <T> the subtype of {@link IQueryParameterType} for this parameter
	 * @return the resulting criterion, which is the union of all contained parameters
	 */
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParam(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<T> orListParam, Function<T, Optional<Predicate>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		Predicate[] predicates = toCriteriaArray(handleOrListParam(orListParam).map(handler));
		
		if (predicates.length == 0) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.or(predicates));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterOr} which creates a criterion that
	 * represents the union of all the parameters<br/>
	 * <br/>
	 * Unlike {@link #handleAndListParam(CriteriaBuilder, IQueryParameterAnd, Function)}, this function
	 * is called as part of the list streaming and may be slightly more efficient.
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param orListParam the {@link IQueryParameterOr} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Criterion}
	 * @param <T> the subtype of {@link IQueryParameterType} for this parameter
	 * @return the resulting {@link Predicate}, which is the union of all contained parameters
	 */
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParamAsStream(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<T> orListParam, Function<T, Stream<Optional<Predicate>>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		Predicate[] predicates = toCriteriaArray(handleOrListParam(orListParam).flatMap(handler));
		
		if (predicates.length == 0) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.or(predicates));
	}
	
	/**
	 * Handler for a {@link IQueryParameterOr} of {@link TokenParam}s where tokens should be grouped and
	 * handled according to the system they belong to This is useful for queries drawing their values
	 * from CodeableConcepts
	 *
	 * @param criteriaBuilder the active {@link CriteriaBuilder} for the current query
	 * @param orListParam the {@link IQueryParameterOr} to handle
	 * @param systemTokenHandler a {@link BiFunction} taking the system and associated list of
	 *            {@link TokenParam}s and returning a {@link Criterion}
	 * @return a {@link Criterion} representing the union of all produced {@link Criterion}
	 */
	protected Optional<Predicate> handleOrListParamBySystem(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<TokenParam> orListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.or(toCriteriaArray(
		    handleOrListParam(orListParam).collect(Collectors.groupingBy((t) -> StringUtils.trimToEmpty(t.getSystem())))
		            .entrySet().stream().map(e -> systemTokenHandler.apply(e.getKey(), e.getValue())))));
	}
	
	/**
	 * Converts a given set of {@link Optional<Predicate>}s into a {@link Predicate[]}. <br/>
	 * Any {@link Optional<Predicate>}s that are empty are skipped.
	 *
	 * @param predicates The {@link Optional<Predicate>}s to convert to an array
	 * @return an array of {@link Predicate} objects for each non-empty predicate in the supplied
	 *         arguments
	 */
	@SafeVarargs
	protected final @Nonnull Predicate[] toCriteriaArray(@Nonnull Optional<? extends Predicate>... predicates) {
		return toCriteriaArray(Arrays.stream(predicates));
	}
	
	/**
	 * Converts a given {@link Collection} of {@link Optional<Predicate>}s into a {@link Predicate[]}.
	 * <br/>
	 * Any {@link Optional<Predicate>}s that are empty are skipped.
	 *
	 * @param predicates The {@link Collection} of {@link Optional<Predicate>}s to convert to an array
	 * @return an array of {@link Predicate} objects for each non-empty predicate in the supplied
	 *         arguments
	 */
	protected @Nonnull Predicate[] toCriteriaArray(@Nonnull Collection<Optional<? extends Predicate>> predicates) {
		return toCriteriaArray(predicates.stream());
	}
	
	/**
	 * Converts a given {@link Stream} of {@link Optional<Predicate>}s into a {@link Predicate[]}. <br/>
	 * Any {@link Optional<Predicate>}s that are empty are skipped.
	 *
	 * @param predicates The {@link Stream} of {@link Optional<Predicate>}s to convert to an array
	 * @return an array of {@link Predicate} objects for each non-empty predicate in the supplied
	 *         arguments
	 */
	protected @Nonnull Predicate[] toCriteriaArray(@Nonnull Stream<Optional<? extends Predicate>> predicates) {
		return predicates.filter(Optional::isPresent).map(Optional::get).toArray(Predicate[]::new);
	}
	
	/**
	 * Concerts a {@link List} of {@link TokenParam}s into a {@link List} of {@link String}s where each
	 * string is the value only of the token param.
	 *
	 * @param tokens The {@link List} of {@link TokenParam}s to convert
	 * @return A list of the values of each {@link TokenParam}
	 */
	public List<String> tokensToList(List<TokenParam> tokens) {
		return tokensToParams(tokens).collect(Collectors.toList());
	}
	
	/**
	 * Concerts a {@link List} of {@link TokenParam}s into a {@link Stream} of {@link String}s where
	 * each string is the value only of the token param.
	 *
	 * @param tokens The {@link List} of {@link TokenParam}s to convert
	 * @return A {@link Stream} of the values of each {@link TokenParam}
	 */
	public Stream<String> tokensToParams(List<TokenParam> tokens) {
		return tokens.stream().map(TokenParam::getValue);
	}
}
