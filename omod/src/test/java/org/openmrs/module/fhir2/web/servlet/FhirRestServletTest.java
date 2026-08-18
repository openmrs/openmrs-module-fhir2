/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.fhir2.api.annotations.FhirInterceptor;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.context.support.GenericApplicationContext;

public class FhirRestServletTest {
	
	@Mock
	private HttpServletRequest mockRequest;
	
	@Mock
	private HttpServletResponse mockResponse;
	
	@Mock
	private ServletConfig mockServletConfig;
	
	@Mock
	private PrintWriter mockWriter;
	
	private TestableFhirRestServlet servlet;
	
	private GenericApplicationContext context;
	
	@Before
	public void setUp() throws ServletException, IOException {
		MockitoAnnotations.initMocks(this);
		
		servlet = new TestableFhirRestServlet();
		
		when(mockServletConfig.getServletContext()).thenReturn(mock(javax.servlet.ServletContext.class));
		when(mockResponse.getWriter()).thenReturn(mockWriter);
		
		servlet.init(mockServletConfig);
	}
	
	@After
	public void closeContext() {
		if (context != null) {
			context.close();
			context = null;
		}
	}
	
	@Test
	public void testServiceSetsContextClassLoader() throws ServletException, IOException {
		// setup
		when(mockRequest.getMethod()).thenReturn("GET");
		when(mockRequest.getRequestURI()).thenReturn("/fhir2Servlet/metadata");
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/fhir2Servlet/metadata"));
		when(mockRequest.getServletPath()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		when(mockRequest.getQueryString()).thenReturn("");
		
		Thread.currentThread().setContextClassLoader(null);
		assertNull(Thread.currentThread().getContextClassLoader());
		servlet.service(mockRequest, mockResponse);
		assertEquals(OpenmrsClassLoader.getInstance(), Thread.currentThread().getContextClassLoader());
	}
	
	@Test
	public void registerInterceptors_shouldRegisterAnAnnotatedInterceptorBeanFromTheContext() {
		ContributedInterceptor contributed = withContextContaining("contributed", ContributedInterceptor.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		servlet.registerInterceptors();
		
		assertThat(servlet.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
	}
	
	/**
	 * A bean carrying hook methods but not the annotation is an ordinary bean, and picking it up would
	 * put every hook-bearing bean in every module into the FHIR request path without its author saying
	 * so. It carries real hooks deliberately: HAPI refuses an interceptor with none, so a hookless bean
	 * would pass this whether the annotation were honoured or not.
	 */
	@Test
	public void registerInterceptors_shouldIgnoreAHookBearingBeanThatIsNotAnnotated() {
		HookedButNotAnnotated ignored = withContextContaining("ignored", HookedButNotAnnotated.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		servlet.registerInterceptors();
		
		assertThat(servlet.getInterceptorService().getAllRegisteredInterceptors(), not(hasItem(ignored)));
	}
	
	private <T> T withContextContaining(String beanName, Class<T> beanClass) {
		context = new GenericApplicationContext();
		context.registerBeanDefinition(beanName, org.springframework.beans.factory.support.BeanDefinitionBuilder
		        .genericBeanDefinition(beanClass).getBeanDefinition());
		context.refresh();
		return context.getBean(beanName, beanClass);
	}
	
	/**
	 * Carries a real hook because HAPI refuses an interceptor that has none: it logs "Interceptor
	 * registered with no valid hooks" and moves on, so a bean annotated but hookless is silently absent
	 * from the request path.
	 */
	@FhirInterceptor
	public static class ContributedInterceptor {
		
		@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return true;
		}
	}
	
	public static class HookedButNotAnnotated {
		
		@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return true;
		}
	}
	
	class TestableFhirRestServlet extends FhirRestServlet {
		
		@Override
		public void initialize() {
		}
		
		@Override
		protected GenericApplicationContext getModuleApplicationContext() {
			return context;
		}
	}
}
