package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

import javax.annotation.Nonnull;

public interface FhirMediaTranslator extends ToFhirTranslator<Obs,Media>, OpenmrsFhirUpdatableTranslator<Obs,Media>{

    /**
     * Maps an {@link org.openmrs.Obs} to a {@link org.hl7.fhir.r4.model.Media}
     *
     * @param data the Observation to translate
     * @return the corresponding FHIR location resource
     */
    @Override
    Media toFhirResource(@Nonnull Obs data);

    /**
     * Maps a {@link org.hl7.fhir.r4.model.Media} to an {@link org.openmrs.Obs}
     *
     * @param resource the FHIR Media resource to translate
     * @return the corresponding OpenMRS observation resource
     */
    @Override
    Obs toOpenmrsType(@Nonnull Media resource);

    /**
     * Maps a {@link Media} to an existing {@link org.openmrs.Obs}
     *
     * @param existingObject the observation to update
     * @param resource the FHIR complex object to map
     * @return the updated OpenMRS observation
     */
    @Override
    Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
