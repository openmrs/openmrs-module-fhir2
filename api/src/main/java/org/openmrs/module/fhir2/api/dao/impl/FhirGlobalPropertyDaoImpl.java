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

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.api.dao.FhirGlobalPropertyDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirGlobalPropertyDaoImpl implements FhirGlobalPropertyDao {
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public String getGlobalProperty(String property) throws APIException {
		GlobalProperty globalProperty = (GlobalProperty) sessionFactory.getCurrentSession()
		        .createCriteria(GlobalProperty.class).add(Restrictions.eq("property", property)).uniqueResult();
		return globalProperty == null ? null : globalProperty.getPropertyValue();
	}
	
	@Override
	public GlobalProperty getGlobalPropertyObject(String property) {
		return (GlobalProperty) sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class)
		        .add(Restrictions.eq("property", property)).uniqueResult();
	}
	
	@Override
	public Map<String, String> getGlobalProperties(String... properties) {
		Map<String, String> globalPropertiesMap = new HashMap<>();
		
		Collection<GlobalProperty> globalProperties = (sessionFactory.getCurrentSession()
		        .createCriteria(GlobalProperty.class).add(Restrictions.in("property", properties)).list());
		
		for (GlobalProperty property : globalProperties) {
			globalPropertiesMap.put(property.getProperty(), property.getPropertyValue());
		}
		
		return globalPropertiesMap;
	}
}
