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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.fhir2.api.dao.FhirContactPointMapDao;
import org.openmrs.module.fhir2.model.FhirConceptSource;
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
	
	private EntityManager entityManager;
	
	private CriteriaBuilder criteriaBuilder;
	
	private CriteriaQuery<FhirContactPointMap> criteriaQuery;
	
	private Root<FhirContactPointMap> fhirFhirContactPointMapRoot;
	
	private TypedQuery<FhirContactPointMap> fhirFhirContactPointMapTypedQuery;
	
	public FhirContactPointMapDaoImpl() {
		entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(FhirContactPointMap.class);
		fhirFhirContactPointMapRoot = criteriaQuery.from(FhirContactPointMap.class);
		fhirFhirContactPointMapTypedQuery = entityManager.createQuery(criteriaQuery);
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapByUuid(String uuid) {
		criteriaBuilder.and(criteriaBuilder.equal(fhirFhirContactPointMapRoot.get("uuid"), uuid));
		return Optional.ofNullable(fhirFhirContactPointMapTypedQuery.getSingleResult());
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(
	        @Nonnull PersonAttributeType attributeType) {
		if (attributeType == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable((FhirContactPointMap) sessionFactory.getCurrentSession().createQuery(
		    "from FhirContactPointMap fcp where fcp.attributeTypeDomain = 'person' and fcp.attributeTypeId = :attribute_type_id")
		        .setParameter("attribute_type_id", attributeType.getId()).uniqueResult());
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
		
		return Optional.ofNullable((FhirContactPointMap) sessionFactory.getCurrentSession().createQuery(
		    "from FhirContactPointMap fcp where fcp.attributeTypeDomain = :attribute_type_domain and fcp.attributeTypeId = :attribute_type_id")
		        .setParameter("attribute_type_domain", attributeTypeDomain)
		        .setParameter("attribute_type_id", attributeType.getId()).uniqueResult());
	}
	
	@Override
	public FhirContactPointMap saveFhirContactPointMap(@Nonnull FhirContactPointMap contactPointMap) {
		FhirContactPointMap existingContactPointMap = (FhirContactPointMap) sessionFactory.getCurrentSession().createQuery(
		    "from FhirContactPointMap fcp where fcp.attributeTypeDomain = :attribute_type_domain and fcp.attributeTypeId = :attribute_type_id")
		        .setParameter("attribute_type_domain", contactPointMap.getAttributeTypeDomain())
		        .setParameter("attribute_type_id", contactPointMap.getAttributeTypeId()).uniqueResult();
		
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
	
}
