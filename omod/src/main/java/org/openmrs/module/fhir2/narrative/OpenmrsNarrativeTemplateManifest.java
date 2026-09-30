/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.narrative;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import ca.uhn.fhir.util.ClasspathUtil;
import org.openmrs.util.OpenmrsUtil;

/**
 * Builds a HAPI {@link NarrativeTemplateManifest} from narrative property files, adding support for
 * the OpenMRS-specific {@code openmrs:} location prefix (accepted by
 * {@code NarrativeUtils.getValidatedPropertiesFilePath()} for the
 * {@code fhir2.narrativesOverridePropertyFile} global property) on top of HAPI's {@code classpath:}
 * and {@code file:} prefixes.
 * <p>
 * The prefix is honoured in two places: the location of the manifest file itself, and the
 * {@code <name>.narrative} values inside it that point at template files. HAPI resolves template
 * locations itself when a narrative is rendered and rejects unknown prefixes, so the manifest text
 * is read here, {@code openmrs:} values are rewritten to {@code file:} locations under the OpenMRS
 * application data directory, and the rewritten contents are handed to HAPI.
 */
public final class OpenmrsNarrativeTemplateManifest {
	
	private static final String OPENMRS_PREFIX = "openmrs:";
	
	private static final String CLASSPATH_PREFIX = "classpath:";
	
	private static final String FILE_PREFIX = "file:";
	
	private OpenmrsNarrativeTemplateManifest() {
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(Collection<String> propertyFilePaths)
	        throws IOException {
		List<String> manifestContents = new ArrayList<>(propertyFilePaths.size());
		for (String path : propertyFilePaths) {
			manifestContents.add(resolveTemplateLocations(loadResource(resolveOpenmrsPath(path))));
		}
		return NarrativeTemplateManifest.forManifestFileContents(manifestContents);
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(String... propertyFilePaths) throws IOException {
		return forManifestFileLocation(Arrays.asList(propertyFilePaths));
	}
	
	/**
	 * Rewrites an {@code openmrs:<relative path>} location to a {@code file:} location under the
	 * OpenMRS application data directory. Locations using any other prefix are returned unchanged for
	 * HAPI to resolve.
	 */
	static String resolveOpenmrsPath(String path) {
		if (path != null && path.startsWith(OPENMRS_PREFIX)) {
			File file = new File(OpenmrsUtil.getApplicationDataDirectory(), path.substring(OPENMRS_PREFIX.length()));
			return FILE_PREFIX + file.getAbsolutePath();
		}
		
		return path;
	}
	
	/**
	 * Rewrites every {@code openmrs:} property value in the manifest text (in practice the
	 * {@code <name>.narrative} template locations) so that HAPI can load the templates.
	 */
	private static String resolveTemplateLocations(String manifestText) throws IOException {
		Properties properties = new Properties();
		properties.load(new StringReader(manifestText));
		
		boolean changed = false;
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key);
			if (value != null && value.trim().startsWith(OPENMRS_PREFIX)) {
				properties.setProperty(key, resolveOpenmrsPath(value.trim()));
				changed = true;
			}
		}
		
		if (!changed) {
			return manifestText;
		}
		
		StringWriter writer = new StringWriter();
		properties.store(writer, null);
		return writer.toString();
	}
	
	private static String loadResource(String location) throws IOException {
		if (location.startsWith(CLASSPATH_PREFIX)) {
			return ClasspathUtil.loadResource(location);
		} else if (location.startsWith(FILE_PREFIX)) {
			File file = new File(location.substring(FILE_PREFIX.length()));
			if (!file.exists()) {
				throw new IOException("File not found: " + file.getAbsolutePath());
			}
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		}
		
		throw new IOException("Invalid resource name: '" + location + "' (must start with classpath:, file: or openmrs:)");
	}
}
