/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import javax.servlet.ServletContext;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * Simple wrapper for an {@link ApplicationContext} and a {@link ServletContext}, providing a
 * {@link WebApplicationContext} This exists so that in integration tests we can treat the existing
 * {@link ApplicationContext} as if it were a {@link WebApplicationContext}
 */
public class DelegatingWebApplicationContext implements WebApplicationContext {
	
	private final ApplicationContext applicationContext;
	
	@Getter(AccessLevel.PUBLIC)
	private final ServletContext servletContext;
	
	public DelegatingWebApplicationContext(ApplicationContext delegate, ServletContext servletContext) {
		this.applicationContext = delegate;
		this.servletContext = servletContext;
	}
	
	@Override
	public String getId() {
		return applicationContext.getId();
	}
	
	@Override
	public String getApplicationName() {
		return applicationContext.getApplicationName();
	}
	
	@Override
	public String getDisplayName() {
		return applicationContext.getDisplayName();
	}
	
	@Override
	public long getStartupDate() {
		return applicationContext.getStartupDate();
	}
	
	@Override
	public ApplicationContext getParent() {
		return applicationContext.getParent();
	}
	
	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return applicationContext.getAutowireCapableBeanFactory();
	}
	
	@Override
	public Environment getEnvironment() {
		return applicationContext.getEnvironment();
	}
	
	@Override
	public boolean containsBeanDefinition(String beanName) {
		return applicationContext.containsBeanDefinition(beanName);
	}
	
	@Override
	public Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}
	
	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return applicationContext.getBean(name, requiredType);
	}
	
	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return applicationContext.getBean(requiredType);
	}
	
	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		return applicationContext.getBean(name, args);
	}
	
	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		return applicationContext.getBean(requiredType, args);
	}
	
	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass) {
		return applicationContext.getBeanProvider(aClass);
	}
	
	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType) {
		return applicationContext.getBeanProvider(resolvableType);
	}
	
	@Override
	public boolean containsBean(String name) {
		return applicationContext.containsBean(name);
	}
	
	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return applicationContext.isSingleton(name);
	}
	
	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return applicationContext.isPrototype(name);
	}
	
	@Override
	public boolean isTypeMatch(String s, ResolvableType resolvableType) throws NoSuchBeanDefinitionException {
		return applicationContext.isTypeMatch(s, resolvableType);
	}
	
	@Override
	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		return applicationContext.isTypeMatch(name, targetType);
	}
	
	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return applicationContext.getType(name);
	}
	
	@Override
	public Class<?> getType(String s, boolean b) throws NoSuchBeanDefinitionException {
		return applicationContext.getType(s,b);
	}
	
	@Override
	public String[] getAliases(String name) {
		return applicationContext.getAliases(name);
	}
	
	@Override
	public BeanFactory getParentBeanFactory() {
		return applicationContext.getParentBeanFactory();
	}
	
	@Override
	public boolean containsLocalBean(String name) {
		return applicationContext.containsLocalBean(name);
	}
	
	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return applicationContext.getMessage(code, args, defaultMessage, locale);
	}
	
	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return applicationContext.getMessage(code, args, locale);
	}
	
	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return applicationContext.getMessage(resolvable, locale);
	}
	
	@Override
	public void publishEvent(ApplicationEvent event) {
		applicationContext.publishEvent(event);
	}
	
	@Override
	public void publishEvent(Object o) {
		applicationContext.publishEvent(o);
	}
	
	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		return applicationContext.getResources(locationPattern);
	}
	
	@Override
	public Resource getResource(String location) {
		return applicationContext.getResource(location);
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return applicationContext.getClassLoader();
	}
	
	@Override
	public int getBeanDefinitionCount() {
		return applicationContext.getBeanDefinitionCount();
	}
	
	@Override
	public String[] getBeanDefinitionNames() {
		return applicationContext.getBeanDefinitionNames();
	}
	
	@Override
	public String[] getBeanNamesForType(ResolvableType resolvableType) {
		return applicationContext.getBeanNamesForType(resolvableType);
	}
	
	@Override
	public String[] getBeanNamesForType(ResolvableType resolvableType, boolean b, boolean b1) {
		return applicationContext.getBeanNamesForType(resolvableType,b,b1);
	}
	
	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		return applicationContext.getBeanNamesForType(type);
	}
	
	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		return applicationContext.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}
	
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return applicationContext.getBeansOfType(type);
	}
	
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
	        throws BeansException {
		return applicationContext.getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}
	
	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		return applicationContext.getBeanNamesForAnnotation(annotationType);
	}
	
	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
		return applicationContext.getBeansWithAnnotation(annotationType);
	}
	
	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
	        throws NoSuchBeanDefinitionException {
		return applicationContext.findAnnotationOnBean(beanName, annotationType);
	}
}
