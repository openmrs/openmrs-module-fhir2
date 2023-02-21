package org.openmrs.module.fhir2.provider.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r3.BaseFhirR3IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.EncounterFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class EncounterFhirResourceProvider_2_2IntegrationTest extends BaseFhirR3IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImpl_2_2Test_initial_data.xml";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestShouldRestrictByDateAndActive() throws Exception {
		
		// note that we restrict by date to rule out encounters from standard test data set
		MockHttpServletResponse response = get(
		    "/Encounter/?_query=encountersWithMedicationRequests&date=ge2009-01-01&status=active")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(2))); // 5 encounters in FhirEncounterDao_2_2ImplTest_initial_data, but one contains a discontinued order, one contains a cancelled order, and one contains a completed order, so we only expect 2 returned
		assertThat(result.getEntry(), hasSize(4)); // 2 orders with those 2 encounters from the test data so total elements should be 2 + 2 = 4
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
		assertThat(entries, hasItem(hasProperty("fullUrl", endsWith("430bbb70-6a9c-4e1e-badb-9d1034b1b5e9")))); // encounter 1001 from custom test data
		assertThat(entries, hasItem(hasProperty("fullUrl", endsWith("ed19b329-9ac6-4271-b730-906a27dd2d2c")))); // encounter 2004 from custom test data
		assertThat(entries, hasItem(hasProperty("fullUrl", endsWith("0d86e8b4-2f33-495e-aa22-6954785e4e9e")))); // order 1001 from custom test data
		assertThat(entries, hasItem(hasProperty("fullUrl", endsWith("cbf3f654-f2d5-4306-89aa-bc02cdd29f34")))); // order 2004 from custom test data
	}
	
	private Set<ResourceType> getEncounterWithMedicationRequestsValidResourceTypes() {
		Set<ResourceType> validTypes = new HashSet<>();
		
		validTypes.add(ResourceType.Encounter);
		validTypes.add(ResourceType.MedicationRequest);
		validTypes.add(ResourceType.MedicationDispense);
		
		return validTypes;
	}
	
}
