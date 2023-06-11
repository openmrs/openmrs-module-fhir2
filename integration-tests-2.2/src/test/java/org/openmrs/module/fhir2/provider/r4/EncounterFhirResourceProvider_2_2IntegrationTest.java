package org.openmrs.module.fhir2.provider.r4;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.EncounterFhirResourceProvider;
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

public class EncounterFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String MEDICATION_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounter_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/Encounter_patch.json";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingEncounterAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldPatchExistingMedicationViaJsonMergePatch() throws Exception {
		String jsonEncounterPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			jsonEncounterPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Encounter/" + ENCOUNTER_UUID).jsonMergePatch(jsonEncounterPatch)
				.accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
		
//		assertThat(encounter.getClass_().getCode(), is("EMER"));
//		assertThat(encounter.getClass_().getDisplay(), is("emergency"));
	}
}
