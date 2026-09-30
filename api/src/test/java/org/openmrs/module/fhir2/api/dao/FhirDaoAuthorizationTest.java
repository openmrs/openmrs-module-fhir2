/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openmrs.annotation.Authorized;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * Privilege checks on DAOs are enforced by the {@code @Authorized} advisor in
 * {@code FhirAopConfiguration}, which resolves the annotation from the interface method the DAO
 * implements. That makes the DAO interfaces the single place where access is declared, and an
 * unannotated method there is an unguarded database operation. This test scans every DAO interface
 * and requires that every read and every write declares a privilege, that the method names fit the
 * read/write vocabulary the scan understands, and that {@code exists(String)} carries exactly the
 * privilege of {@code get(String)}, since the composite services probe it before any other call.
 */
public class FhirDaoAuthorizationTest {
	
	private static final String[] READ_PREFIXES = { "get", "exists" };
	
	private static final String[] WRITE_PREFIXES = { "create", "save", "delete" };
	
	@Test
	public void everyReadAndWriteShouldDeclareAPrivilege() throws Exception {
		List<String> problems = new ArrayList<>();
		int checked = 0;
		
		for (Class<?> dao : daoInterfaces()) {
			for (Method method : effectiveMethods(dao).values()) {
				checked++;
				String label = dao.getSimpleName() + "." + signature(method);
				
				if (!startsWithAny(method.getName(), READ_PREFIXES) && !startsWithAny(method.getName(), WRITE_PREFIXES)) {
					problems.add(label + " is neither a read nor a write by name; rename it or extend this test");
					continue;
				}
				
				if (method.getAnnotation(Authorized.class) == null) {
					problems.add(label + " has no @Authorized");
				}
			}
		}
		
		assertThat(problems, empty());
		assertThat("expected to find DAO methods to check", checked, greaterThan(0));
	}
	
	@Test
	public void existsShouldCarryTheSamePrivilegeAsGetOnEveryDaoInterface() throws Exception {
		List<String> problems = new ArrayList<>();
		int checked = 0;
		
		for (Class<?> dao : daoInterfaces()) {
			Map<String, Method> methods = effectiveMethods(dao);
			Method get = methods.get("get(java.lang.String)");
			Authorized onGet = get == null ? null : get.getAnnotation(Authorized.class);
			if (onGet == null) {
				continue;
			}
			
			checked++;
			Method exists = methods.get("exists(java.lang.String)");
			Authorized onExists = exists == null ? null : exists.getAnnotation(Authorized.class);
			if (onExists == null) {
				problems.add(dao.getSimpleName() + " declares @Authorized on get(String) but not on exists(String)");
			} else if (!Arrays.equals(onGet.value(), onExists.value()) || onGet.requireAll() != onExists.requireAll()) {
				problems.add(dao.getSimpleName() + " declares different privileges on get(String) and exists(String)");
			}
		}
		
		assertThat(problems, empty());
		assertThat("expected to find annotated DAO interfaces to check", checked, greaterThan(0));
	}
	
	/**
	 * The abstract methods a DAO interface exposes, keyed by signature, resolved the way the advisor
	 * resolves them: a declaration on the interface itself wins over one inherited from a parent
	 * interface, so re-declaring a {@code FhirDao} method with {@code @Authorized} is what puts it
	 * under guard. Default methods count too, since the advisor intercepts calls to them as well.
	 */
	private static Map<String, Method> effectiveMethods(Class<?> dao) {
		Map<String, Method> methods = new LinkedHashMap<>();
		collectMethods(dao, dao, methods);
		return methods;
	}
	
	private static void collectMethods(Class<?> type, Class<?> dao, Map<String, Method> methods) {
		for (Method method : type.getDeclaredMethods()) {
			if (method.isSynthetic()) {
				continue;
			}
			methods.putIfAbsent(signature(method, dao), method);
		}
		
		for (Class<?> parent : type.getInterfaces()) {
			collectMethods(parent, dao, methods);
		}
	}
	
	/**
	 * Parameter types are resolved against the DAO so that {@code FhirDao<T>.createOrUpdate(T)} and
	 * {@code FhirPatientDao.createOrUpdate(Patient)} produce the same key and the latter wins.
	 */
	private static String signature(Method method, Class<?> dao) {
		StringBuilder sb = new StringBuilder(method.getName()).append('(');
		for (int i = 0; i < method.getParameterCount(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			ResolvableType parameter = ResolvableType.forMethodParameter(MethodParameter.forExecutable(method, i),
			    ResolvableType.forClass(dao));
			sb.append(parameter.toClass().getName());
		}
		return sb.append(')').toString();
	}
	
	private static String signature(Method method) {
		return signature(method, method.getDeclaringClass());
	}
	
	private static boolean startsWithAny(String name, String[] prefixes) {
		for (String prefix : prefixes) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	private static List<Class<?>> daoInterfaces() throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
			
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
			}
		};
		scanner.addIncludeFilter(new AssignableTypeFilter(FhirDaoAop.class));
		
		List<Class<?>> result = new ArrayList<>();
		for (BeanDefinition definition : scanner.findCandidateComponents(FhirDao.class.getPackage().getName())) {
			Class<?> type = Class.forName(definition.getBeanClassName());
			if (type != FhirDao.class && type != FhirDaoAop.class) {
				result.add(type);
			}
		}
		return result;
	}
}
