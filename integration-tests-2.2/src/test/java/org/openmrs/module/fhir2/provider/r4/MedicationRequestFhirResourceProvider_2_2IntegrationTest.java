package org.openmrs.module.fhir2.provider.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.MedicationRequestFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationRequestFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_UUID = "546ba5a6-5aa6-4325-afc0-50bc00d5ffa1";
	
	private static final String MEDICATION_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequest_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_MEDICATION_REQUEST_PATH = "org/openmrs/module/fhir2/providers/MedicationRequest_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_REQUEST_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingMedicationRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
		
		// confirm that the new fulfiller extension has been added
		assertThat(medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS),
		    notNullValue());
		assertThat(
		    medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS).getValue().toString(),
		    is("RECEIVED"));
		
	}
	
	@Test
	public void shouldPatchExistingMedicationRequestViaJson() throws Exception {
		String jsonMedicationRequestPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationRequestPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .jsonPatch(jsonMedicationRequestPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
		
		// confirm that the fulfiller extension has been updated
		assertThat(medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS),
		    notNullValue());
		assertThat(
		    medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS).getValue().toString(),
		    is("COMPLETED"));
	}
}
