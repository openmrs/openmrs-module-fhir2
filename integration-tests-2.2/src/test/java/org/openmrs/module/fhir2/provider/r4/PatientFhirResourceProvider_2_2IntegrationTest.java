package org.openmrs.module.fhir2.provider.r4;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.PatientFhirResourceProvider;
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

public class PatientFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<PatientFhirResourceProvider, Patient> {
	private static final String PATIENT_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PATIENT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatient_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_PATIENT_PATH = "org/openmrs/module/fhir2/providers/Patient_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PatientFhirResourceProvider patientFhirResourceProvider;
	
	@Override
	public PatientFhirResourceProvider getResourceProvider() {
		return patientFhirResourceProvider;
	}
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PATIENT_DATA_XML);
	}
	
	@Test
	public void shouldPatchExistingPatientViaJson() throws Exception {
		String jsonPatientPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatientPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Patient/" + PATIENT_UUID).jsonMergePatch(jsonPatientPatch)
				.accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response, notNullValue());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
		
		assertThat(patient.getGender(), is("F"));
	}
}
