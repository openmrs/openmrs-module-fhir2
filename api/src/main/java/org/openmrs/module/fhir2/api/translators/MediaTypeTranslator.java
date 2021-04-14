package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Type;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaTypeTranslator extends OpenmrsFhirUpdatableTranslator<Obs, Type>{

    /**
     * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.Type}
     *
     * @param data the obs to translate
     * @return the corresponding media resource type
     */
    @Override
    Type toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Type} to an {@link Obs}
     *
     * @param existingObject the obs to update
     * @param resource the media resource type element
     * @return the corresponding media resource type element
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Type resource);
}
