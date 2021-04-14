package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaStatusTranslator;

import javax.annotation.Nonnull;

public class MediaStatusTranslatorImpl implements MediaStatusTranslator {
    @Override
    public Media.MediaStatus toFhirResource(@Nonnull Obs data) {
        return null;
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media.MediaStatus resource) {
        return null;
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Media.MediaStatus resource) {
        return null;
    }
}
