package org.openmrs.module.fhir2.api.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Test;

public class JsonPatchUtilTest {
	
	private static final String JSON_PATCH_MEDICATION_REQUEST_PATH = "org/openmrs/module/fhir2/providers/MedicationRequest_patch.json";
	
	@Test
	public void shouldPatchMedicationRequest() {
		
		String medicationRequestPatchJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestPatchJson = inputStreamToString(is, UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		MedicationRequest medicationRequest = new MedicationRequest();
		String id = "123abc";
		medicationRequest.setId(id);
		
		CodeableConcept route = new CodeableConcept();
		route.setText("route");
		Dosage dosage = new Dosage();
		dosage.setRoute(route);
		medicationRequest.addDosageInstruction(dosage);
		
		Extension extension = new Extension();
		extension.setUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS);
		extension.setValue(new CodeType("RECEIVED"));
		medicationRequest.addExtension(extension);
		
		MedicationRequest patchedMedicationRequest = JsonPatchUtils.apply(FhirContext.forR4(), medicationRequest,
		    medicationRequestPatchJson);
		
		assertThat(patchedMedicationRequest.getIdElement().getIdPart(), equalTo(id));
		assertThat(patchedMedicationRequest.getDosageInstruction().get(0).getRoute().getText(), equalTo("route"));
		
		Extension resultExtension = patchedMedicationRequest
		        .getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS);
		assertThat(resultExtension.getValue().toString(), equalTo("COMPLETED"));
		
	}
}
