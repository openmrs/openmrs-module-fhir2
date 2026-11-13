package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Order;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;
import org.openmrs.module.fhir2.model.FhirBatch;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import javax.annotation.Nonnull;

public interface BatchTranslator extends OpenmrsFhirUpdatableTranslator<FhirBatch, Bundle> {

	/**
	 * Maps a {@link Order} to a {@link Bundle}
	 *
	 * @param fhirBatch the FhirBatch object to translate
	 * @return the corresponding FHIR Bundle
	 */
	@Override
	Bundle toFhirResource(@Nonnull FhirBatch fhirBatch);

	/**
	 * Maps {@link Bundle} to {@link FhirBatch}
	 *
	 * @param bundle the FHIR batch to translate
	 * @return the corresponding OpenMRS FhirBatch
	 */
	@Override
	FhirBatch toOpenmrsType(@Nonnull Bundle bundle);
}
