/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.spring;

import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;

/**
 * This is a Spring Auto-proxy creator that matches beans that implement the FhirDao marker
 * interface, which is where authorization is checked in the FHIR2 module and sub-implementations.
 * This is used to ensure that our custom interceptors are applied to the classes in the FHIR2
 * module, even though they do not follow our usual pattern of registering with the service
 * container.
 */
public class FhirAutoProxyCreator extends AbstractAutoProxyCreator {
	
	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(@NonNull Class<?> beanClass, @NonNull String beanName,
	        TargetSource customTargetSource) throws BeansException {
		if (FhirDao.class.isAssignableFrom(beanClass)) {
			return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
		}
		
		return DO_NOT_PROXY;
	}
}
