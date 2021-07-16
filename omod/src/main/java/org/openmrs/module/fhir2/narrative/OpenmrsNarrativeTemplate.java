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

/**
 * Class which defines the narrative template
 */
public class OpenmrsNarrativeTemplate implements INarrativeTemplate {
	
	private String contextPath;
	
	private final Set<String> appliesToProfiles = new HashSet<>();
	
	private final Set<String> appliesToResourceTypes = new HashSet<>();
	
	private final Set<Class<? extends IBase>> appliesToClasses = new HashSet<>();
	
	private TemplateTypeEnum templateType = TemplateTypeEnum.THYMELEAF;
	
	private String templateName;
	
	private String templateFilename;
	
	private final Set<String> appliesToDatatypes = new HashSet<>();
	
	/**
	 * @return the context path of template
	 */
	@Override
	public String getContextPath() {
		return this.contextPath;
	}
	
	protected void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	/**
	 * @return string set of profiles which the template is applicable to
	 */
	@Override
	public Set<String> getAppliesToProfiles() {
		return Collections.unmodifiableSet(this.appliesToProfiles);
	}
	
	protected void addAppliesToProfile(String profile) {
		this.appliesToProfiles.add(profile);
	}
	
	/**
	 * @return string set of resource types which the template is applicable to
	 */
	@Override
	public Set<String> getAppliesToResourceTypes() {
		return Collections.unmodifiableSet(this.appliesToResourceTypes);
	}
	
	protected void addAppliesToResourceType(String resourceType) {
		this.appliesToResourceTypes.add(resourceType);
	}
	
	/**
	 * @return set of resource classes which the template is applicable to
	 */
	@Override
	public Set<Class<? extends IBase>> getAppliesToClasses() {
		return Collections.unmodifiableSet(appliesToClasses);
	}
	
	void addAppliesToClass(Class<? extends IBase> theAppliesToClass) {
		appliesToClasses.add(theAppliesToClass);
	}
	
	/**
	 * @return the template type
	 */
	@Override
	public TemplateTypeEnum getTemplateType() {
		return this.templateType;
	}
	
	protected void setTemplateType(TemplateTypeEnum templateType) {
		this.templateType = templateType;
	}
	
	/**
	 * @return the template name
	 */
	@Override
	public String getTemplateName() {
		return this.templateName;
	}
	
	protected OpenmrsNarrativeTemplate setTemplateName(String templateName) {
		this.templateName = templateName;
		return this;
	}
	
	/**
	 * @return the template text
	 */
	@Override
	public String getTemplateText() {
		try {
			return OpenmrsNarrativeTemplateManifest.loadResource(this.templateFilename);
		}
		catch (IOException e) {
			throw new InternalErrorException(e);
		}
	}
	
	protected void setTemplateFileName(String templateFileName) {
		this.templateFilename = templateFileName;
	}
	
	/**
	 * @return string set of data types which the template is applicable to
	 */
	public Set<String> getAppliesToDataTypes() {
		return Collections.unmodifiableSet(this.appliesToDatatypes);
	}
	
	protected void addAppliesToDatatype(String dataType) {
		this.appliesToDatatypes.add(dataType);
	}
}
