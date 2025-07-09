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

import java.util.Arrays;

import org.openmrs.module.fhir2.api.FhirHelperService;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDaoAop;
import org.openmrs.module.fhir2.api.translators.FhirTranslator;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * This is a Spring Auto-proxy creator that matches beans that implement the FhirDao marker
 * interface, which is where authorization is checked in the FHIR2 module and sub-implementations.
 * This is used to ensure that our custom interceptors are applied to the classes in the FHIR2
 * module, even though they do not follow our usual pattern of registering with the service
 * container.
 */
public class FhirAutoProxyCreator extends AbstractAutoProxyCreator {
	
	private static final String[] DAO_PROXIES = new String[] { "authorizationInterceptor", "cacheInterceptor",
	        "transactionInterceptor" };
	
	private static final String[] CACHEABLE_PROXIES = new String[] { "cacheInterceptor" };
	
	private static volatile Object[] daoProxies = null;
	
	private static volatile Object[] cacheProxies = null;
	
	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(@NonNull Class<?> beanClass, @NonNull String beanName,
	        TargetSource customTargetSource) throws BeansException {
		if (FhirDaoAop.class.isAssignableFrom(beanClass)) {
			return getDaoProxies();
		} else if (FhirTranslator.class.isAssignableFrom(beanClass) || FhirService.class.isAssignableFrom(beanClass)
		        || FhirHelperService.class.isAssignableFrom(beanClass)) {
			boolean shouldProxy = AnnotationUtils.findAnnotation(beanClass, CacheConfig.class) != null
			        || AnnotationUtils.findAnnotation(beanClass, Cacheable.class) != null
			        || AnnotationUtils.findAnnotation(beanClass, CacheEvict.class) != null
			        || AnnotationUtils.findAnnotation(beanClass, CachePut.class) != null
			        || Arrays.stream(beanClass.getMethods())
			                .anyMatch((m) -> AnnotationUtils.findAnnotation(m, Cacheable.class) != null
			                        || AnnotationUtils.findAnnotation(m, CacheEvict.class) != null
			                        || AnnotationUtils.findAnnotation(m, CachePut.class) != null);
			
			if (shouldProxy) {
				return getCacheableProxies();
			}
		}
		
		return DO_NOT_PROXY;
	}
	
	private Object[] getDaoProxies() {
		if (daoProxies == null) {
			synchronized (FhirAutoProxyCreator.class) {
				if (daoProxies == null) {
					Object[] proxies = new Object[DAO_PROXIES.length];
					BeanFactory bf = getBeanFactory();
					Assert.state(bf != null, "BeanFactory required for resolving interceptor names");
					
					ConfigurableBeanFactory cbf = (bf instanceof ConfigurableBeanFactory ? (ConfigurableBeanFactory) bf
					        : null);
					AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
					
					for (int i = 0; i < DAO_PROXIES.length; i++) {
						String beanName = DAO_PROXIES[i];
						if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
							proxies[i] = advisorAdapterRegistry.wrap(bf.getBean(beanName));
						}
					}
					
					daoProxies = proxies;
				}
			}
		}
		
		return daoProxies;
	}
	
	private Object[] getCacheableProxies() {
		if (cacheProxies == null) {
			synchronized (FhirAutoProxyCreator.class) {
				if (cacheProxies == null) {
					Object[] proxies = new Object[CACHEABLE_PROXIES.length];
					BeanFactory bf = getBeanFactory();
					Assert.state(bf != null, "BeanFactory required for resolving interceptor names");
					
					ConfigurableBeanFactory cbf = (bf instanceof ConfigurableBeanFactory ? (ConfigurableBeanFactory) bf
					        : null);
					AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
					
					for (int i = 0; i < CACHEABLE_PROXIES.length; i++) {
						String beanName = CACHEABLE_PROXIES[i];
						if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
							proxies[i] = advisorAdapterRegistry.wrap(bf.getBean(beanName));
						}
					}
					
					cacheProxies = proxies;
				}
			}
		}
		
		return cacheProxies;
	}
}
