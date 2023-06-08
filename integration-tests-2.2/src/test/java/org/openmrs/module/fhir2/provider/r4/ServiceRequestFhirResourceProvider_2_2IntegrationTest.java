package org.openmrs.module.fhir2.provider.r4;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.ServiceRequestFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.InputStream;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

public class ServiceRequestFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<ServiceRequestFhirResourceProvider, ServiceRequest> {
	
	private static final String SERVICE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String SERVICE_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequest_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_SERVICE_REQUEST_PATH = "org/openmrs/module/fhir2/providers/ServiceRequest_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ServiceRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(SERVICE_REQUEST_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingServiceRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ServiceRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldPatchExistingServiceRequestViaJson() throws Exception {
		String jsonServiceRequestPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_SERVICE_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			jsonServiceRequestPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/ServiceRequest/" + SERVICE_REQUEST_UUID)
				.jsonMergePatch(jsonServiceRequestPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ServiceRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
		
		assertThat(serviceRequest.getStatus(), equalTo("active"));

	}
}
