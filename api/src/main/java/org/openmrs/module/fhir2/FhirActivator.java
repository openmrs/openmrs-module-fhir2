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

import static org.openmrs.module.fhir2.FhirConstants.FHIR2_MODULE_ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.spi.ServiceClassLoader;
import org.openmrs.module.fhir2.api.translators.FhirTranslator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
@Slf4j
@Component
@SuppressWarnings("unused")
public class FhirActivator extends BaseModuleActivator implements ApplicationContextAware {
	
	@Getter
	private static ConfigurableApplicationContext applicationContext;
	
	private final Map<String, Set<Class<?>>> services = new HashMap<>();
	
	private boolean started = false;
	
	@Override
	public void started() {
		if (applicationContext == null) {
			throw new ModuleException("Cannot load FHIR2 module as the main application context is not available");
		}
		
		loadModules();
		started = true;
		log.info("Started FHIR");
	}
	
	@Override
	public void willRefreshContext() {
		unloadModules();
	}
	
	@Override
	public void contextRefreshed() {
		if (!started) {
			return;
		}
		
		loadModules();
	}
	
	@Override
	public void willStop() {
		unloadModules();
	}
	
	@Override
	public void stopped() {
		started = false;
		log.info("Shutdown FHIR");
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			FhirActivator.applicationContext = (ConfigurableApplicationContext) applicationContext;
		}
	}
	
	protected void loadModules() {
		ModuleFactory.getLoadedModules().stream()
		        // only try to load services from modules that have the FHIR2 module available on their ModuleClasspath
		        .filter(m -> m.getRequiredModuleVersion(FHIR2_MODULE_ID) != null
		                || m.getAwareOfModuleVersion(FHIR2_MODULE_ID) != null)
		        .forEach(this::loadModuleInternal);
	}
	
	protected void loadModule(Module module) {
		if (!services.containsKey(module.getName())) {
			loadModuleInternal(module);
		}
	}
	
	protected void unloadModules() {
		services.clear();
	}
	
	protected void unloadModule(String moduleName) {
		services.remove(moduleName);
	}
	
	private void loadModuleInternal(Module module) {
		ClassLoader cl = ModuleFactory.getModuleClassLoader(module);
		
		Set<Class<?>> moduleServices = services.computeIfAbsent(module.getName(),
		    k -> Collections.newSetFromMap(new WeakHashMap<>()));
		Stream.of(FhirDao.class, FhirTranslator.class, FhirService.class, IResourceProvider.class)
		        .flatMap(c -> new ServiceClassLoader<>(c, cl).load().stream()).filter(c -> {
			        boolean result;
			        try {
				        result = c.getAnnotation(Component.class) != null;
			        }
			        catch (NullPointerException e) {
				        result = false;
			        }
			        
			        if (!result) {
				        log.warn("Skipping {} as it is not an annotated Spring Component", c);
			        }
			        
			        return result;
		        }).forEach(moduleServices::add);
	}
}
