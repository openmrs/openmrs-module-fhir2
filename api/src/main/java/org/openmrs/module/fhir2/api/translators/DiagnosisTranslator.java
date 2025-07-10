package org.openmrs.module.fhir2.api.translators;

import org.openmrs.Diagnosis;

import javax.annotation.Nonnull;

public interface DiagnosisTranslator extends OpenmrsFhirUpdatableTranslator<Diagnosis, org.hl7.fhir.r4.model.Condition> {

    /**
     * Maps <T> an openMrs condition to a {@link org.hl7.fhir.r4.model.Condition}
     *
     * @param diagnosis the OpenMRS diagnosis to translate
     * @return the corresponding FHIR condition resource
     */
    @Override
    org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Diagnosis diagnosis);

    /**
     * Maps a {@link org.hl7.fhir.r4.model.Condition} to an <T> an openMrs diagnosis
     *
     * @param condition the FHIR condition to translate
     * @return the corresponding OpenMRS diagnosis
     */
    @Override
    Diagnosis toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition);

    /**
     * Maps a {@link org.hl7.fhir.r4.model.Condition} to an existing <T> an openMrs condition
     *
     * @param existingDiagnosis the existing diagnosis to update
     * @param condition the condition to map
     * @return an updated version of the existingDiagnosis
     */
    @Override
    Diagnosis toOpenmrsType(@Nonnull Diagnosis existingDiagnosis, @Nonnull org.hl7.fhir.r4.model.Condition condition);
}
