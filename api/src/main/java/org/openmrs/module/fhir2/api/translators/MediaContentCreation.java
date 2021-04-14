package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Type;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface MediaContentCreation extends ToFhirTranslator<Obs, Type>, UpdatableOpenmrsTranslator<Obs, Type>{

    /**
     * Maps an {@link org.openmrs.Obs} to a corresponding {@link org.hl7.fhir.r4.model.Type}
     *
     * @param data the obs datetime to translate
     * @return the corresponding FHIR type
     */
    @Override
    Type toFhirResource(@Nonnull Obs data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Type} to a existing {@link org.openmrs.Obs}
     *
     * @param existingObject the obs datetime property to update
     * @param resource the fhir resource type  to map
     * @return an updated version of the obs
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Type resource);
}
