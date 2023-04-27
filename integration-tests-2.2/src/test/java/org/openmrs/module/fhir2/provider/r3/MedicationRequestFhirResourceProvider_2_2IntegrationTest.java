package org.openmrs.module.fhir2.provider.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.MedicationRequestFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationRequestFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String MEDICATION_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequest_2_2_initial_data.xml";
	
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
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
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
		    is("COMPLETED"));
		
	}
}
