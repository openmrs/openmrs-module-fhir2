/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.springframework.http.HttpHeaders.ACCEPT;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.module.fhir2.WebTestFhirSpringConfiguration;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public abstract class BaseFhirResourceProviderTest<T extends IResourceProvider> {

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

	public static class FhirRequestBuilder {

		private final MockHttpServletRequest request;

		private FhirRequestBuilder(RequestTypeEnum requestType, String uri) {
			request = new MockHttpServletRequest();
			request.setMethod(requestType.toString());
			request.setRequestURI(uri);
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

	private static abstract class HttpResponseMatcher extends TypeSafeMatcher<MockHttpServletResponse> {

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

	private static final String SERVLET_NAME = "fhir2Servlet";

	private static AnnotationConfigWebApplicationContext webApplicationContext;

	private static ServletConfig servletConfig;

	private static boolean initialized = false;

	private static FhirRestServlet servlet;

	@BeforeClass
	public static void setupServlet() {
		webApplicationContext = new AnnotationConfigWebApplicationContext();
		webApplicationContext.register(WebTestFhirSpringConfiguration.class);
		webApplicationContext.refresh();

		MockServletContext servletContext = new MockServletContext(webApplicationContext);
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

		servletConfig = new MockServletConfig(servletContext, SERVLET_NAME);
	}

	@Before
	public void setup() throws Exception {
		if (!initialized) {
			T resourceProvider = getResourceProvider();
			webApplicationContext.getBeanFactory().registerSingleton("fhirResources", resourceProvider);
			servlet = webApplicationContext.getBean(FhirRestServlet.class);
			servlet.init(servletConfig);
			initialized = true;
		}
	}

	public FhirRequestBuilder get(@NotNull String uri) {
		return new FhirRequestBuilder(RequestTypeEnum.GET, "/" + SERVLET_NAME + uri);
	}

	public FhirRequestBuilder post(@NotNull String uri) {
		return new FhirRequestBuilder(RequestTypeEnum.POST, "/" + SERVLET_NAME + uri);
	}

	abstract T getResourceProvider();
}
