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
 * Copyright (C) 2014 - 2022 Smile CDR, Inc. The Thymeleaf bridge logic was
 * inlined here when HAPI 7.x removed ca.uhn.fhir.narrative2.ThymeleafNarrativeGenerator.
 */

package org.openmrs.module.fhir2.narrative;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.narrative2.BaseNarrativeGenerator;
import ca.uhn.fhir.narrative2.INarrativeTemplate;
import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import ca.uhn.fhir.narrative2.TemplateTypeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.context.MessageSource;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.DefaultTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

/**
 * Class for carrying out the task of narrative generation
 */
public class OpenmrsThymeleafNarrativeGenerator extends BaseNarrativeGenerator {
	
	private IMessageResolver myMessageResolver;
	
	private boolean isInitialized;
	
	private NarrativeTemplateManifest myManifest;
	
	@Getter
	private List<String> propertyFiles;
	
	@Override
	protected NarrativeTemplateManifest getManifest() {
		return myManifest;
	}
	
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, String... propertyFiles) {
		this(messageSource, Arrays.asList(propertyFiles));
	}
	
	public OpenmrsThymeleafNarrativeGenerator(MessageSource messageSource, List<String> propertyFiles) {
		super();
		setMessageResolver(new OpenmrsMessageResolver(messageSource));
		setPropertyFiles(propertyFiles);
	}
	
	@Override
	public boolean populateResourceNarrative(FhirContext theFhirContext, IBaseResource theResource) {
		if (!isInitialized) {
			initialize();
		}
		return super.populateResourceNarrative(theFhirContext, theResource);
	}
	
	public void setPropertyFiles(List<String> propertyFiles) {
		Validate.notNull(propertyFiles, "Property file can not be null");
		this.propertyFiles = propertyFiles;
	}
	
	public void setMessageResolver(IMessageResolver theMessageResolver) {
		myMessageResolver = theMessageResolver;
	}
	
	private synchronized void initialize() {
		if (!isInitialized) {
			try {
				myManifest = OpenmrsNarrativeTemplateManifest.forManifestFileLocation(getPropertyFiles());
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}
			isInitialized = true;
		}
	}
	
	@Override
	protected String applyTemplate(FhirContext theFhirContext, INarrativeTemplate theTemplate, IBase theTargetContext) {
		Context context = new Context();
		context.setVariable("resource", theTargetContext);
		context.setVariable("context", theTargetContext);
		context.setVariable("fhirVersion", theFhirContext.getVersion().getVersion().name());
		return getTemplateEngine(theFhirContext).process(theTemplate.getTemplateName(), context);
	}
	
	@Override
	protected EnumSet<TemplateTypeEnum> getStyle() {
		return EnumSet.of(TemplateTypeEnum.THYMELEAF);
	}
	
	private TemplateEngine getTemplateEngine(FhirContext theFhirContext) {
		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(new ProfileResourceResolver(theFhirContext));
		if (myMessageResolver != null) {
			engine.setMessageResolver(myMessageResolver);
		}
		StandardDialect dialect = new StandardDialect() {
			
			@Override
			public Set<IProcessor> getProcessors(String theDialectPrefix) {
				Set<IProcessor> retVal = super.getProcessors(theDialectPrefix);
				retVal.add(new NarrativeTagProcessor(theFhirContext, theDialectPrefix));
				retVal.add(new NarrativeAttributeProcessor(theDialectPrefix, theFhirContext));
				return retVal;
			}
		};
		engine.setDialect(dialect);
		return engine;
	}
	
	private String applyTemplateWithinTag(FhirContext theFhirContext, ITemplateContext theTemplateContext, String theName,
	        String theElement) {
		IEngineConfiguration configuration = theTemplateContext.getConfiguration();
		IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);
		IStandardExpression expression = expressionParser.parseExpression(theTemplateContext, theElement);
		Object elementValueObj = expression.execute(theTemplateContext);
		IBase elementValue = (IBase) elementValueObj;
		if (elementValue == null) {
			return "";
		}
		
		List<INarrativeTemplate> templateOpt;
		if (isNotBlank(theName)) {
			templateOpt = getManifest().getTemplateByName(theFhirContext, getStyle(), theName);
			if (templateOpt.isEmpty()) {
				throw new InternalErrorException(Msg.code(1863) + "Unknown template name: " + theName);
			}
		} else {
			templateOpt = getManifest().getTemplateByElement(theFhirContext, getStyle(), elementValue);
			if (templateOpt.isEmpty()) {
				throw new InternalErrorException(Msg.code(1864) + "No template for type: " + elementValue.getClass());
			}
		}
		
		return applyTemplate(theFhirContext, templateOpt.get(0), elementValue);
	}
	
	private class ProfileResourceResolver extends DefaultTemplateResolver {
		
		private final FhirContext myFhirContext;
		
		private ProfileResourceResolver(FhirContext theFhirContext) {
			myFhirContext = theFhirContext;
		}
		
		@Override
		protected boolean computeResolvable(IEngineConfiguration theConfiguration, String theOwnerTemplate,
		        String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return !getManifest().getTemplateByName(myFhirContext, getStyle(), theTemplate).isEmpty();
		}
		
		@Override
		protected TemplateMode computeTemplateMode(IEngineConfiguration theConfiguration, String theOwnerTemplate,
		        String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return TemplateMode.XML;
		}
		
		@Override
		protected ITemplateResource computeTemplateResource(IEngineConfiguration theConfiguration, String theOwnerTemplate,
		        String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return getManifest().getTemplateByName(myFhirContext, getStyle(), theTemplate).stream().findFirst()
			        .map(t -> new StringTemplateResource(t.getTemplateText()))
			        .orElseThrow(() -> new IllegalArgumentException("Unknown template: " + theTemplate));
		}
		
		@Override
		protected ICacheEntryValidity computeValidity(IEngineConfiguration theConfiguration, String theOwnerTemplate,
		        String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return AlwaysValidCacheEntryValidity.INSTANCE;
		}
	}
	
	private class NarrativeTagProcessor extends AbstractElementTagProcessor {
		
		private final FhirContext myFhirContext;
		
		NarrativeTagProcessor(FhirContext theFhirContext, String dialectPrefix) {
			super(TemplateMode.XML, dialectPrefix, "narrative", true, null, true, 0);
			myFhirContext = theFhirContext;
		}
		
		@Override
		protected void doProcess(ITemplateContext theTemplateContext, IProcessableElementTag theTag,
		        IElementTagStructureHandler theStructureHandler) {
			String name = theTag.getAttributeValue("th:name");
			String element = theTag.getAttributeValue("th:element");
			String appliedTemplate = applyTemplateWithinTag(myFhirContext, theTemplateContext, name, element);
			theStructureHandler.replaceWith(appliedTemplate, false);
		}
	}
	
	private class NarrativeAttributeProcessor extends AbstractAttributeTagProcessor {
		
		private final FhirContext myFhirContext;
		
		NarrativeAttributeProcessor(String theDialectPrefix, FhirContext theFhirContext) {
			super(TemplateMode.XML, theDialectPrefix, null, false, "narrative", true, 0, true);
			myFhirContext = theFhirContext;
		}
		
		@Override
		protected void doProcess(ITemplateContext theContext, IProcessableElementTag theTag, AttributeName theAttributeName,
		        String theAttributeValue, IElementTagStructureHandler theStructureHandler) {
			String text = applyTemplateWithinTag(myFhirContext, theContext, null, theAttributeValue);
			theStructureHandler.setBody(text, false);
		}
	}
}
