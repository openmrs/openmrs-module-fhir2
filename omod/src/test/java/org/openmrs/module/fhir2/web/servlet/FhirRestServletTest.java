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
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.annotations.FhirInterceptor;
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
		
		// the page sizes the module ships with; the paging provider rejects a size of zero, which is what
		// an unstubbed mock would hand it
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
	
	/**
	 * The acceptance criterion this ticket turns on. The two tests above call registerInterceptors()
	 * directly, so they never run unregisterAllInterceptors() and cannot tell "registered" apart from
	 * "survives being unregistered and registered again" -- which is the whole defect. This one drives
	 * the real initialize() and then the real refreshed(), the path a module start or stop actually
	 * takes.
	 */
	@Test
	public void refreshed_shouldStillHaveTheContributedInterceptorAfterAContextRefresh() throws ServletException {
		ContributedInterceptor contributed = withRefreshableContext();
		
		LoggingInterceptor loggingInterceptorBeforeInit = new LoggingInterceptor();
		
		RefreshableFhirRestServlet refreshable = new RefreshableFhirRestServlet();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(loggingInterceptorBeforeInit);
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
		
		refreshable.refreshed();
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
		// the rebuilt context's logging interceptor is what shows the registry was actually torn down and
		// rebuilt. The built-ins alone would not: had unregisterAllInterceptors() never run they would
		// still be there from initialize(), so their presence tells the two cases apart not at all.
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(),
		    hasItem(context.getBean("hapiLoggingInterceptor", LoggingInterceptor.class)));
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(),
		    not(hasItem(loggingInterceptorBeforeInit)));
	}
	
	/**
	 * {@link FhirInterceptor} tells a module author its bean is picked up by every version-specific
	 * servlet, and nothing else checks that promise -- the R3 servlet keeps it only by not overriding
	 * initialize(), which a later change could do without noticing what it costs.
	 */
	@Test
	public void initialize_shouldRegisterTheContributedInterceptorOnTheR3ServletToo() throws ServletException {
		ContributedInterceptor contributed = withContextContaining("contributed", ContributedInterceptor.class);
		
		R3ServletReadingTheTestContext r3 = new R3ServletReadingTheTestContext();
		r3.setGlobalPropertyService(globalPropertyService);
		r3.setLoggingInterceptor(new LoggingInterceptor());
		r3.setMessageSource(new StaticMessageSource());
		r3.init(mockServletConfig);
		
		assertThat(r3.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
	}
	
	/**
	 * A context carrying the contributed interceptor plus every bean refreshed() resolves by name or by
	 * type. The names are the ones the servlet asks for; a rename here reads as the interceptor being
	 * lost rather than as the bean being missing.
	 */
	private ContributedInterceptor withRefreshableContext() {
		context = new GenericApplicationContext();
		context.registerBeanDefinition("contributed",
		    BeanDefinitionBuilder.genericBeanDefinition(ContributedInterceptor.class).getBeanDefinition());
		context.getBeanFactory().registerSingleton("hapiLoggingInterceptor", new LoggingInterceptor());
		context.getBeanFactory().registerSingleton("adminService", mock(AdministrationService.class));
		context.getBeanFactory().registerSingleton("fhirGlobalPropertyService", globalPropertyService);
		context.getBeanFactory().registerSingleton("serverAddressStrategy", mock(IServerAddressStrategy.class));
		context.refresh();
		return context.getBean("contributed", ContributedInterceptor.class);
	}
	
	private <T> T withContextContaining(String beanName, Class<T> beanClass) {
		context = new GenericApplicationContext();
		context.registerBeanDefinition(beanName, BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition());
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
	
	class R3ServletReadingTheTestContext extends FhirR3RestServlet {
		
		@Override
		protected GenericApplicationContext getModuleApplicationContext() {
			return context;
		}
	}
	
	/**
	 * Unlike {@link TestableFhirRestServlet} this leaves initialize() alone, because refreshed() does
	 * nothing until initialize() has set the servlet started.
	 */
	class RefreshableFhirRestServlet extends FhirRestServlet {
		
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
