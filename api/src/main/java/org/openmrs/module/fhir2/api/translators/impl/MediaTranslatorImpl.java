package org.openmrs.module.fhir2.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class MediaTranslatorImpl implements MediaTranslator {


    @Autowired
    private ObservationStatusTranslator observationStatusTranslator;

    @Override
    public Media toFhirResource(@Nonnull Obs data) {
        notNull(data, "The Openmrs Complex obs object should not be null");
        Media media = new Media();
        return media;
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Media resource) {
        notNull(resource, "The media resource should not be null");
        return toOpenmrsType(new Obs(), resource);
    }

    @Override
    public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource) {
        notNull(existingObject, "The existing object should not be null");
        notNull(resource, "The observation object should not be null");
        return existingObject;
    }
}
