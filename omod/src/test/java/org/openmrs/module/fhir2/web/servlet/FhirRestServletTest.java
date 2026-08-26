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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
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
import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.annotations.FhirInterceptor;
import org.openmrs.module.fhir2.web.authentication.RequireAuthenticationInterceptor;
import org.openmrs.module.fhir2.web.util.DisableCacheInterceptor;
import org.openmrs.module.fhir2.web.util.SummaryInterceptor;
import org.openmrs.module.fhir2.web.util.SupportMergePatchInterceptor;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticMessageSource;

public class FhirRestServletTest {
	
	@Mock
	private HttpServletRequest mockRequest;
	
	@Mock
	private HttpServletResponse mockResponse;
	
	@Mock
	private ServletConfig mockServletConfig;
	
	@Mock
	private PrintWriter mockWriter;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private TestableFhirRestServlet servlet;
	
	private GenericApplicationContext context;
	
	@Before
	public void setUp() throws ServletException, IOException {
		MockitoAnnotations.initMocks(this);
		
		servlet = new TestableFhirRestServlet();
		
		when(mockServletConfig.getServletContext()).thenReturn(mock(javax.servlet.ServletContext.class));
		when(mockResponse.getWriter()).thenReturn(mockWriter);
		
		// the shipped defaults; the paging provider rejects the zero an unstubbed mock would hand it
		when(globalPropertyService.getGlobalPropertyAsInteger(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10))
		        .thenReturn(10);
		when(globalPropertyService.getGlobalPropertyAsInteger(FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE, 100))
		        .thenReturn(100);
		
		servlet.init(mockServletConfig);
	}
	
	@After
	public void closeContext() {
		if (context != null) {
			context.close();
			context = null;
		}
		// static, and every test in this module shares one JVM
		InterceptorWatchingProvider.watch(null);
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
	 * Picking up an unannotated bean would put every hook-bearing bean in every module into the FHIR
	 * request path uninvited. The fixture carries real hooks because HAPI drops a hookless interceptor
	 * anyway, so a hookless one would pass this whether the annotation were honoured or not.
	 */
	@Test
	public void registerInterceptors_shouldIgnoreAHookBearingBeanThatIsNotAnnotated() {
		HookedButNotAnnotated ignored = withContextContaining("ignored", HookedButNotAnnotated.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		servlet.registerInterceptors();
		
		assertThat(servlet.getInterceptorService().getAllRegisteredInterceptors(), not(hasItem(ignored)));
	}
	
	/**
	 * The {@code registerInterceptors_should...} tests call that method directly, so they never reach
	 * unregisterAllInterceptors() and cannot tell "registered" from "survives being unregistered and
	 * registered again" -- the whole defect. This drives the real initialize() then the real
	 * refreshed().
	 */
	@Test
	public void refreshed_shouldStillHaveTheContributedInterceptorAfterAContextRefresh() throws ServletException {
		ContributedInterceptor contributed = withRefreshableContext();
		
		LoggingInterceptor loggingInterceptorBeforeInit = new LoggingInterceptor();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(loggingInterceptorBeforeInit);
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
		
		refreshable.refreshed();
		
		List<Object> afterRefresh = refreshable.getInterceptorService().getAllRegisteredInterceptors();
		
		// the built-ins have to come back too, and nothing else here would notice if one stopped
		assertThat(afterRefresh,
		    hasItems(instanceOf(RequireAuthenticationInterceptor.class), instanceOf(DisableCacheInterceptor.class),
		        instanceOf(SummaryInterceptor.class), instanceOf(SupportMergePatchInterceptor.class)));
		assertThat(afterRefresh, hasItem(contributed));
		assertThat(afterRefresh, hasItem(sameInstance(context.getBean("hapiLoggingInterceptor", LoggingInterceptor.class))));
		// the assertion that shows the registry was torn down -- the rebuilt one's presence above passes
		// either way
		assertThat(afterRefresh, not(hasItem(sameInstance(loggingInterceptorBeforeInit))));
		// and nothing else, so a duplicate or a stray registration is caught rather than ignored
		assertThat(afterRefresh, hasSize(6));
	}
	
	/**
	 * {@link FhirInterceptor} promises a module author every version-specific servlet. R3 keeps that by
	 * inheriting initialize(), so an override added there later would drop contributed interceptors
	 * from the R3 API.
	 */
	@Test
	public void initialize_shouldRegisterTheContributedInterceptorOnTheR3ServletToo() throws ServletException {
		ContributedInterceptor contributed = withContextContaining("contributed", ContributedInterceptor.class);
		
		R3ServletWithTestContext r3 = new R3ServletWithTestContext();
		r3.setGlobalPropertyService(globalPropertyService);
		r3.setLoggingInterceptor(new LoggingInterceptor());
		r3.setMessageSource(new StaticMessageSource());
		r3.init(mockServletConfig);
		
		assertThat(r3.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
	}
	
	/**
	 * The servlet keeps answering through a refresh, and a request carrying no credentials passes
	 * straight through AuthenticationFilter -- which rejects only a failed Basic one -- so
	 * RequireAuthenticationInterceptor is what stops it. This provider reports the registry as it was
	 * when refreshed() built it, in the middle of rebuilding the providers.
	 */
	@Test
	public void refreshed_shouldKeepInterceptorsRegisteredWhileTheProvidersAreRebuilt() throws ServletException {
		withRefreshableContext();
		context.registerBeanDefinition("interceptorWatchingProvider",
		    BeanDefinitionBuilder.genericBeanDefinition(InterceptorWatchingProvider.class).getBeanDefinition());
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		InterceptorWatchingProvider.watch(refreshable);
		refreshable.refreshed();
		
		assertThat(InterceptorWatchingProvider.seenWhileBeingBuilt,
		    hasItem(instanceOf(RequireAuthenticationInterceptor.class)));
	}
	
	/**
	 * The contributed interceptor plus the four beans refreshed() throws without. It finds the
	 * contributed one by annotation, so that bean name is arbitrary; of the rest it asks for
	 * hapiLoggingInterceptor and adminService by name, the other two by type.
	 */
	private ContributedInterceptor withRefreshableContext() {
		ContributedInterceptor contributed = withContextContaining("contributed", ContributedInterceptor.class);
		context.getBeanFactory().registerSingleton("hapiLoggingInterceptor", new LoggingInterceptor());
		context.getBeanFactory().registerSingleton("adminService", mock(AdministrationService.class));
		context.getBeanFactory().registerSingleton("fhirGlobalPropertyService", globalPropertyService);
		context.getBeanFactory().registerSingleton("serverAddressStrategy", mock(IServerAddressStrategy.class));
		return contributed;
	}
	
	private <T> T withContextContaining(String beanName, Class<T> beanClass) {
		context = new GenericApplicationContext();
		context.registerBeanDefinition(beanName, BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition());
		context.refresh();
		return context.getBean(beanName, beanClass);
	}
	
	/**
	 * Records the servlet's interceptor registry from its own constructor, which refreshed() runs while
	 * it is rebuilding the resource providers. Static because Spring, not the test, constructs it.
	 */
	public static class InterceptorWatchingProvider implements IResourceProvider {
		
		private static FhirRestServlet watched;
		
		static List<Object> seenWhileBeingBuilt;
		
		static void watch(FhirRestServlet servlet) {
			watched = servlet;
			seenWhileBeingBuilt = null;
		}
		
		public InterceptorWatchingProvider() {
			if (watched != null) {
				seenWhileBeingBuilt = new ArrayList<>(watched.getInterceptorService().getAllRegisteredInterceptors());
			}
		}
		
		@Override
		public Class<? extends IBaseResource> getResourceType() {
			return Patient.class;
		}
	}
	
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
	
	class R3ServletWithTestContext extends FhirR3RestServlet {
		
		@Override
		protected GenericApplicationContext getModuleApplicationContext() {
			return context;
		}
	}
	
	/**
	 * Unlike {@link TestableFhirRestServlet} this leaves initialize() alone, because refreshed() does
	 * nothing until initialize() has set the servlet started.
	 */
	class R4ServletWithTestContext extends FhirRestServlet {
		
		@Override
		protected GenericApplicationContext getModuleApplicationContext() {
			return context;
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
