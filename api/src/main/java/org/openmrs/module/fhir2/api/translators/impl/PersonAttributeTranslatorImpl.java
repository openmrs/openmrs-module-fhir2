/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAttributeTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
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
		if (personAttribute == null || personAttribute.getAttributeType() == null) {
			return null;
		}
		
		return createPersonAttributeExtension(personAttribute);
	}
	
	@Override
	public PersonAttribute toOpenmrsType(@Nonnull Extension extension) {
		if (extension == null) {
			return null;
		}
		
		return toOpenmrsType(new PersonAttribute(), extension);
	}
	
	@Override
	public PersonAttribute toOpenmrsType(@Nonnull PersonAttribute personAttribute, @Nonnull Extension extension) {
		if (!isValidPatientAttributeExtension(extension)) {
			return null;
		}
		
		String attributeTypeName = extractAttributeTypeName(extension.getExtensionFirstRep());
		
		try {
			PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByName(attributeTypeName);
			if (personAttributeType != null) {
				personAttribute.setAttributeType(personAttributeType);
			}
		}
		catch (Exception e) {
			log.warn("Error encountered while setting person attribute type for name: {}", attributeTypeName);
		}
		
		if (extension.hasValue()) {
			setPersonAttributeValue(personAttribute, extension.getValue());
		}
		
		return personAttribute;
	}
	
	private Extension createPersonAttributeExtension(PersonAttribute personAttribute) {
		PersonAttributeType attributeType = personAttribute.getAttributeType();
		
		StringType personAttributeTypeValue = new StringType(attributeType.getName());
		Extension personAttributeTypeExtension = new Extension(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(personAttributeTypeValue);
		
		Extension extension = new Extension(new UriType(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
		extension.addExtension(personAttributeTypeExtension);
		setExtensionValue(extension, personAttribute);
		
		return extension;
	}
	
	private void setExtensionValue(Extension extension, PersonAttribute personAttribute) {
		String format = personAttribute.getAttributeType().getFormat();
		String value = personAttribute.getValue();
		
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
				log.warn("extension has unsupported patient attribute type format: {}", format);
				extension.setValue(null);
		}
	}
	
	private void handleLocationFormat(Extension extension, String locationId) {
		if (locationId == null) {
			return;
		}
		
		Location openmrsLocation = locationService.getLocation(Integer.parseInt(locationId));
		
		if (openmrsLocation != null) {
			String locationUUID = openmrsLocation.getUuid();
			Reference locationReference = new Reference().setType(LOCATION_TYPE)
			        .setReference(LOCATION_TYPE + "/" + locationUUID);
			locationReference.setDisplay(openmrsLocation.getDisplayString());
			extension.setValue(locationReference);
		}
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
		return extension != null && extension.hasUrl() && extension.hasValue()
		        && extension.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE) && extension.hasExtension()
		        && extension.getExtensionFirstRep().getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE)
		        && extension.getExtensionFirstRep().hasValue();
	}
	
	private String extractAttributeTypeName(Extension extension) {
		return extension.getValue().toString();
	}
	
	private void setPersonAttributeValue(PersonAttribute personAttribute, Type extensionValue) {
		if (extensionValue instanceof BooleanType) {
			personAttribute.setValue(((BooleanType) extensionValue).getValueAsString());
		} else if (extensionValue instanceof StringType) {
			personAttribute.setValue(((StringType) extensionValue).getValue());
		} else if (extensionValue instanceof Reference) {
			String reference = ((Reference) extensionValue).getReference();
			Optional<String> locationUUID = FhirUtils.referenceToId(reference);
			if (locationUUID.isPresent()) {
				Location location = locationService.getLocationByUuid(locationUUID.get());
				if (location != null) {
					personAttribute.setValue(location.getId().toString());
				}
			}
		} else if (extensionValue instanceof CodeableConcept) {
			Concept concept = conceptTranslator.toOpenmrsType((CodeableConcept) extensionValue);
			if (concept != null) {
				personAttribute.setValue(concept.getConceptId().toString());
			}
		} else {
			log.warn("Extension value type: {} is not supported for PersonAttribute value",
			    extensionValue.getClass().getSimpleName());
		}
	}
}
