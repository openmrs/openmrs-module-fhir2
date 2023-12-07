package org.openmrs.module.fhir2.api.dao.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OpenmrsFhirCriteriaContext<T> {

   @Getter
   @NonNull
   private final EntityManager entityManager;

   @Getter
   @NonNull
   private final CriteriaBuilder criteriaBuilder;

   @Getter
   @NonNull
   private final CriteriaQuery<T> criteriaQuery;

   @Getter
   @NonNull
   private final Root<T> root;

   private final List<Predicate> predicates = new ArrayList<>();

   private final List<Order> orders = new ArrayList<>();

   public void addPredicate(Predicate predicate) {
      predicates.add(predicate);
   }

   public void addOrder(Order order) {
      orders.add(order);
   }

   public CriteriaQuery<T> finalizeQuery() {
      return criteriaQuery.where(predicates.toArray(new Predicate[0])).orderBy(orders);
   }
}
