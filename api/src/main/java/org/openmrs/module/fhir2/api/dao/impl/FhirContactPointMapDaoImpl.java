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

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
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
	public Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(
	        @Nonnull PersonAttributeType attributeType) {
		if (attributeType == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable((FhirContactPointMap) sessionFactory.getCurrentSession().createQuery(
		    "from fhir_contact_point_map fcp where fcp.attribute_type_domain = 'person' and fcp.attribute_type_id = :attribute_type_id")
		        .setParameter("attribute_type_id", attributeType.getId()).uniqueResult());
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForAttributeType(
	        @Nonnull BaseAttributeType<?> attributeType) {
		if (attributeType == null) {
			return Optional.empty();
		}
		
		String attributeTypeDomain = null;
		if (attributeType instanceof LocationAttributeType) {
			attributeTypeDomain = "location";
		} else if (attributeType instanceof ProviderAttributeType) {
			attributeTypeDomain = "provider";
		}
		
		if (attributeTypeDomain == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable((FhirContactPointMap) sessionFactory.getCurrentSession().createQuery(
		    "from fhir_contact_point_map fcp where fcp.attribute_type_domain = :attribute_type_domain and fcp.attribute_type_id = :attribute_type_id")
		        .setParameter("attribute_type_domain", attributeTypeDomain)
		        .setParameter("attribute_type_id", attributeType.getId()).uniqueResult());
	}
	
	@Override
	public FhirContactPointMap saveFhirContactPointMap(@Nonnull FhirContactPointMap contactPointMap) {
		sessionFactory.getCurrentSession().saveOrUpdate(contactPointMap);
		return contactPointMap;
	}
}
