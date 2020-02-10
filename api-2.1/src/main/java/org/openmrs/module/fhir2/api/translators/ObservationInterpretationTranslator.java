package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Obs;

public interface ObservationInterpretationTranslator extends ToFhirTranslator<Obs, CodeableConcept>, UpdatableOpenmrsTranslator<Obs, CodeableConcept> {

    /**
     * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.CodeableConcept}
     * @param obs the OpenMRS obs element to translate
     * @return
     */
    @Override
    CodeableConcept toFhirResource(Obs obs);

    /**
     * Maps an {@link org.hl7.fhir.r4.model.CodeableConcept} to an {@link Obs}
     * @param existingObject the existingObject to update
     * @param resource the resource to map
     * @return
     */
    @Override
    Obs toOpenmrsType(Obs existingObject, CodeableConcept resource);
}
