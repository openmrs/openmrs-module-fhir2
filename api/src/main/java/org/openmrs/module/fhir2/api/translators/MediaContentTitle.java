package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaContentTitle extends OpenmrsFhirUpdatableTranslator<Obs, Media>{

    /**
     * Maps an {@link org.openmrs.Obs} to a corresponding {@link org.hl7.fhir.r4.model.Media}
     *
     * @param data the obs propert to translate
     * @return the corresponding FHIR media resource title
     */
    @Override
    Media toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Media} to a existing {@link org.openmrs.Obs}
     *
     * @param existingObject the obs propert to update
     * @param resource the media resource title to map
     * @return an updated version of the obs
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
