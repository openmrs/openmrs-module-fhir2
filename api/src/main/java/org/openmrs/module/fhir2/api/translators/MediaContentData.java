package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaContentData extends OpenmrsFhirUpdatableTranslator<Obs, Media>{

    /**
     * Maps an {@link org.openmrs.Obs} to a corresponding {@link org.hl7.fhir.r4.model.Type}
     *
     * @param data the obs data to translate
     * @return the corresponding FHIR base64 encoded version of the data
     */
    @Override
    Media toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Type} to a existing {@link org.openmrs.Obs}
     *
     * @param existingObject the obs data to update
     * @param resource the FHIR base64 encoded version of the data to map
     * @return an updated version of the obs data
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
