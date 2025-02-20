package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Extension;
import org.openmrs.PersonAttribute;

import javax.annotation.Nonnull;

public interface PersonAttributeTranslator extends OpenmrsFhirTranslator<org.openmrs.PersonAttribute, Extension>{

    /**
     * Maps a {@link PersonAttribute} to a {@link Extension}
     *
     * @param personAttribute the attribute to translate in extension
     * @return the corresponding FHIR Extension
     */
    @Override
    Extension toFhirResource(@Nonnull PersonAttribute personAttribute);

    /**
     * Maps a {@link Extension} to a {@link PersonAttribute}
     *
     * @param extension the extension with attribute information
     * @return the corresponding Person Attribute
     */
    @Override
    PersonAttribute toOpenmrsType(@Nonnull Extension extension);
}
