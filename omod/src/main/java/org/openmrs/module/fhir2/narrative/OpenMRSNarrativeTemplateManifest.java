/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

/*
 * This class is derived and modified from the HAPI FHIR Core Library under the
 * terms of the Apache License, Version 2.0. You may obtain a copy of the
 * License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * The portions of this class that have not been modified by OpenMRS are
 * Copyright (C) 2014 - 2020 University Health Network.
 */

package org.openmrs.module.fhir2.narrative;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative2.INarrativeTemplate;
import ca.uhn.fhir.narrative2.INarrativeTemplateManifest;
import ca.uhn.fhir.narrative2.TemplateTypeEnum;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.util.OpenmrsUtil;

@Slf4j
public class OpenMRSNarrativeTemplateManifest implements INarrativeTemplateManifest {
	
	private final Map<String, List<OpenMRSNarrativeTemplate>> styleToResourceTypeToTemplate;
	
	private final Map<String, List<OpenMRSNarrativeTemplate>> styleToDatatypeToTemplate;
	
	private final Map<String, List<OpenMRSNarrativeTemplate>> styleToNameToTemplate;
	
	private OpenMRSNarrativeTemplateManifest(Collection<OpenMRSNarrativeTemplate> templates) {
		Map<String, List<OpenMRSNarrativeTemplate>> resourceTypeToTemplate = new HashMap<>();
		Map<String, List<OpenMRSNarrativeTemplate>> datatypeToTemplate = new HashMap<>();
		Map<String, List<OpenMRSNarrativeTemplate>> nameToTemplate = new HashMap<>();
		
		for (OpenMRSNarrativeTemplate nextTemplate : templates) {
			nameToTemplate.computeIfAbsent(nextTemplate.getTemplateName(), t -> new ArrayList<>()).add(nextTemplate);
			for (String nextResourceType : nextTemplate.getAppliesToResourceTypes()) {
				resourceTypeToTemplate.computeIfAbsent(nextResourceType.toUpperCase(), t -> new ArrayList<>())
				        .add(nextTemplate);
			}
			for (String nextDataType : nextTemplate.getAppliesToDataTypes()) {
				datatypeToTemplate.computeIfAbsent(nextDataType.toUpperCase(), t -> new ArrayList<>()).add(nextTemplate);
			}
		}
		
		styleToNameToTemplate = makeImmutable(nameToTemplate);
		styleToResourceTypeToTemplate = makeImmutable(resourceTypeToTemplate);
		styleToDatatypeToTemplate = makeImmutable(datatypeToTemplate);
	}
	
	@Override
	public List<INarrativeTemplate> getTemplateByResourceName(FhirContext fhirContext, EnumSet<TemplateTypeEnum> styles,
	        String resourceName) {
		return getFromMap(styles, resourceName.toUpperCase(), styleToResourceTypeToTemplate);
	}
	
	@Override
	public List<INarrativeTemplate> getTemplateByName(FhirContext fhirContext, EnumSet<TemplateTypeEnum> styles,
	        String name) {
		return getFromMap(styles, name, styleToNameToTemplate);
	}
	
	@Override
	public List<INarrativeTemplate> getTemplateByElement(FhirContext fhirContext, EnumSet<TemplateTypeEnum> styles,
	        IBase element) {
		if (element instanceof IBaseResource) {
			String resourceName = fhirContext.getResourceDefinition((IBaseResource) element).getName();
			return getTemplateByResourceName(fhirContext, styles, resourceName);
		}
		String datatypeName = fhirContext.getElementDefinition(element.getClass()).getName();
		return getFromMap(styles, datatypeName.toUpperCase(), styleToDatatypeToTemplate);
	}
	
	public static OpenMRSNarrativeTemplateManifest forManifestFileLocation(Collection<String> propertyFilePaths)
	        throws IOException {
		log.debug("Loading narrative properties file(s): {}", propertyFilePaths);
		List<String> manifestFileContents = new ArrayList<>(propertyFilePaths.size());
		for (String next : propertyFilePaths) {
			String resource = loadResource(next);
			manifestFileContents.add(resource);
		}
		return forManifestFileContents(manifestFileContents);
	}
	
	public static OpenMRSNarrativeTemplateManifest forManifestFileContents(Collection<String> resources) throws IOException {
		List<OpenMRSNarrativeTemplate> templates = new ArrayList<>();
		for (String next : resources) {
			templates.addAll(loadProperties(next));
		}
		return new OpenMRSNarrativeTemplateManifest(templates);
	}
	
	private static Collection<OpenMRSNarrativeTemplate> loadProperties(String manifestText) throws IOException {
		Map<String, OpenMRSNarrativeTemplate> nameToTemplate = new HashMap<>();
		
		Properties file = new Properties();
		
		file.load(new StringReader(manifestText));
		for (Object nextKeyObj : file.keySet()) {
			String nextKey = (String) nextKeyObj;
			Validate.isTrue(StringUtils.countMatches(nextKey, ".") == 1, "Invalid narrative property file key: %s", nextKey);
			String name = nextKey.substring(0, nextKey.indexOf('.'));
			Validate.notBlank(name, "Invalid narrative property file key: %s", nextKey);
			
			OpenMRSNarrativeTemplate nextTemplate = nameToTemplate.computeIfAbsent(name,
			    t -> new OpenMRSNarrativeTemplate().setTemplateName(name));
			
			Validate.isTrue(!nextKey.endsWith(".class"),
			    "Narrative manifest does not support specifying templates by class name - Use \"[name].resourceType=[resourceType]\" instead");
			
			if (nextKey.endsWith(".profile")) {
				String profile = file.getProperty(nextKey);
				if (isNotBlank(profile)) {
					nextTemplate.addAppliesToProfile(profile);
				}
			} else if (nextKey.endsWith(".resourceType")) {
				String resourceType = file.getProperty(nextKey);
				Arrays.stream(resourceType.split(",")).map(String::trim).filter(StringUtils::isNotBlank)
				        .forEach(nextTemplate::addAppliesToResourceType);
			} else if (nextKey.endsWith(".dataType")) {
				String dataType = file.getProperty(nextKey);
				Arrays.stream(dataType.split(",")).map(String::trim).filter(StringUtils::isNotBlank)
				        .forEach(nextTemplate::addAppliesToDatatype);
			} else if (nextKey.endsWith(".style")) {
				String templateTypeName = file.getProperty(nextKey).toUpperCase();
				TemplateTypeEnum templateType = TemplateTypeEnum.valueOf(templateTypeName);
				nextTemplate.setTemplateType(templateType);
			} else if (nextKey.endsWith(".contextPath")) {
				String contextPath = file.getProperty(nextKey);
				nextTemplate.setContextPath(contextPath);
			} else if (nextKey.endsWith(".narrative")) {
				String narrativePropName = name + ".narrative";
				String narrativeName = file.getProperty(narrativePropName);
				if (isNotBlank(narrativeName)) {
					nextTemplate.setTemplateFileName(narrativeName);
				}
			} else if (nextKey.endsWith(".title")) {
				log.debug("Ignoring title property as narrative generator no longer generates titles: {}", nextKey);
			} else {
				throw new ConfigurationException("Invalid property name: " + nextKey
				        + " - the key must end in one of the expected extensions "
				        + "'.profile', '.resourceType', '.dataType', '.style', '.contextPath', '.narrative', '.title'");
			}
		}
		return nameToTemplate.values();
	}
	
	static String loadResource(String name) throws IOException {
		if (name.startsWith("classpath:")) {
			String cpName = name.substring("classpath:".length());
			try (InputStream resource = DefaultThymeleafNarrativeGenerator.class.getResourceAsStream(cpName)) {
				if (resource == null) {
					try (InputStream resource2 = DefaultThymeleafNarrativeGenerator.class
					        .getResourceAsStream("/" + cpName)) {
						if (resource2 == null) {
							throw new IOException("Can not find '" + cpName + "' on classpath");
						}
						return IOUtils.toString(resource2, Charsets.UTF_8);
					}
				}
				return IOUtils.toString(resource, Charsets.UTF_8);
			}
		} else if (name.startsWith("file:")) {
			File file = new File(name.substring("file:".length()));
			if (!file.exists()) {
				throw new IOException("File not found: " + file.getAbsolutePath());
			}
			try (FileInputStream inputStream = new FileInputStream(file)) {
				return IOUtils.toString(inputStream, Charsets.UTF_8);
			}
		} else if (name.startsWith("openmrs:")) {
			File file = new File(OpenmrsUtil.getApplicationDataDirectory(), name.substring("openmrs:".length()));
			if (!file.exists()) {
				throw new IOException("File not found: " + file.getAbsolutePath());
			}
			try (FileInputStream inputStream = new FileInputStream(file)) {
				return IOUtils.toString(inputStream, Charsets.UTF_8);
			}
		} else {
			throw new IOException("Invalid resource name: '" + name + "' (must start with classpath: or file: or openmrs:)");
		}
	}
	
	private static <T> List<INarrativeTemplate> getFromMap(EnumSet<TemplateTypeEnum> styles, T key,
	        Map<T, List<OpenMRSNarrativeTemplate>> map) {
		return map.getOrDefault(key, Collections.emptyList()).stream().filter(t -> styles.contains(t.getTemplateType()))
		        .collect(Collectors.toList());
	}
	
	private static <T> Map<T, List<OpenMRSNarrativeTemplate>> makeImmutable(
	        Map<T, List<OpenMRSNarrativeTemplate>> styleToResourceTypeToTemplate) {
		styleToResourceTypeToTemplate.replaceAll((key, value) -> Collections.unmodifiableList(value));
		return Collections.unmodifiableMap(styleToResourceTypeToTemplate);
	}
}
