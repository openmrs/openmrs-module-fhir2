package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.FhirMediaTranslator;

import javax.annotation.Nonnull;

public class FhirMediaTranslatorImpl extends BaseEncounterTranslator implements FhirMediaTranslator {
    @Override
    public Media toFhirResource(@Nonnull Obs data) {
        return null;
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Media resource) {
        return null;
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource) {
        return null;
    }
}
