package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaContentType extends OpenmrsFhirUpdatableTranslator<Obs, Media>{

    /**
     * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.Media}
     *
     * @param data the complex obs value to translate
     * @return the corresponding media resource content type
     */
    @Override
    Media toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Media} to an {@link Obs}
     *
     * @param existingObject the obs object to to update
     * @param resource the media resource
     * @return the corresponding media resource content type
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
