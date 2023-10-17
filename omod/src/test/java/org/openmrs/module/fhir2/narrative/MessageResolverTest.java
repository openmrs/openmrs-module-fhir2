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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.thymeleaf.context.ITemplateContext;

@RunWith(MockitoJUnitRunner.class)
public class MessageResolverTest {
	
	private OpenmrsMessageResolver messageResolver;
	
	@Mock
	private ITemplateContext ctx;
	
	@Before
	public void setup() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:testMessages");
		messageResolver = new OpenmrsMessageResolver(messageSource);
	}
	
	@Test
	public void shouldThrowIllegalArgumentExceptionWhenContextIsNull() {
		assertThrows(IllegalArgumentException.class,
		    () -> messageResolver.resolveMessage(null, this.getClass(), "", new Object[] {}));
	}
	
	@Test
	public void shouldReturnNullWhenMessageNotFound() {
		String givenKey = "someRandomKey";
		
		doReturn(Locale.ENGLISH).when(ctx).getLocale();
		String message = messageResolver.resolveMessage(ctx, this.getClass(), givenKey, new Object[] {});
		
		assertThat(message, nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenOriginIsNullAndMessageNotFound() {
		String givenKey = "someRandomKey";
		
		doReturn(Locale.ENGLISH).when(ctx).getLocale();
		String message = messageResolver.resolveMessage(ctx, null, givenKey, new Object[] {});
		
		assertThat(message, nullValue());
	}
	
	@Test
	public void shouldReturnExpectedMessageWhenValidKeyPassed() {
		String givenKey = "testKey";
		String expectedMessage = "testMessage";
		
		doReturn(Locale.ENGLISH).when(ctx).getLocale();
		String message = messageResolver.resolveMessage(ctx, this.getClass(), givenKey, new Object[] {});
		
		assertThat(message.trim(), equalTo(expectedMessage));
	}
	
	@Test
	public void shouldReturnNullAsMessageAbsentRepresentation() {
		assertThat(messageResolver.createAbsentMessageRepresentation(null, null, null, new Object[] {}), nullValue());
	}
}
