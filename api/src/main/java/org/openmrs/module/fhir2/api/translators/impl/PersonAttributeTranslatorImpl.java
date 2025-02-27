package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.openmrs.Concept;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAttributeTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;

@Component
public class PersonAttributeTranslatorImpl implements PersonAttributeTranslator {

    private static final String JAVA_STRING_FORMAT = "java.lang.String";
    private static final String JAVA_BOOLEAN_FORMAT = "java.lang.Boolean";
    private static final String OPENMRS_LOCATION_FORMAT = "org.openmrs.Location";
    private static final String OPENMRS_CONCEPT_FORMAT = "org.openmrs.Concept";
    private static final String LOCATION_TYPE = "Location";

    @Autowired
    private LocationService locationService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private ConceptTranslator conceptTranslator;

    @Override
    public Extension toFhirResource(@Nonnull PersonAttribute personAttribute) {
        if ( personAttribute.getAttributeType() == null) {
            throw new IllegalArgumentException("PersonAttribute and AttributeType cannot be null");
        }

        Extension wrapperExtension = createWrapperExtension();
        Extension valueExtension = createValueExtension(personAttribute);
        wrapperExtension.setExtension(Collections.singletonList(valueExtension));

        return valueExtension;
    }

    @Override
    public PersonAttribute toOpenmrsType(@Nonnull Extension extension) {
        if(!isValidPatientAttributeExtension(extension)) {
            return null;
        }

        PersonAttribute personAttribute = new PersonAttribute();
        Type extensionValue = extension.getExtensionFirstRep().getValue();
        String attributeTypeName = extractAttributeTypeName(extension.getUrl());

        personAttribute.setAttributeType(getPersonAttributeType(attributeTypeName));
        setPersonAttributeValue(personAttribute, extensionValue);

        return personAttribute;
    }

    private Extension createWrapperExtension() {
        return new Extension(new UriType(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
    }

    private Extension createValueExtension(PersonAttribute personAttribute) {
        PersonAttributeType attributeType = personAttribute.getAttributeType();
        String extensionUrl = FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE + "#" + attributeType.getName();
        Extension extension = new Extension(new UriType(extensionUrl));

        setExtensionValue(extension, personAttribute);
        return extension;
    }

    private void setExtensionValue(Extension extension, PersonAttribute personAttribute) {
        String format = personAttribute.getAttributeType().getFormat();
        String value = personAttribute.getValue();

        try {
            switch (format) {
                case JAVA_STRING_FORMAT:
                    extension.setValue(new StringType(value));
                    break;
                case JAVA_BOOLEAN_FORMAT:
                    extension.setValue(new BooleanType(value));
                    break;
                case OPENMRS_LOCATION_FORMAT:
                    handleLocationFormat(extension, value);
                    break;
                case OPENMRS_CONCEPT_FORMAT:
                    handleConceptFormat(extension, value);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported attribute format: " + format);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error processing attribute value for format: " + format, e);
        }
    }

    private void handleLocationFormat(Extension extension, String locationId) {
        if (locationId == null) {
            return;
        }

        Reference locationReference = new Reference()
                .setType(LOCATION_TYPE)
                .setReference(LOCATION_TYPE + "/" + locationId);

        Optional.ofNullable(locationService.getLocation(locationId))
                .ifPresent(location -> locationReference.setDisplay(location.getDisplayString()));

        extension.setValue(locationReference);
    }

    private void handleConceptFormat(Extension extension, String conceptId) {
        if (conceptId == null) {
            return;
        }

        Concept concept = conceptService.getConcept(conceptId);
        if (concept != null) {
            CodeableConcept codeableConcept = conceptTranslator.toFhirResource(concept);
            extension.setValue(codeableConcept);
        }

    }

    private boolean isValidPatientAttributeExtension(Extension extension) {
        return extension != null && extension.hasUrl() &&
                extension.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE) &&
                extension.hasValue();
    }

    private String extractAttributeTypeName(String url) {
        return url.substring(url.lastIndexOf("#") + 1);
    }

    private PersonAttributeType getPersonAttributeType(String typeName) {
        PersonAttributeType attributeType = personService.getPersonAttributeTypeByName(typeName);
        if (attributeType == null) {
            throw new IllegalStateException("PersonAttributeType not found: " + typeName);
        }
        return attributeType;
    }

    private void setPersonAttributeValue(PersonAttribute personAttribute, Type extensionValue) {
        try {
            if (extensionValue instanceof BooleanType) {
                personAttribute.setValue(((BooleanType) extensionValue).getValueAsString());
            } else if (extensionValue instanceof StringType) {
                personAttribute.setValue(((StringType) extensionValue).getValue());
            } else if (extensionValue instanceof Reference) {
                String reference = ((Reference) extensionValue).getReference();
                personAttribute.setValue(reference.substring(reference.lastIndexOf("/") + 1));
            } else if (extensionValue instanceof CodeableConcept) {
                Concept concept = conceptTranslator.toOpenmrsType((CodeableConcept) extensionValue);
                if (concept != null) {
                    personAttribute.setValue(concept.getConceptId().toString());
                }
            } else {
                throw new UnsupportedOperationException("Unsupported extension value type: " +
                        extensionValue.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error setting attribute value", e);
        }
    }
}