package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAttributeTranslator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.Collections;

public class PersonAttributeTranslatorImpl implements PersonAttributeTranslator {

    @Autowired
    LocationTypeTranslator locationTypeTranslator;

    @Override
    public Extension toFhirResource(@Nonnull PersonAttribute personAttribute) {
        Extension wrapperExtension = new Extension(new UriType(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
        PersonAttributeType personAttributeType = personAttribute.getAttributeType();

        Extension extension = new Extension(new UriType(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE + "#" + personAttributeType.getName()));

        if(personAttributeType.getFormat().equals("java.lang.String")) {
            extension.setValue(new StringType(personAttribute.getValue()));
        } else if (personAttributeType.getFormat().equals("java.lang.Boolean")) {
            extension.setValue( new BooleanType(personAttribute.getValue()));
        } else if (personAttributeType.getFormat().equals("org.openmrs.Location")) {
            //TODO
        } else if (personAttributeType.getFormat().equals("org.openmrs.Concept")) {
            //TODO
        }

        wrapperExtension.setExtension(Collections.singletonList(extension));

        return extension;
    }


    @Override
    public PersonAttribute toOpenmrsType(@Nonnull Extension extension) {
        //TODO
        return null;
    }
}
