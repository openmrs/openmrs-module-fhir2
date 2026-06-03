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

import java.io.IOException;
import java.util.Collection;

import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;

/**
 * Loads narrative template property files into a HAPI FHIR {@link NarrativeTemplateManifest}. Kept
 * as a thin facade so callers don't depend directly on HAPI's manifest factory.
 */
public final class OpenmrsNarrativeTemplateManifest {
	
	private OpenmrsNarrativeTemplateManifest() {
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(Collection<String> propertyFilePaths)
	        throws IOException {
		return NarrativeTemplateManifest.forManifestFileLocation(propertyFilePaths);
	}
	
	public static NarrativeTemplateManifest forManifestFileLocation(String... propertyFilePaths) throws IOException {
		return NarrativeTemplateManifest.forManifestFileLocation(propertyFilePaths);
	}
}
