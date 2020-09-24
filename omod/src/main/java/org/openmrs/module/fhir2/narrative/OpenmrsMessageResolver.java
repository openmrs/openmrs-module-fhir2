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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.StandardMessageResolver;

@Slf4j
public class OpenmrsMessageResolver extends AbstractMessageResolver {
	
	private final StandardMessageResolver standardMessageResolver = new StandardMessageResolver();
	
	private final MessageSource messageSource;
	
	public OpenmrsMessageResolver(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public String resolveMessage(final ITemplateContext context, final Class<?> origin, final String key,
	        final Object[] messageParameters) {
		
		try {
			return messageSource.getMessage(key, messageParameters, context != null ? context.getLocale() : null);
		}
		catch (NoSuchMessageException ignored) {}
		
		if (origin != null) {
			return standardMessageResolver.resolveMessage(context, origin, key, messageParameters, false, true, true);
		}
		
		return null;
	}
	
	@Override
	public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key,
	        Object[] objects) {
		// returns null so that absence of the message can be identified in the templates
		return null;
	}
}
