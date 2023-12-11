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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.fhir2.api.dao.FhirContactPointMapDao;
import org.openmrs.module.fhir2.model.FhirContactPointMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirContactPointMapDaoImpl implements FhirContactPointMapDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapByUuid(String uuid) {
		OpenmrsFhirCriteriaContext<FhirContactPointMap> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), uuid));
		
		return Optional.ofNullable(criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getSingleResult());
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(
	        @Nonnull PersonAttributeType attributeType) {
		if (attributeType == null) {
			return Optional.empty();
		}
		
		OpenmrsFhirCriteriaContext<FhirContactPointMap> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		return criteriaContext.getEntityManager().createQuery(
				"from FhirContactPointMap fcp where fcp.attributeTypeDomain = 'person' and fcp.attributeTypeId = :attribute_type_id",
				FhirContactPointMap.class).setParameter("attribute_type_id", attributeType.getId()).getResultList().stream().findFirst();
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForAttributeType(
	        @Nonnull BaseAttributeType<?> attributeType) {
		if (attributeType == null) {
			return Optional.empty();
		}
		
		String attributeTypeClassName = attributeType.getClass().getSimpleName();
		if (attributeTypeClassName == null || "".equals(attributeTypeClassName)
		        || !attributeTypeClassName.endsWith("AttributeType")) {
			return Optional.empty();
		}
		
		String attributeTypeDomain = StringUtils.removeEnd(attributeTypeClassName.toLowerCase(), "attributetype");
		if (attributeType instanceof LocationAttributeType) {
			attributeTypeDomain = "location";
		} else if (attributeType instanceof ProviderAttributeType) {
			attributeTypeDomain = "provider";
		}
		
		if (attributeTypeDomain == null) {
			return Optional.empty();
		}
		
		OpenmrsFhirCriteriaContext<FhirContactPointMap> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		return criteriaContext.getEntityManager().createQuery(
						"from FhirContactPointMap fcp where fcp.attributeTypeDomain = :attribute_type_domain and fcp.attributeTypeId = :attribute_type_id",
						FhirContactPointMap.class).setParameter("attribute_type_domain", attributeTypeDomain)
				.setParameter("attribute_type_id", attributeType.getId()).getResultList().stream().findFirst();
	}
	
	@Override
	public FhirContactPointMap saveFhirContactPointMap(@Nonnull FhirContactPointMap contactPointMap) {
		OpenmrsFhirCriteriaContext<FhirContactPointMap> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		FhirContactPointMap existingContactPointMap = criteriaContext.getEntityManager().createQuery(
						"from FhirContactPointMap fcp where fcp.attributeTypeDomain = :attribute_type_domain and fcp.attributeTypeId = :attribute_type_id",
						FhirContactPointMap.class).setParameter("attribute_type_domain", contactPointMap.getAttributeTypeDomain())
				.setParameter("attribute_type_id", contactPointMap.getAttributeTypeId()).getResultList().stream().findFirst().orElse(null);
		
		if (existingContactPointMap != null) {
			existingContactPointMap.setSystem(contactPointMap.getSystem());
			existingContactPointMap.setUse(contactPointMap.getUse());
			existingContactPointMap.setRank(contactPointMap.getRank());
			sessionFactory.getCurrentSession().merge(existingContactPointMap);
			return existingContactPointMap;
		} else {
			sessionFactory.getCurrentSession().saveOrUpdate(contactPointMap);
			return contactPointMap;
		}
	}
	
	private OpenmrsFhirCriteriaContext<FhirContactPointMap> openmrsFhirCriteriaContext() {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirContactPointMap> cq = cb.createQuery(FhirContactPointMap.class);
		Root<FhirContactPointMap> root = cq.from(FhirContactPointMap.class);
		
		return new OpenmrsFhirCriteriaContext<>(em,cb,cq,root);
	}
	
}
