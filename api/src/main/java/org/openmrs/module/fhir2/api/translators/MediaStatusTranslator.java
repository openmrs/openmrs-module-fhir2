package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaStatusTranslator extends OpenmrsFhirUpdatableTranslator<Obs, Media.MediaStatus>{

    /**
     * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.Media.MediaStatus}
     *
     * @param data the complex obs to translate
     * @return the corresponding media resource
     */
    @Override
    Media.MediaStatus toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Media.MediaStatus} to an {@link Obs}
     *
     * @param existingObject the observation to update
     * @param resource the media status
     * @return the corresponding media resource status
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media.MediaStatus resource);
}
