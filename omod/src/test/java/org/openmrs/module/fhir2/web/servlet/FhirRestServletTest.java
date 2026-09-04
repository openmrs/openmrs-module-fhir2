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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.interceptor.executor.InterceptorService;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
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
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
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
		
		// an unstubbed mock returns 0, which BasePagingProvider rejects
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
	 * The fixture declares a real hook: HAPI refuses a hookless interceptor anyway, so without one the
	 * test would pass either way.
	 */
	@Test
	public void registerInterceptors_shouldIgnoreAHookBearingBeanThatIsNotAnnotated() {
		HookedButNotAnnotated ignored = withContextContaining("ignored", HookedButNotAnnotated.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		servlet.registerInterceptors();
		
		assertThat(servlet.getInterceptorService().getAllRegisteredInterceptors(), not(hasItem(ignored)));
	}
	
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
		
		assertThat(afterRefresh,
		    hasItems(instanceOf(RequireAuthenticationInterceptor.class), instanceOf(DisableCacheInterceptor.class),
		        instanceOf(SummaryInterceptor.class), instanceOf(SupportMergePatchInterceptor.class)));
		assertThat(afterRefresh, hasItem(contributed));
		assertThat(afterRefresh, hasItem(sameInstance(context.getBean("hapiLoggingInterceptor", LoggingInterceptor.class))));
		// this is what shows the teardown ran; asserting the rebuilt interceptor present does not
		assertThat(afterRefresh, not(hasItem(sameInstance(loggingInterceptorBeforeInit))));
		assertThat(afterRefresh, hasSize(6));
	}
	
	@Test
	public void refreshed_shouldRejectAnUnauthenticatedRequestBeforeAContributedInterceptorSeesIt()
	        throws ServletException, IOException {
		ContributedInterceptor contributed = withRefreshableContext();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		refreshable.refreshed();
		
		when(mockRequest.getMethod()).thenReturn("GET");
		when(mockRequest.getRequestURI()).thenReturn("/fhir2Servlet/Patient/1");
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/fhir2Servlet/Patient/1"));
		when(mockRequest.getServletPath()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		when(mockRequest.getQueryString()).thenReturn("");
		
		refreshable.service(mockRequest, mockResponse);
		
		// dispatch order, not registry order: the registry sorts on class-level order alone
		verify(mockResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
		assertThat(contributed.sawRequest, is(false));
	}
	
	@Test
	public void refreshed_shouldRegisterAContributedInterceptorThatReachedTheContextAfterInitialize()
	        throws ServletException {
		withRefreshableContext();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		context.registerBeanDefinition("lateComer",
		    BeanDefinitionBuilder.genericBeanDefinition(ContributedInterceptor.class).getBeanDefinition());
		ContributedInterceptor lateComer = context.getBean("lateComer", ContributedInterceptor.class);
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), not(hasItem(lateComer)));
		
		refreshable.refreshed();
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(lateComer));
	}
	
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
	
	@Test
	public void refreshed_shouldStillRouteAResourceReadAfterARefresh() throws ServletException {
		withRefreshableContext();
		context.registerBeanDefinition("patientProvider",
		    BeanDefinitionBuilder.genericBeanDefinition(ReadablePatientProvider.class).getBeanDefinition());
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setFhirContext(FhirContext.forR4());
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.setResourceProviders(singletonList(context.getBean("patientProvider", ReadablePatientProvider.class)));
		refreshable.init(mockServletConfig);
		
		int boundAtInit = bindingCountFor(refreshable, "Patient");
		assertThat(boundAtInit, greaterThan(0));
		
		refreshable.refreshed();
		
		// equality, not greaterThan: registerProviders appends, so a skipped unregister doubles it
		assertThat(bindingCountFor(refreshable, "Patient"), is(boundAtInit));
		assertThat(refreshable.getResourceProviders(), hasSize(1));
	}
	
	@Test
	public void registerInterceptors_shouldNotRegisterAnInterceptorThatWouldSortAheadOfTheBuiltIns() {
		JumpsTheQueueInterceptor byType = withContextContaining("byType", JumpsTheQueueInterceptor.class);
		context.registerBeanDefinition("byHook",
		    BeanDefinitionBuilder.genericBeanDefinition(HookJumpsTheQueueInterceptor.class).getBeanDefinition());
		HookJumpsTheQueueInterceptor byHook = context.getBean("byHook", HookJumpsTheQueueInterceptor.class);
		// a well-ordered bean alongside them: rejecting one must not suppress the rest
		context.registerBeanDefinition("contributed",
		    BeanDefinitionBuilder.genericBeanDefinition(ContributedInterceptor.class).getBeanDefinition());
		ContributedInterceptor accepted = context.getBean("contributed", ContributedInterceptor.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		servlet.registerInterceptors();
		
		List<Object> registered = servlet.getInterceptorService().getAllRegisteredInterceptors();
		assertThat(registered, not(hasItem(byType)));
		assertThat(registered, not(hasItem(byHook)));
		assertThat(registered, hasItem(accepted));
		assertThat(registered, hasItem(instanceOf(RequireAuthenticationInterceptor.class)));
	}
	
	/**
	 * The invariant is about the moment an interceptor comes off, not the state either side of the
	 * refresh, so this watches every unregistration rather than comparing before and after.
	 */
	@Test
	public void refreshed_shouldNeverLeaveTheServletWithoutAnAuthenticationInterceptor() throws ServletException {
		withRefreshableContext();
		
		AuthenticationWatchingInterceptorService interceptorService = new AuthenticationWatchingInterceptorService();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setInterceptorService(interceptorService);
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		refreshable.refreshed();
		
		assertThat(interceptorService.authenticatedAtEachUnregister, not(empty()));
		assertThat(interceptorService.authenticatedAtEachUnregister, everyItem(is(true)));
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(),
		    hasItem(instanceOf(RequireAuthenticationInterceptor.class)));
	}
	
	/**
	 * A refresh that does not rebuild the Spring context hands back the same hapiLoggingInterceptor
	 * singleton, which the previous set also holds.
	 */
	@Test
	public void refreshed_shouldKeepALoggingInterceptorTheContextHandsBackUnchanged() throws ServletException {
		withRefreshableContext();
		LoggingInterceptor fromContext = context.getBean("hapiLoggingInterceptor", LoggingInterceptor.class);
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(fromContext);
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		refreshable.refreshed();
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(sameInstance(fromContext)));
	}
	
	/**
	 * The servlet unregisters only what it registered, so an interceptor another module put on the
	 * servlet directly outlives a refresh.
	 */
	@Test
	public void refreshed_shouldLeaveAnInterceptorRegisteredOutsideTheServletAlone() throws ServletException {
		withRefreshableContext();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		ContributedInterceptor outsider = new ContributedInterceptor();
		refreshable.registerInterceptor(outsider);
		
		refreshable.refreshed();
		
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(sameInstance(outsider)));
	}
	
	/**
	 * Surviving a refresh must not also mean overtaking authentication. The rebuilt interceptors are
	 * appended, so an outsider registered at the default order keeps an earlier slot in HAPI's list and
	 * only the reserved order on RequireAuthenticationInterceptor holds it back. An outsider that
	 * declares a lower order still wins; nothing here screens what it did not register.
	 */
	@Test
	public void refreshed_shouldRejectAnUnauthenticatedRequestBeforeAnOutsideInterceptorSeesIt()
	        throws ServletException, IOException {
		withRefreshableContext();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		ContributedInterceptor outsider = new ContributedInterceptor();
		refreshable.registerInterceptor(outsider);
		
		refreshable.refreshed();
		
		givenARequestFor("/fhir2Servlet/Patient/1");
		refreshable.service(mockRequest, mockResponse);
		
		verify(mockResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
		assertThat(outsider.sawRequest, is(false));
	}
	
	/**
	 * The sibling test asserting a contributed interceptor does not see an unauthenticated request
	 * would pass just as well if contributed hooks never dispatched at all; /metadata is the path
	 * RequireAuthenticationInterceptor lets through.
	 */
	@Test
	public void refreshed_shouldDispatchToAContributedInterceptorOnAnExemptRequest() throws ServletException, IOException {
		ContributedInterceptor contributed = withRefreshableContext();
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setFhirContext(FhirContext.forR4());
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		refreshable.refreshed();
		
		givenARequestFor("/fhir2Servlet/metadata");
		refreshable.service(mockRequest, mockResponse);
		
		assertThat(contributed.sawRequest, is(true));
	}
	
	/**
	 * A contributed bean HAPI cannot scan is third-party code failing, not a reason to abandon the
	 * rebuild: the beans after it still register and the built-ins are not left doubled up.
	 */
	@Test
	public void refreshed_shouldNotDuplicateTheBuiltInsWhenAContributedInterceptorCannotBeRegistered()
	        throws ServletException {
		context = new GenericApplicationContext();
		// declared first, so a rebuild that gave up on the first failure would drop "contributed"
		context.registerBeanDefinition("badHook",
		    BeanDefinitionBuilder.genericBeanDefinition(BadHookSignatureInterceptor.class).getBeanDefinition());
		context.registerBeanDefinition("contributed",
		    BeanDefinitionBuilder.genericBeanDefinition(ContributedInterceptor.class).getBeanDefinition());
		context.refresh();
		withRefreshableSingletons();
		ContributedInterceptor contributed = context.getBean("contributed", ContributedInterceptor.class);
		
		R4ServletWithTestContext refreshable = new R4ServletWithTestContext();
		refreshable.setGlobalPropertyService(globalPropertyService);
		refreshable.setLoggingInterceptor(new LoggingInterceptor());
		refreshable.setMessageSource(new StaticMessageSource());
		refreshable.init(mockServletConfig);
		
		// twice, because a set orphaned by a throw only becomes visible on the refresh after it
		refreshable.refreshed();
		refreshable.refreshed();
		
		assertThat(countRegistered(refreshable, RequireAuthenticationInterceptor.class), is(1L));
		assertThat(countRegistered(refreshable, DisableCacheInterceptor.class), is(1L));
		assertThat(countRegistered(refreshable, SummaryInterceptor.class), is(1L));
		assertThat(countRegistered(refreshable, SupportMergePatchInterceptor.class), is(1L));
		assertThat(refreshable.getInterceptorService().getAllRegisteredInterceptors(), hasItem(contributed));
	}
	
	/**
	 * getResourceProviderAnnotation() is overridden on the R3 servlet but read only from refreshed(),
	 * so no other test in this class reaches the override.
	 */
	@Test
	public void refreshed_shouldRegisterOnlyR3ProvidersOnTheR3Servlet() throws ServletException {
		withRefreshableContext();
		context.registerBeanDefinition("r3PatientProvider",
		    BeanDefinitionBuilder.genericBeanDefinition(R3PatientProvider.class).getBeanDefinition());
		context.registerBeanDefinition("r4PatientProvider",
		    BeanDefinitionBuilder.genericBeanDefinition(ReadablePatientProvider.class).getBeanDefinition());
		
		R3ServletWithTestContext r3 = new R3ServletWithTestContext();
		r3.setFhirContext(FhirContext.forDstu3());
		r3.setGlobalPropertyService(globalPropertyService);
		r3.setLoggingInterceptor(new LoggingInterceptor());
		r3.setMessageSource(new StaticMessageSource());
		r3.init(mockServletConfig);
		
		r3.refreshed();
		
		assertThat(r3.getResourceProviders(), hasSize(1));
		assertThat(r3.getResourceProviders(), hasItem(instanceOf(R3PatientProvider.class)));
	}
	
	/** refreshed() reaches a servlet the container has not initialized yet, and must no-op. */
	@Test
	public void refreshed_shouldDoNothingBeforeTheServletIsInitialized() {
		withRefreshableContext();
		
		R4ServletWithTestContext neverInitialized = new R4ServletWithTestContext();
		neverInitialized.setGlobalPropertyService(globalPropertyService);
		
		neverInitialized.refreshed();
		
		assertThat(neverInitialized.getInterceptorService().getAllRegisteredInterceptors(), is(empty()));
	}
	
	/**
	 * HAPI drops a hookless interceptor with only a WARN naming the runtime class, which for a proxied
	 * bean is a synthetic name that says nothing about FHIR2. The module's own error is the only thing
	 * that ties the drop back to a bean an author can go and fix, so it is the behaviour worth pinning.
	 */
	@Test
	public void registerInterceptors_shouldReportAContributedBeanHapiRefusesToRegister() {
		withContextContaining("hookless", HooklessInterceptor.class);
		
		servlet.setLoggingInterceptor(new LoggingInterceptor());
		
		List<String> errors = new ArrayList<>();
		Appender appender = collectErrorsInto(errors);
		try {
			servlet.registerInterceptors();
		}
		finally {
			stopCollecting(appender);
		}
		
		assertThat(errors, hasItem(allOf(containsString("hookless"), containsString("@Hook"))));
	}
	
	private Appender collectErrorsInto(List<String> errors) {
		Appender appender = new AbstractAppender("collectErrors", null, null, true, Property.EMPTY_ARRAY) {
			
			@Override
			public void append(LogEvent event) {
				if (event.getLevel() == Level.ERROR) {
					errors.add(event.getMessage().getFormattedMessage());
				}
			}
		};
		appender.start();
		loggerConfigFor(FhirRestServlet.class).addAppender(appender, Level.ERROR, null);
		return appender;
	}
	
	private void stopCollecting(Appender appender) {
		loggerConfigFor(FhirRestServlet.class).removeAppender(appender.getName());
		appender.stop();
	}
	
	private LoggerConfig loggerConfigFor(Class<?> type) {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		return loggerContext.getConfiguration().getLoggerConfig(type.getName());
	}
	
	private long countRegistered(FhirRestServlet servlet, Class<?> type) {
		return servlet.getInterceptorService().getAllRegisteredInterceptors().stream().filter(type::isInstance).count();
	}
	
	private void givenARequestFor(String uri) {
		when(mockRequest.getMethod()).thenReturn("GET");
		when(mockRequest.getRequestURI()).thenReturn(uri);
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost" + uri));
		when(mockRequest.getServletPath()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		when(mockRequest.getQueryString()).thenReturn("");
	}
	
	private int bindingCountFor(FhirRestServlet servlet, String resourceName) {
		return servlet.getResourceBindings().stream().filter(b -> resourceName.equals(b.getResourceName()))
		        .mapToInt(b -> b.getMethodBindings().size()).sum();
	}
	
	/**
	 * refreshed() looks two of these up by name, so hapiLoggingInterceptor and adminService cannot be
	 * renamed.
	 */
	private ContributedInterceptor withRefreshableContext() {
		ContributedInterceptor contributed = withContextContaining("contributed", ContributedInterceptor.class);
		withRefreshableSingletons();
		return contributed;
	}
	
	private void withRefreshableSingletons() {
		context.getBeanFactory().registerSingleton("hapiLoggingInterceptor", new LoggingInterceptor());
		context.getBeanFactory().registerSingleton("adminService", mock(AdministrationService.class));
		context.getBeanFactory().registerSingleton("fhirGlobalPropertyService", globalPropertyService);
		context.getBeanFactory().registerSingleton("serverAddressStrategy", mock(IServerAddressStrategy.class));
	}
	
	private <T> T withContextContaining(String beanName, Class<T> beanClass) {
		context = new GenericApplicationContext();
		context.registerBeanDefinition(beanName, BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition());
		context.refresh();
		return context.getBean(beanName, beanClass);
	}
	
	/**
	 * Captures the interceptor registry from its constructor, which Spring runs during the provider
	 * rebuild. Static because Spring, not the test, constructs it.
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
	
	@R4Provider
	public static class ReadablePatientProvider implements IResourceProvider {
		
		@Read
		public Patient read(@IdParam IdType id) {
			return new Patient();
		}
		
		@Override
		public Class<? extends IBaseResource> getResourceType() {
			return Patient.class;
		}
	}
	
	@FhirInterceptor
	public static class ContributedInterceptor {
		
		boolean sawRequest;
		
		@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			sawRequest = true;
			return true;
		}
	}
	
	/** Declares the order on the type, which is what HAPI reads when no hook overrides it. */
	@FhirInterceptor
	@Interceptor(order = -1)
	public static class JumpsTheQueueInterceptor {
		
		@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return true;
		}
	}
	
	/** The hook order wins over the type's, so a type-level check alone would let this through. */
	@FhirInterceptor
	public static class HookJumpsTheQueueInterceptor {
		
		@Hook(value = Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED, order = -1)
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return true;
		}
	}
	
	/** HAPI rejects a hook that returns anything but boolean or void, throwing as it is registered. */
	@FhirInterceptor
	public static class BadHookSignatureInterceptor {
		
		@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
		public String incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return "not a boolean";
		}
	}
	
	/** Annotated, but nothing for HAPI to scan - the shape an interface-proxied bean presents. */
	@FhirInterceptor
	public static class HooklessInterceptor {
		
		public boolean incomingRequest(HttpServletRequest request, HttpServletResponse response) {
			return true;
		}
	}
	
	@R3Provider
	public static class R3PatientProvider implements IResourceProvider {
		
		@Read
		public org.hl7.fhir.dstu3.model.Patient read(@IdParam org.hl7.fhir.dstu3.model.IdType id) {
			return new org.hl7.fhir.dstu3.model.Patient();
		}
		
		@Override
		public Class<? extends IBaseResource> getResourceType() {
			return org.hl7.fhir.dstu3.model.Patient.class;
		}
	}
	
	/**
	 * Records, at each unregistration, whether an authentication interceptor other than the one being
	 * removed is still on the server.
	 */
	static class AuthenticationWatchingInterceptorService extends InterceptorService {
		
		final List<Boolean> authenticatedAtEachUnregister = new ArrayList<>();
		
		@Override
		public boolean unregisterInterceptor(Object theInterceptor) {
			authenticatedAtEachUnregister.add(getAllRegisteredInterceptors().stream()
			        .anyMatch(i -> i != theInterceptor && i instanceof RequireAuthenticationInterceptor));
			return super.unregisterInterceptor(theInterceptor);
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
