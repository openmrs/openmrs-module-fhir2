package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.BatchTranslator;
import org.openmrs.module.fhir2.model.FhirBatch;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.Validate.notNull;

public class BatchTranslatorImpl implements BatchTranslator {

	@Override
	public Bundle toFhirResource(@Nonnull FhirBatch fhirBatch) {
		notNull(fhirBatch, "The batch operation should not be null");

		Bundle bundle = new Bundle();

		bundle.setId(fhirBatch.getUuid());

		if (fhirBatch.getDateChanged() != null) {
			bundle.getMeta().setLastUpdated(fhirBatch.getDateChanged());
		} else {
			bundle.getMeta().setLastUpdated(fhirBatch.getDateCreated());
		}

		return bundle;
	}

	@Override
	public FhirBatch toOpenmrsType(@Nonnull Bundle bundle) {
		return toOpenmrsType(new FhirBatch(), bundle);
	}

	@Override
	public FhirBatch toOpenmrsType(@Nonnull FhirBatch existingObject, @Nonnull Bundle resource) {
		return null;
	}
}
