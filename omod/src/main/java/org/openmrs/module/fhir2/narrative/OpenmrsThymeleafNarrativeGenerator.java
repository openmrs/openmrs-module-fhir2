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
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.narrative.BaseThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.context.MessageSource;

/**
 * Thymeleaf-based narrative generator that resolves messages through the OpenMRS
 * {@link MessageSource} and loads its template manifest through
 * {@link OpenmrsNarrativeTemplateManifest}, so {@code openmrs:}-prefixed property file locations
 * are supported alongside HAPI's {@code classpath:} and {@code file:} prefixes.
 */
public class OpenmrsThymeleafNarrativeGenerator extends BaseThymeleafNarrativeGenerator {
	
	@Getter
	private volatile List<String> propertyFiles;
	
	private volatile NarrativeTemplateManifest manifest;
	
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, String... propertyFiles) {
		this(messageSource, Arrays.asList(propertyFiles));
	}
	
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, List<String> propertyFiles) {
		super();
		setMessageResolver(new OpenmrsMessageResolver(messageSource));
		setPropertyFiles(propertyFiles);
	}
	
	public void setPropertyFiles(List<String> propertyFiles) {
		Validate.notNull(propertyFiles, "Property file can not be null");
		this.propertyFiles = propertyFiles;
		this.manifest = null;
	}
	
	/**
	 * Lazily loads the manifest on first use so that property files are only read (and validated) once
	 * a narrative is actually requested, matching the behaviour of HAPI's own generators.
	 */
	@Override
	protected NarrativeTemplateManifest getManifest() {
		NarrativeTemplateManifest result = manifest;
		if (result == null) {
			synchronized (this) {
				result = manifest;
				if (result == null) {
					try {
						result = OpenmrsNarrativeTemplateManifest.forManifestFileLocation(propertyFiles);
					} catch (IOException e) {
						throw new InternalErrorException(e);
					}
					manifest = result;
				}
			}
		}
		return result;
	}
}
