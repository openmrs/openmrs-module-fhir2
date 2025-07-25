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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.openmrs.aop.AuthorizationAdvice;
import org.openmrs.module.fhir2.api.FhirHelperService;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDaoAop;
import org.openmrs.module.fhir2.api.translators.FhirTranslator;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * This is a Spring AOP configuration that add advices to beans that implement the FhirDao marker
 * interface, which is where authorization is checked in the FHIR2 module and sub-implementations.
 * This is used to ensure that our custom interceptors are applied to the classes in the FHIR2
 * module, even though they do not follow our usual pattern of registering with the service
 * container.
 */
@Configuration
@EnableAspectJAutoProxy
public class FhirAopConfiguration {
	
	@Bean
	public Advisor createFhirAuthorizationAdvisor(@Autowired AuthorizationAdvice authorizationAdvice) {
		return new StaticMethodMatcherPointcutAdvisor(authorizationAdvice) {
			
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return FhirDaoAop.class.isAssignableFrom(targetClass);
			}
		};
	}
	
	@Bean
	public Advisor createFhirTransactionAdvisor(@Autowired(required = false) TransactionInterceptor transactionInterceptor) {
		if (transactionInterceptor != null) {
			// TransactionInterceptor is not available since core 2.8 as it is done via tx:annotation-driven on all beans
			return new StaticMethodMatcherPointcutAdvisor(transactionInterceptor) {
				
				@Override
				public boolean matches(Method method, Class<?> targetClass) {
					return FhirDaoAop.class.isAssignableFrom(targetClass);
				}
			};
		}
		return null;
	}
	
	/**
	 * Creates advisors for FHIR beans using Spring caching. It is required before OpenMRS core 2.8.
	 * 
	 * @param cacheInterceptor the cache interceptor
	 * @return the cache advisor
	 */
	@Bean
	public Advisor createFhirCacheAdvisor(@Autowired(required = false) CacheInterceptor cacheInterceptor) {
		if (cacheInterceptor != null) {
			// CacheInterceptor is not available since core 2.8 as it is done via cache:annotation-driven on all beans
			return new StaticMethodMatcherPointcutAdvisor(cacheInterceptor) {
				
				@Override
				public boolean matches(Method method, Class<?> targetClass) {
					return (FhirTranslator.class.isAssignableFrom(targetClass)
					        || FhirService.class.isAssignableFrom(targetClass)
					        || FhirHelperService.class.isAssignableFrom(targetClass))
					        && (AnnotationUtils.findAnnotation(targetClass, CacheConfig.class) != null
					                || AnnotationUtils.findAnnotation(targetClass, Cacheable.class) != null
					                || AnnotationUtils.findAnnotation(targetClass, CacheEvict.class) != null
					                || AnnotationUtils.findAnnotation(targetClass, CachePut.class) != null
					                || Arrays.stream(targetClass.getMethods())
					                        .anyMatch((m) -> AnnotationUtils.findAnnotation(m, Cacheable.class) != null
					                                || AnnotationUtils.findAnnotation(m, CacheEvict.class) != null
					                                || AnnotationUtils.findAnnotation(m, CachePut.class) != null));
				}
			};
		}
		return null;
	}
}
