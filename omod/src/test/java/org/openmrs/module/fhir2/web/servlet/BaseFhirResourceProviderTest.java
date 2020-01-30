/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import static org.springframework.http.HttpHeaders.ACCEPT;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import lombok.SneakyThrows;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public abstract class BaseFhirResourceProviderTest<T extends IResourceProvider, U extends IBaseResource> {
	
	public static class FhirMediaTypes {
		
		public static final MediaType JSON;
		
		public static final MediaType XML;
		
		static {
			JSON = MediaType.valueOf("application/fhir+json");
			XML = MediaType.valueOf("application/fhir+xml");
		}
		
		private FhirMediaTypes() {
		}
	}
	
	private static abstract class HttpResponseMatcher extends TypeSafeMatcher<MockHttpServletResponse> {
		
		@SneakyThrows
		@Override
		protected void describeMismatchSafely(MockHttpServletResponse item, Description mismatchDescription) {
			mismatchDescription.appendText("response with status code ").appendValue(item.getStatus());
		}
	}
	
	private static class IsOkMatcher extends HttpResponseMatcher {
		
		@Override
		protected boolean matchesSafely(MockHttpServletResponse item) {
			int status = item.getStatus();
			return status >= HttpStatus.OK.value() && status < HttpStatus.BAD_REQUEST.value();
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("response with HTTP status indicating request was handled successfully");
		}
	}
	
	private static class StatusEqualsMatcher extends HttpResponseMatcher {
		
		private int status;
		
		private StatusEqualsMatcher(int status) {
			this.status = status;
		}
		
		@Override
		protected boolean matchesSafely(MockHttpServletResponse item) {
			return item.getStatus() == status;
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("response with HTTP status ").appendValue(status);
		}
	}
	
	public static Matcher<MockHttpServletResponse> isOk() {
		return new IsOkMatcher();
	}
	
	public static Matcher<MockHttpServletResponse> isNotFound() {
		return statusEquals(HttpStatus.NOT_FOUND);
	}
	
	public static Matcher<MockHttpServletResponse> statusEquals(final int status) {
		return new StatusEqualsMatcher(status);
	}
	
	public static Matcher<MockHttpServletResponse> statusEquals(HttpStatus status) {
		return statusEquals(status.value());
	}
	
	public class FhirRequestBuilder {
		
		private final MockHttpServletRequest request;
		
		private FhirRequestBuilder(RequestTypeEnum requestType, String uri) throws MalformedURLException {
			request = new MockHttpServletRequest();
			request.setMethod(requestType.toString());
			URL url = new URL(uri);
			request.setRequestURI(url.getPath());
			request.setQueryString(url.getQuery());
		}
		
		public FhirRequestBuilder accept(@NotNull MediaType mediaType) {
			request.addHeader(ACCEPT, mediaType.toString());
			return this;
		}
		
		public MockHttpServletResponse go() throws ServletException, IOException {
			MockHttpServletResponse response = new MockHttpServletResponse();
			servlet.service(request, response);
			return response;
		}
	}
	
	private static final String SERVLET_NAME = "fhir2Servlet";
	
	private static ServletConfig servletConfig;
	
	private static IParser parser;
	
	private static LoggingInterceptor interceptor;
	
	private FhirRestServlet servlet;
	
	@BeforeClass
	public static void setupServlet() {
		parser = FhirContext.forR4().newJsonParser();
		
		interceptor = new LoggingInterceptor();
		interceptor.setLoggerName("org.openmrs.module.fhir2.accessLog");
		
		MockServletContext servletContext = new MockServletContext();
		servletConfig = new MockServletConfig(servletContext, SERVLET_NAME);
	}
	
	@Before
	public void setup() throws Exception {
		servlet = new FhirRestServlet();
		servlet.setFhirContext(FhirContext.forR4());
		servlet.setLoggingInterceptor(interceptor);
		servlet.setGlobalPropertyService(property -> {
			switch (property) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					return "10";
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					return "100";
			}
			
			return null;
		});
		servlet.setResourceProviders(getResourceProvider());
		servlet.init(servletConfig);
	}
	
	public FhirRequestBuilder get(@NotNull String uri) throws MalformedURLException {
		return new FhirRequestBuilder(RequestTypeEnum.GET, "http://localhost:8080/" + SERVLET_NAME + uri);
	}
	
	public FhirRequestBuilder post(@NotNull String uri) throws MalformedURLException {
		return new FhirRequestBuilder(RequestTypeEnum.POST, "http://localhost:8080/" + SERVLET_NAME + uri);
	}
	
	public U readResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (U) parser.parseResource(response.getContentAsString());
	}
	
	public Bundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (Bundle) parser.parseResource(response.getContentAsString());
	}
	
	public abstract T getResourceProvider();
}
