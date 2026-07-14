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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import org.openmrs.util.OpenmrsUtil;

/**
 * Loads narrative template property files into a HAPI FHIR {@link NarrativeTemplateManifest}.
 * <p>
 * HAPI's manifest loader only understands the {@code classpath:} and {@code file:} prefixes. This
 * facade additionally supports the OpenMRS-specific {@code openmrs:} prefix (accepted by
 * {@code NarrativeUtils#getValidatedPropertiesFilePath} for the
 * {@code fhir2.narrativesOverridePropertyFile} global property), resolving it against the OpenMRS
 * application data directory before delegating to HAPI.
 */
public final class OpenmrsNarrativeTemplateManifest {
	
	private static final String OPENMRS_PREFIX = "openmrs:";
	
	private OpenmrsNarrativeTemplateManifest() {
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(Collection<String> propertyFilePaths)
	        throws IOException {
		List<String> resolvedPaths = new ArrayList<>(propertyFilePaths.size());
		for (String path : propertyFilePaths) {
			resolvedPaths.add(resolveOpenmrsPath(path));
		}
		return NarrativeTemplateManifest.forManifestFileLocation(resolvedPaths);
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(String... propertyFilePaths) throws IOException {
		return forManifestFileLocation(Arrays.asList(propertyFilePaths));
	}
	
	/**
	 * Rewrites an {@code openmrs:<relative path>} location to a {@code file:} location under the
	 * OpenMRS application data directory. Paths using any other prefix are returned unchanged for HAPI
	 * to resolve.
	 */
	private static String resolveOpenmrsPath(String path) {
		if (path != null && path.startsWith(OPENMRS_PREFIX)) {
			File file = new File(OpenmrsUtil.getApplicationDataDirectory(), path.substring(OPENMRS_PREFIX.length()));
			return "file:" + file.getAbsolutePath();
		}
		
		return path;
	}
}
