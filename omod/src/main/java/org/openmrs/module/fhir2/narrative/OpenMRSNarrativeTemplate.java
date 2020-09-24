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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ca.uhn.fhir.narrative2.INarrativeTemplate;
import ca.uhn.fhir.narrative2.TemplateTypeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.instance.model.api.IBase;

public class OpenMRSNarrativeTemplate implements INarrativeTemplate {
	
	private String contextPath;
	
	private final Set<String> appliesToProfiles = new HashSet<>();
	
	private final Set<String> appliesToResourceTypes = new HashSet<>();
	
	private final Set<Class<? extends IBase>> appliesToResourceClasses = new HashSet<>();
	
	private TemplateTypeEnum templateType = TemplateTypeEnum.THYMELEAF;
	
	private String templateName;
	
	private String templateFilename;
	
	private final Set<String> appliesToDatatypes = new HashSet<>();
	
	@Override
	public String getContextPath() {
		return this.contextPath;
	}
	
	protected void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	@Override
	public Set<String> getAppliesToProfiles() {
		return Collections.unmodifiableSet(this.appliesToProfiles);
	}
	
	protected void addAppliesToProfile(String profile) {
		this.appliesToProfiles.add(profile);
	}
	
	@Override
	public Set<String> getAppliesToResourceTypes() {
		return Collections.unmodifiableSet(this.appliesToResourceTypes);
	}
	
	protected void addAppliesToResourceType(String resourceType) {
		this.appliesToResourceTypes.add(resourceType);
	}
	
	@Override
	public Set<Class<? extends IBase>> getAppliesToResourceClasses() {
		return Collections.unmodifiableSet(this.appliesToResourceClasses);
	}
	
	@Override
	public TemplateTypeEnum getTemplateType() {
		return this.templateType;
	}
	
	protected void setTemplateType(TemplateTypeEnum templateType) {
		this.templateType = templateType;
	}
	
	@Override
	public String getTemplateName() {
		return this.templateName;
	}
	
	protected OpenMRSNarrativeTemplate setTemplateName(String templateName) {
		this.templateName = templateName;
		return this;
	}
	
	@Override
	public String getTemplateText() {
		try {
			return OpenMRSNarrativeTemplateManifest.loadResource(this.templateFilename);
		}
		catch (IOException e) {
			throw new InternalErrorException(e);
		}
	}
	
	protected void setTemplateFileName(String templateFileName) {
		this.templateFilename = templateFileName;
	}
	
	public Set<String> getAppliesToDataTypes() {
		return Collections.unmodifiableSet(this.appliesToDatatypes);
	}
	
	protected void addAppliesToDatatype(String dataType) {
		this.appliesToDatatypes.add(dataType);
	}
}
