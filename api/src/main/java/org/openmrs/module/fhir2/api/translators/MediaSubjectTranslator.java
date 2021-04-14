package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;

import javax.annotation.Nonnull;

public interface MediaSubjectTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.Patient, Reference>{

    /**
     * Maps an {@link org.openmrs.Patient} to a {@link org.hl7.fhir.r4.model.Reference}
     *
     * @param data the patient to translate
     * @return the corresponding FHIR reference resource
     */
    @Override
    Reference toFhirResource(@Nonnull Patient data);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.Reference} to an {@link Patient}
     *
     * @param existingObject the patient to update
     * @param resource the media patient reference
     * @return the corresponding patient
     */
    @Override
    Patient toOpenmrsType(@Nonnull Patient existingObject, @Nonnull Reference resource);
}
