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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.annotation.Authorized;
import org.openmrs.aop.AuthorizationAdvice;
import org.openmrs.module.fhir2.api.dao.FhirDaoAop;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

@RunWith(MockitoJUnitRunner.class)
public class FhirAopConfigurationTest {

	@Mock
	private AuthorizationAdvice authorizationAdvice;

	private FhirAopConfiguration configuration;

	private Advisor advisor;

	@Before
	public void setup() {
		configuration = new FhirAopConfiguration();
		advisor = configuration.createFhirAuthorizationAdvisor(authorizationAdvice);
	}

	@Test
	public void matches_shouldMatchClassesImplementingFhirDaoAop() throws Exception {
		StaticMethodMatcherPointcutAdvisor pointcutAdvisor = (StaticMethodMatcherPointcutAdvisor) advisor;
		assertThat(pointcutAdvisor.matches(
		    DaoWithAuthorizedInterface.class.getMethod("authorizedMethod"), DaoImplWithoutAnnotation.class), is(true));
	}

	@Test
	public void matches_shouldNotMatchClassesNotImplementingFhirDaoAop() throws Exception {
		StaticMethodMatcherPointcutAdvisor pointcutAdvisor = (StaticMethodMatcherPointcutAdvisor) advisor;
		assertThat(pointcutAdvisor.matches(
		    NotADao.class.getMethod("someMethod"), NotADao.class), is(false));
	}

	@Test
	public void before_shouldDelegateToAuthorizationAdvice() throws Throwable {
		doNothing().when(authorizationAdvice).before(any(Method.class), any(), any());

		Method concreteMethod = DaoImplWithAnnotation.class.getMethod("authorizedMethod");
		Object target = new DaoImplWithAnnotation();
		Object[] args = new Object[0];

		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		advice.before(concreteMethod, args, target);

		verify(authorizationAdvice).before(any(Method.class), eq(args), eq(target));
	}

	@Test
	public void before_shouldDelegateWithOriginalMethodWhenNoAuthorizedAnnotationExists() throws Throwable {
		doNothing().when(authorizationAdvice).before(any(Method.class), any(), any());

		Method concreteMethod = DaoImplNoAuthAnywhere.class.getMethod("unannotatedMethod");
		Object target = new DaoImplNoAuthAnywhere();
		Object[] args = new Object[0];

		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		advice.before(concreteMethod, args, target);

		verify(authorizationAdvice).before(eq(concreteMethod), eq(args), eq(target));
	}

	@Test
	public void findAuthorizedInterfaceMethod_shouldFindAnnotatedInterfaceMethod() throws Exception {
		Method implMethod = DaoImplWithoutAnnotation.class.getMethod("authorizedMethod");

		Method resolved = invokeFindAuthorizedInterfaceMethod(implMethod, DaoImplWithoutAnnotation.class);

		assertThat(resolved, notNullValue());
		assertThat(resolved.getDeclaringClass(), equalTo(DaoWithAuthorizedInterface.class));
	}

	@Test
	public void findAuthorizedInterfaceMethod_shouldReturnNullWhenNoInterfaceHasAnnotation() throws Exception {
		Method implMethod = DaoImplNoAuthAnywhere.class.getMethod("unannotatedMethod");

		Method resolved = invokeFindAuthorizedInterfaceMethod(implMethod, DaoImplNoAuthAnywhere.class);

		assertThat(resolved, nullValue());
	}

	@Test
	public void findAuthorizedInterfaceMethod_shouldReturnNullWhenInterfaceDoesNotDeclareMethod() throws Exception {
		Method implMethod = DaoImplWithExtraMethod.class.getMethod("extraMethod");

		Method resolved = invokeFindAuthorizedInterfaceMethod(implMethod, DaoImplWithExtraMethod.class);

		assertThat(resolved, nullValue());
	}

	private Method invokeFindAuthorizedInterfaceMethod(Method method, Class<?> targetClass) throws Exception {
		Method findMethod = FhirAopConfiguration.class.getDeclaredMethod("findAuthorizedInterfaceMethod", Method.class,
		    Class.class);
		findMethod.setAccessible(true);
		return (Method) findMethod.invoke(null, method, targetClass);
	}

	// --- Test fixtures ---

	interface DaoWithAuthorizedInterface {

		@Authorized
		void authorizedMethod();
	}

	interface DaoWithoutAuthorizedInterface {

		void unannotatedMethod();
	}

	static class DaoImplWithAnnotation implements DaoWithAuthorizedInterface, FhirDaoAop {

		@Override
		@Authorized
		public void authorizedMethod() {}
	}

	static class DaoImplWithoutAnnotation implements DaoWithAuthorizedInterface, FhirDaoAop {

		@Override
		public void authorizedMethod() {}
	}

	static class DaoImplNoAuthAnywhere implements DaoWithoutAuthorizedInterface, FhirDaoAop {

		@Override
		public void unannotatedMethod() {}
	}

	static class DaoImplWithExtraMethod implements DaoWithAuthorizedInterface, FhirDaoAop {

		@Override
		public void authorizedMethod() {}

		public void extraMethod() {}
	}

	static class NotADao {

		public void someMethod() {}
	}
}
