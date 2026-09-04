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

import static org.openmrs.module.fhir2.FhirConstants.FHIR2_MODULE_ID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.BasePagingProvider;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.ReflectionUtil;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.fhir2.FhirActivator;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.annotations.FhirInterceptor;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.spi.ModuleLifecycleListener;
import org.openmrs.module.fhir2.narrative.OpenmrsThymeleafNarrativeGenerator;
import org.openmrs.module.fhir2.web.authentication.RequireAuthenticationInterceptor;
import org.openmrs.module.fhir2.web.util.DisableCacheInterceptor;
import org.openmrs.module.fhir2.web.util.NarrativeUtils;
import org.openmrs.module.fhir2.web.util.SummaryInterceptor;
import org.openmrs.module.fhir2.web.util.SupportMergePatchInterceptor;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FhirRestServlet extends RestfulServer implements ModuleLifecycleListener {
	
	private static final long serialVersionUID = 2L;
	
	private static final List<String> DEFAULT_NARRATIVE_FILES = Arrays.asList(FhirConstants.OPENMRS_NARRATIVES_PROPERTY_FILE,
	    FhirConstants.HAPI_NARRATIVES_PROPERTY_FILE);
	
	@Setter(value = AccessLevel.PUBLIC, onMethod_ = { @Qualifier("adminService"), @Autowired })
	private AdministrationService administrationService;
	
	@Setter(value = AccessLevel.PUBLIC, onMethod_ = { @Autowired })
	private FhirGlobalPropertyService globalPropertyService;
	
	@Setter(value = AccessLevel.PUBLIC, onMethod_ = { @Qualifier("hapiLoggingInterceptor"), @Autowired })
	private LoggingInterceptor loggingInterceptor;
	
	private boolean started = false;
	
	/**
	 * What the last {@link #registerInterceptors()} put on the server, so the next one can take it back
	 * off. Narrower than unregistering everything: an interceptor some other code registered directly
	 * on this servlet is not ours to drop.
	 */
	private final List<Object> registeredInterceptors = new ArrayList<>();
	
	@Setter(value = AccessLevel.PUBLIC, onMethod_ = { @Qualifier("messageSourceService"), @Autowired })
	private MessageSource messageSource;
	
	private final GlobalPropertyListener fhirRestServletListener = new GlobalPropertyListener() {
		
		@Override
		public boolean supportsPropertyName(String propertyName) {
			return FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE.equals(propertyName)
			        || FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE.equals(propertyName);
		}
		
		@Override
		public void globalPropertyChanged(GlobalProperty newValue) {
			int value;
			try {
				value = Integer.parseInt(newValue.getPropertyValue());
			}
			catch (NumberFormatException e) {
				globalPropertyDeleted(newValue.getProperty());
				return;
			}
			
			switch (newValue.getProperty()) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					((BasePagingProvider) getPagingProvider()).setDefaultPageSize(value);
					break;
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					((BasePagingProvider) getPagingProvider()).setMaximumPageSize(value);
					break;
			}
		}
		
		@Override
		public void globalPropertyDeleted(String propertyName) {
			switch (propertyName) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					((BasePagingProvider) getPagingProvider()).setDefaultPageSize(10);
					break;
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					((BasePagingProvider) getPagingProvider()).setMaximumPageSize(100);
					break;
			}
		}
	};
	
	// TODO: Why does the formatter screw up only this method?
	//@formatter:off
	@Override
	protected void initialize() {
		// we need to load the application context for the FHIR2 module
		Module fhirModule = ModuleFactory.getModuleById(FHIR2_MODULE_ID);
		if (fhirModule != null) {
			FhirActivator activator = (FhirActivator) fhirModule.getModuleActivator();
			// listen to the module lifecycle calls
			activator.addModuleLifecycleListener(this);
		}

		// globalPropertyService is chosen reasonably arbitrarily to ensure we don't overwrite classes explicitly added
		// by tests
		if (globalPropertyService == null) {
			// ensure properties for this class are properly injected
			autoInject();
			administrationService.addGlobalPropertyListener(fhirRestServletListener);
		}

		setPagingProvider(createPagingProvider());
		setDefaultResponseEncoding(EncodingEnum.JSON);

		registerInterceptors();

		String narrativesOverridePropertyFile = NarrativeUtils.getValidatedPropertiesFilePath(
				globalPropertyService.getGlobalProperty(FhirConstants.NARRATIVES_OVERRIDE_PROPERTY_FILE, null));

		List<String> narrativePropertiesFiles;
		if (narrativesOverridePropertyFile != null) {
			narrativePropertiesFiles = new ArrayList<>(3);
			narrativePropertiesFiles.add(narrativesOverridePropertyFile);
			narrativePropertiesFiles.addAll(DEFAULT_NARRATIVE_FILES);
		} else {
			narrativePropertiesFiles = DEFAULT_NARRATIVE_FILES;
		}

		getFhirContext()
				.setNarrativeGenerator(new OpenmrsThymeleafNarrativeGenerator(messageSource, narrativePropertiesFiles));

		started = true;
	}
	//@formatter:on
	
	/**
	 * Builds the servlet's interceptor set: the module's own interceptors, then any bean another module
	 * contributes with {@link FhirInterceptor}. Safe to call on a servlet that is already serving,
	 * which is what {@link #refreshed()} does - the new set is registered before the previous one is
	 * dropped, so a concurrent request never finds the servlet without
	 * {@link RequireAuthenticationInterceptor}.
	 * <p>
	 * Synchronized because it reads and rewrites {@code registeredInterceptors} around calls that
	 * mutate HAPI's registry: {@link #initialize()} runs on the container's init thread and
	 * {@link #refreshed()} on whichever thread drove the context refresh.
	 */
	protected synchronized void registerInterceptors() {
		List<Object> previous = new ArrayList<>(registeredInterceptors);
		List<Object> current = new ArrayList<>();
		
		try {
			// these carry FhirConstants.BUILT_IN_INTERCEPTOR_ORDER, so they dispatch ahead of a contributed
			// bean whatever order the two happen to be registered in - which is only enforced for beans
			// this method registers, not for interceptors put on the servlet by other means
			for (Object interceptor : Arrays.asList(new RequireAuthenticationInterceptor(), loggingInterceptor,
			    new DisableCacheInterceptor(), new SummaryInterceptor(), new SupportMergePatchInterceptor())) {
				registerInterceptor(interceptor);
				current.add(interceptor);
			}
			
			// drop the previous set only now that its replacement is in place, and only the entries the new
			// set did not carry over by identity - unregistering one HAPI has kept would leave a hole
			Set<Object> retained = Collections.newSetFromMap(new IdentityHashMap<>());
			retained.addAll(current);
			previous.stream().filter(interceptor -> !retained.contains(interceptor))
			        .forEach(getInterceptorService()::unregisterInterceptor);
			
			registerContributedInterceptors(current);
		}
		finally {
			// in the finally so that a throw cannot leave the field naming interceptors that are no longer
			// on the server: the next call would skip unregistering the ones that are, orphaning them
			registeredInterceptors.clear();
			registeredInterceptors.addAll(current);
		}
	}
	
	/**
	 * Registers each {@link FhirInterceptor} bean, adding the ones HAPI accepts to {@code current}.
	 * Contributed beans are third-party code, so one that cannot be built or scanned is reported and
	 * skipped rather than allowed to abandon the rebuild half-done.
	 */
	private void registerContributedInterceptors(List<Object> current) {
		ConfigurableApplicationContext ctx = getModuleApplicationContext();
		if (ctx == null) {
			return;
		}
		
		Map<String, Object> contributed;
		try {
			contributed = ctx.getBeansWithAnnotation(FhirInterceptor.class);
		}
		catch (Exception e) {
			log.error("Could not read the contributed FHIR interceptors from the Spring context; none of them will run", e);
			return;
		}
		
		for (Map.Entry<String, Object> entry : contributed.entrySet()) {
			String beanName = entry.getKey();
			Object interceptor = entry.getValue();
			
			try {
				String orderViolation = describeOrderViolation(interceptor);
				
				if (orderViolation != null) {
					log.error(
					    "Not registering contributed FHIR interceptor bean {} ({}): {}, which would run it ahead of this "
					            + "module's own interceptors, including authentication. Contributed interceptors must "
					            + "order at or above {}.",
					    beanName, interceptor.getClass().getName(), orderViolation, Interceptor.DEFAULT_ORDER);
				} else if (getInterceptorService().registerInterceptor(interceptor)) {
					current.add(interceptor);
					log.info("Registered contributed FHIR interceptor bean {} ({})", beanName,
					    interceptor.getClass().getName());
				} else {
					log.error(
					    "Contributed FHIR interceptor bean {} ({}) declares no @Hook method HAPI can see and will not "
					            + "run. A bean behind an interface-based Spring proxy hides its hooks; annotate a concrete "
					            + "class and keep it clear of interface-based AOP.",
					    beanName, interceptor.getClass().getName());
				}
			}
			catch (Exception e) {
				log.error("Could not register contributed FHIR interceptor bean {} ({}); it will not run", beanName,
				    interceptor.getClass().getName(), e);
			}
		}
	}
	
	/**
	 * Where a contributed interceptor declares an order that would sort it ahead of this module's own,
	 * phrased for a log message, or null if it declares none. HAPI orders the hooks for a pointcut by
	 * the {@code order} on {@link Interceptor} or {@link Hook}, and this module's interceptors hold
	 * {@link FhirConstants#BUILT_IN_INTERCEPTOR_ORDER}, so a contributed interceptor only has to stay
	 * at or above {@link Interceptor#DEFAULT_ORDER} to run after them. Screening the beans this servlet
	 * registers is all this does; it says nothing about interceptors registered elsewhere.
	 */
	private static String describeOrderViolation(Object interceptor) {
		// mirrors BaseInterceptorService#scanInterceptorForHookMethods, concrete class included, so that
		// this sees the same orders HAPI will
		Class<?> type = interceptor.getClass();
		
		Interceptor typeAnnotation = type.getAnnotation(Interceptor.class);
		if (typeAnnotation != null && typeAnnotation.order() < Interceptor.DEFAULT_ORDER) {
			return "the class declares @Interceptor(order = " + typeAnnotation.order() + ")";
		}
		
		for (Method method : ReflectionUtil.getDeclaredMethods(type, true)) {
			Hook hook = MethodUtils.getAnnotation(method, Hook.class, true, true);
			if (hook != null && hook.order() < Interceptor.DEFAULT_ORDER) {
				return method.getName() + "() declares @Hook(order = " + hook.order() + ")";
			}
		}
		
		return null;
	}
	
	protected ConfigurableApplicationContext getModuleApplicationContext() {
		return FhirActivator.getApplicationContext();
	}
	
	protected Class<? extends Annotation> getResourceProviderAnnotation() {
		return R4Provider.class;
	}
	
	@Override
	protected String createPoweredByHeaderComponentName() {
		return FhirConstants.OPENMRS_FHIR_SERVER_NAME;
	}
	
	@Override
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath
		        .substring(escapedLength(servletContextPath) + escapedLength(servletPath) + escapedLength("/fhir2Servlet"));
	}
	
	@Override
	protected void service(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		super.service(theReq, theResp);
	}
	
	@Override
	@Autowired
	@Qualifier("fhirR4")
	public void setFhirContext(FhirContext theFhirContext) {
		super.setFhirContext(theFhirContext);
	}
	
	@Override
	@Autowired
	@R4Provider
	public void setResourceProviders(Collection<IResourceProvider> theProviders) {
		super.setResourceProviders(theProviders);
	}
	
	@Override
	@Autowired
	public void setServerAddressStrategy(IServerAddressStrategy theServerAddressStrategy) {
		super.setServerAddressStrategy(theServerAddressStrategy);
	}
	
	private BasePagingProvider createPagingProvider() {
		int defaultPageSize = globalPropertyService.getGlobalPropertyAsInteger(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE,
		    10);
		int maximumPageSize = globalPropertyService.getGlobalPropertyAsInteger(FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE,
		    100);
		
		BasePagingProvider pagingProvider = new FifoMemoryPagingProvider(100);
		pagingProvider.setDefaultPageSize(defaultPageSize);
		pagingProvider.setMaximumPageSize(maximumPageSize);
		return pagingProvider;
	}
	
	protected void autoInject() {
		final ConfigurableApplicationContext ctx = getModuleApplicationContext();
		if (ctx != null) {
			AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
			bpp.setBeanFactory(ctx.getAutowireCapableBeanFactory());
			bpp.processInjection(this);
		}
	}
	
	@Override
	public void willRefresh() {
		if (fhirRestServletListener != null) {
			administrationService.removeGlobalPropertyListener(fhirRestServletListener);
		}
	}
	
	@Override
	public void refreshed() {
		if (started) {
			final ConfigurableApplicationContext ctx = getModuleApplicationContext();
			if (ctx != null) {
				unregisterAllProviders();
				
				// load the resource providers from the Spring context
				Set<String> validBeanNames = Arrays.stream(ctx.getBeanNamesForAnnotation(getResourceProviderAnnotation()))
				        .collect(Collectors.toSet());
				// registerProviders, not setResourceProviders: the setter leaves no method bindings
				registerProviders(ctx.getBeansOfType(IResourceProvider.class).entrySet().stream()
				        .filter(entry -> validBeanNames.contains(entry.getKey())).map(Map.Entry::getValue)
				        .collect(Collectors.toList()));
				
				setLoggingInterceptor(ctx.getBean("hapiLoggingInterceptor", LoggingInterceptor.class));
				setAdministrationService(ctx.getBean("adminService", AdministrationService.class));
				setGlobalPropertyService(ctx.getBean(FhirGlobalPropertyService.class));
				setServerAddressStrategy(ctx.getBean(IServerAddressStrategy.class));
				setPagingProvider(createPagingProvider());
				
				administrationService.addGlobalPropertyListener(fhirRestServletListener);
				
				// last, so that a throw from any of the lookups above leaves the interceptors from the
				// previous context in place rather than none at all
				registerInterceptors();
			}
		}
	}
	
	@Override
	public void stopped() {
		if (fhirRestServletListener != null) {
			administrationService.removeGlobalPropertyListener(fhirRestServletListener);
		}
	}
	
	@Override
	public void destroy() {
		try {
			administrationService.removeGlobalPropertyListener(fhirRestServletListener);
		}
		catch (Exception ignored) {
			
		}
		
		try {
			FhirActivator activator = (FhirActivator) ModuleFactory.getModuleById("fhir2").getModuleActivator();
			if (activator != null) {
				activator.removeModuleLifecycleLister(this);
			}
		}
		catch (Exception ignored) {
			
		}
		
		super.destroy();
	}
}
