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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative2.ThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.context.MessageSource;

/**
 * Class for carrying out the task of narrative generation
 */
public class OpenmrsThymeleafNarrativeGenerator extends ThymeleafNarrativeGenerator {
	
	private boolean isInitialized;
	
	@Getter
	private List<String> propertyFiles;
	
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, String... propertyFiles) {
		this(messageSource, Arrays.asList(propertyFiles));
	}
	
	/**
	 * Constructor for OpenMRSThymeleafNarrativeGenerator
	 * 
	 * @param propertyFiles property files to define the narratives for this narrative generator
	 */
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, List<String> propertyFiles) {
		super();
		setMessageResolver(new OpenmrsMessageResolver(messageSource));
		setPropertyFiles(propertyFiles);
	}
	
	/**
	 * populates the resource narratives specified in property file with resource property values
	 * 
	 * @param theFhirContext
	 * @param theResource
	 * @return
	 */
	@Override
	public boolean populateResourceNarrative(FhirContext theFhirContext, IBaseResource theResource) {
		if (!isInitialized) {
			initialize();
		}
		
		return super.populateResourceNarrative(theFhirContext, theResource);
	}
	
	/**
	 * Sets property file paths for the narrative generator
	 *
	 * @param propertyFiles
	 */
	public void setPropertyFiles(List<String> propertyFiles) {
		Validate.notNull(propertyFiles, "Property file can not be null");
		this.propertyFiles = propertyFiles;
	}
	
	private synchronized void initialize() {
		if (!isInitialized) {
			List<String> propertyFile = getPropertyFiles();
			try {
				OpenmrsNarrativeTemplateManifest manifest = OpenmrsNarrativeTemplateManifest
				        .forManifestFileLocation(propertyFile);
				setManifest(manifest);
			}
			catch (IOException e) {
				throw new InternalErrorException(e);
			}
			
			isInitialized = true;
		}
	}
}
