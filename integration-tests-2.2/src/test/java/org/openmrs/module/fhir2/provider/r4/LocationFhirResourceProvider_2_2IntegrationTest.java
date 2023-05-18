package org.openmrs.module.fhir2.provider.r4;

import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.LocationFhirResourceProvider;
import org.hl7.fhir.r4.model.Location;
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

public class LocationFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<LocationFhirResourceProvider, Location> {
	
	private static final String LOCATION_UUID = "e0938432-1691-11df-97a5-7038c432";
	
	private static final String LOCATION_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocation_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_LOCATION_PATH = "org/openmrs/module/fhir2/providers/Location_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private LocationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(LOCATION_DATA_XML);
	}
	
	@Test
	public void shouldPatchExistingLocationViaJson() throws Exception {
		String jsonLocationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_LOCATION_PATH)) {
			Objects.requireNonNull(is);
			jsonLocationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Location/" + LOCATION_UUID)
				.jsonMergePatch(jsonLocationPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response, notNullValue());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Location location = readResponse(response);
		System.out.println(location);
		
		assertThat(location, notNullValue());
		assertThat(location.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
		assertThat(location, validResource());
		
		assertThat(location.getName(),is("Patched Location"));
		assertThat(location.getAddress().getCity(),is("Wakiso"));
		assertThat(location.getAddress().getCountry(),is("Uganda"));
		assertThat(location.getAddress().getPostalCode(),is("0000 WK"));
		assertThat(location.getAddress().getState(),is("Central Region"));
	}
}
