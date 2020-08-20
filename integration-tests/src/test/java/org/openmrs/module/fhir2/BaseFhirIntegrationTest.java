/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import lombok.SneakyThrows;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.junit.Before;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = IntegrationTestConfiguration.class, inheritLocations = false)
public abstract class BaseFhirIntegrationTest<T extends IResourceProvider, U extends IDomainResource> extends BaseModuleWebContextSensitiveTest {
	
	private ServletConfig servletConfig;
	
	private IParser jsonParser;
	
	private IParser xmlParser;
	
	private FhirRestServlet servlet;
	
	// This must be implemented by subclasses
	public abstract T getResourceProvider();
	
	// These are expected to be implemented by version-specific sub-classes
	public abstract String getServletName();
	
	public abstract FhirContext getFhirContext();
	
	public abstract FhirRestServlet getRestfulServer();
	
	public abstract void describeOperationOutcome(Description mismatchDescription, IBaseOperationOutcome operationOutcome);
	
	public abstract Class<? extends IBaseOperationOutcome> getOperationOutcomeClass();
	
	public abstract U removeNarrative(U item);
	
	@Before
	public void setup() throws Exception {
		jsonParser = getFhirContext().newJsonParser();
		xmlParser = getFhirContext().newXmlParser();
		
		MockServletContext servletContext = new MockServletContext();
		WebApplicationContext wac = new DelegatingWebApplicationContext(applicationContext, servletContext);
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		
		servletConfig = new MockServletConfig(servletContext, getServletName());
		
		setupFhirServlet();
	}
	
	public void setupFhirServlet() throws ServletException {
		servlet = getRestfulServer();
		servlet.setFhirContext(getFhirContext());
		servlet.init(servletConfig);
	}
	
	public FhirValidator getValidator() {
		return getFhirContext().newValidator().setValidateAgainstStandardSchema(true);
	}
	
	public Matcher<MockHttpServletResponse> isOk() {
		return new IsOkMatcher();
	}
	
	public Matcher<MockHttpServletResponse> isNotFound() {
		return statusEquals(HttpStatus.NOT_FOUND);
	}
	
	public Matcher<MockHttpServletResponse> isCreated() {
		return statusEquals(HttpStatus.CREATED);
	}
	
	public Matcher<MockHttpServletResponse> isBadRequest() {
		return statusEquals(HttpStatus.BAD_REQUEST);
	}
	
	public Matcher<MockHttpServletResponse> isMethodNotAllowed() {
		return statusEquals(HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	public Matcher<MockHttpServletResponse> statusEquals(final int status) {
		return new StatusEqualsMatcher(status);
	}
	
	public Matcher<MockHttpServletResponse> statusEquals(HttpStatus status) {
		return statusEquals(status.value());
	}
	
	public Matcher<U> validResource() {
		return new IsValidResourceMatcher();
	}
	
	public FhirRequestBuilder get(@NotNull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.GET, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder post(@NotNull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.POST, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder put(@NotNull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.PUT, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder delete(@NotNull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.DELETE, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	@SuppressWarnings("unchecked")
	public U readResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.parseMediaType(response.getContentType());
		if (mediaType.isCompatibleWith(FhirMediaTypes.XML) || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
		        || mediaType.isCompatibleWith(MediaType.TEXT_XML)) {
			return (U) xmlParser.parseResource(getResourceProvider().getResourceType(), response.getContentAsString());
		} else {
			return (U) jsonParser.parseResource(getResourceProvider().getResourceType(), response.getContentAsString());
		}
	}
	
	public IBaseBundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.parseMediaType(response.getContentType());
		if (mediaType.isCompatibleWith(FhirMediaTypes.XML) || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
		        || mediaType.isCompatibleWith(MediaType.TEXT_XML)) {
			return (IBaseBundle) xmlParser.parseResource(response.getContentAsString());
		} else {
			return (IBaseBundle) jsonParser.parseResource(response.getContentAsString());
		}
	}
	
	public IBaseOperationOutcome readOperationOutcome(MockHttpServletResponse response) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.parseMediaType(response.getContentType());
		if (mediaType.isCompatibleWith(FhirMediaTypes.XML) || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
		        || mediaType.isCompatibleWith(MediaType.TEXT_XML)) {
			return xmlParser.parseResource(getOperationOutcomeClass(), response.getContentAsString());
		} else {
			return jsonParser.parseResource(getOperationOutcomeClass(), response.getContentAsString());
		}
	}
	
	public String toJson(U resource) {
		return jsonParser.encodeResourceToString(resource);
	}
	
	public String toXML(U resource) {
		return xmlParser.encodeResourceToString(resource);
	}
	
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
	
	private abstract class HttpResponseMatcher extends TypeSafeMatcher<MockHttpServletResponse> {
		
		@SneakyThrows
		@Override
		protected void describeMismatchSafely(MockHttpServletResponse item, Description mismatchDescription) {
			IBaseOperationOutcome operationOutcome = readOperationOutcome(item);
			
			mismatchDescription.appendText("response with status code ").appendValue(item.getStatus());
			
			if (operationOutcome != null) {
				describeOperationOutcome(mismatchDescription, operationOutcome);
			}
		}
	}
	
	private class IsOkMatcher extends HttpResponseMatcher {
		
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
	
	private class StatusEqualsMatcher extends HttpResponseMatcher {
		
		private final int status;
		
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
	
	private class IsValidResourceMatcher extends TypeSafeMatcher<U> {
		
		@Override
		protected boolean matchesSafely(U item) {
			item = removeNarrative(item);
			return getValidator().validateWithResult(item).isSuccessful();
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("is a valid FHIR resource");
		}
		
		@Override
		protected void describeMismatchSafely(U item, Description mismatchDescription) {
			item = removeNarrative(item);
			mismatchDescription.appendText("was invalid because ");
			mismatchDescription.appendText(getValidator().validateWithResult(item).getMessages().stream()
			        .map(SingleValidationMessage::getMessage).collect(Collectors.joining(", ")));
		}
	}
	
	public class FhirRequestBuilder {
		
		private final MockHttpServletRequest request;
		
		private FhirRequestBuilder(RequestTypeEnum requestType, String uri) throws MalformedURLException {
			request = new MockHttpServletRequest();
			request.setMethod(requestType.toString());
			URL url = new URL(uri);
			request.setRequestURI(url.getPath());
			request.setQueryString(url.getQuery());
			request.setServletPath("/ms/");
		}
		
		public FhirRequestBuilder accept(@NotNull MediaType mediaType) {
			request.addHeader(ACCEPT, mediaType.toString());
			return this;
		}
		
		public FhirRequestBuilder jsonContent(@NotNull String json) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.JSON.toString());
			request.setContent(json.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public FhirRequestBuilder xmlContext(@NotNull String xml) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.XML.toString());
			request.setContent(xml.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public MockHttpServletResponse go() throws ServletException, IOException {
			MockHttpServletResponse response = new MockHttpServletResponse();
			servlet.service(request, response);
			return response;
		}
	}
}
