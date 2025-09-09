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
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;

import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import lombok.SneakyThrows;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.junit.Before;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.module.fhir2.api.util.FhirGlobalPropertyHolder;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
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
	
	@Autowired
	CacheConfig cacheConfig;
	
	@Autowired
	private ConfigurableApplicationContext ctx;
	
	// This must be implemented by subclasses
	public abstract T getResourceProvider();
	
	// These are expected to be implemented by version-specific sub-classes
	public abstract String getServletName();
	
	public abstract FhirContext getFhirContext();
	
	public abstract FhirRestServlet getRestfulServer();
	
	public abstract void describeOperationOutcome(Description mismatchDescription, IBaseOperationOutcome operationOutcome);
	
	public abstract Class<? extends IBaseOperationOutcome> getOperationOutcomeClass();
	
	public abstract U removeNarrativeAndContained(U item);
	
	@Before
	public void setup() throws Exception {
		// Needed until TRUNK-6299 in place
		cacheConfig.cacheManager().getCacheNames().forEach(name -> cacheConfig.cacheManager().getCache(name).clear());
		
		FhirGlobalPropertyHolder.reset();
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
		new FhirActivator().setApplicationContext(ctx);
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
	
	public FhirRequestBuilder get(@Nonnull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.GET, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder post(@Nonnull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.POST, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder put(@Nonnull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.PUT, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder patch(@Nonnull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.PATCH, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public FhirRequestBuilder delete(@Nonnull String uri) throws MalformedURLException {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		
		return new FhirRequestBuilder(RequestTypeEnum.DELETE, "http://localhost:8080/ms/" + getServletName() + uri);
	}
	
	public U readResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.parseMediaType(response.getContentType());
		try {
			if (mediaType.isCompatibleWith(FhirMediaTypes.XML) || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
			        || mediaType.isCompatibleWith(MediaType.TEXT_XML)) {
				@SuppressWarnings("unchecked")
				U result = (U) xmlParser.parseResource(getResourceProvider().getResourceType(),
				    response.getContentAsString());
				return result;
			} else {
				@SuppressWarnings("unchecked")
				U result = (U) jsonParser.parseResource(getResourceProvider().getResourceType(),
				    response.getContentAsString());
				return result;
			}
		}
		catch (DataFormatException e) {
			handleDataFormatException(response, e);
			throw e;
		}
	}
	
	public IBaseBundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.parseMediaType(response.getContentType());
		try {
			if (mediaType.isCompatibleWith(FhirMediaTypes.XML) || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
			        || mediaType.isCompatibleWith(MediaType.TEXT_XML)) {
				return (IBaseBundle) xmlParser.parseResource(response.getContentAsString());
			} else {
				return (IBaseBundle) jsonParser.parseResource(response.getContentAsString());
			}
		}
		catch (DataFormatException e) {
			handleDataFormatException(response, e);
			throw e;
		}
	}
	
	private void handleDataFormatException(MockHttpServletResponse response, DataFormatException e) {
		// DataFormatException usually indicates that you've requested the parser parse the wrong resource type, but
		// the most common reason for having the wrong resource type here is that the response contained an
		// OperationOutcome rather than the expected resource, so here we try to do something useful with the
		// OperationOutcome
		while (e.getCause() != null && e.getCause() instanceof DataFormatException) {
			e = (DataFormatException) e.getCause();
		}
		
		if (e.getMessage() == null || !e.getMessage().contains("OperationOutcome")) {
			return;
		}
		
		// in case we cannot parse the OperationOutcome or there isn't actually one, just return the original exception
		IBaseOperationOutcome operationOutcome;
		try {
			operationOutcome = readOperationOutcome(response);
		}
		catch (Exception ignored) {
			return;
		}
		
		Description errorDescription = new StringDescription();
		errorDescription.appendText("Received unexpected OperationOutcome");
		
		describeOperationOutcome(errorDescription, operationOutcome);
		
		throw new RuntimeException(errorDescription.toString());
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
		
		public static final MediaType JSON_MERGE_PATCH;
		
		public static final MediaType JSON_PATCH;
		
		public static final MediaType XML_PATCH;
		
		static {
			JSON = MediaType.valueOf("application/fhir+json");
			XML = MediaType.valueOf("application/fhir+xml");
			// note that this is actually the MIME-type for a Json Patch (not a Json Merge Patch); it should really be "application/merge-patch+json" but HAPI FHIR doesn't seem to support this
			JSON_MERGE_PATCH = MediaType.valueOf("application/merge-patch+json");
			JSON_PATCH = MediaType.valueOf("application/json-patch+json");
			XML_PATCH = MediaType.valueOf("application/xml-patch+xml");
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
			item = removeNarrativeAndContained(item);
			return getValidator().validateWithResult(item).isSuccessful();
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("is a valid FHIR resource");
		}
		
		@Override
		protected void describeMismatchSafely(U item, Description mismatchDescription) {
			item = removeNarrativeAndContained(item);
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
		
		public FhirRequestBuilder accept(@Nonnull MediaType mediaType) {
			request.addHeader(ACCEPT, mediaType.toString());
			return this;
		}
		
		public FhirRequestBuilder ifNoneMatchHeader(@Nonnull String etag) {
			request.addHeader(IF_NONE_MATCH, etag.toString());
			return this;
		}
		
		public FhirRequestBuilder ifMatchHeader(@Nonnull String etag) {
			request.addHeader(IF_MATCH, etag.toString());
			return this;
		}
		
		public FhirRequestBuilder jsonContent(@Nonnull String json) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.JSON.toString());
			request.setContent(json.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public FhirRequestBuilder jsonMergePatch(@Nonnull String json) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.JSON_MERGE_PATCH.toString());
			request.setContent(json.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public FhirRequestBuilder jsonPatch(@Nonnull String json) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.JSON_PATCH.toString());
			request.setContent(json.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public FhirRequestBuilder xmlPatch(@Nonnull String xml) {
			request.addHeader(CONTENT_TYPE, FhirMediaTypes.XML_PATCH.toString());
			request.setContent(xml.getBytes(StandardCharsets.UTF_8));
			return this;
		}
		
		public FhirRequestBuilder xmlContent(@Nonnull String xml) {
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
