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

import java.util.Arrays;
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
	public PersonAttribute toOpenmrsType(@Nonnull PersonAttribute personAttribute,
	        @Nonnull Extension personAttributeExtension) {
		if (!isValidPatientAttributeExtension(personAttributeExtension)) {
			return null;
		}
		
		String attributeTypeName = extractAttributeTypeName(personAttributeExtension);
		
		try {
			PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByName(attributeTypeName);
			if (personAttributeType != null) {
				personAttribute.setAttributeType(personAttributeType);
			}
		}
		catch (Exception e) {
			log.warn("Error encountered while setting person attribute type for name: {}", attributeTypeName);
		}
		
		Extension valueExtension = personAttributeExtension
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		if (valueExtension.hasValue()) {
			setPersonAttributeValue(personAttribute, valueExtension.getValue());
		}
		
		return personAttribute;
	}
	
	private Extension createPersonAttributeExtension(PersonAttribute personAttribute) {
		PersonAttributeType attributeType = personAttribute.getAttributeType();
		
		//Type Extension
		StringType personAttributeTypeValue = new StringType(attributeType.getName());
		Extension personAttributeTypeExtension = new Extension(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(personAttributeTypeValue);
		
		//Value Extension
		Extension valueExtension = new Extension(new UriType(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE));
		setValueExtension(valueExtension, personAttribute);
		
		//Person Attribute Extension
		Extension personAttributeExtension = new Extension(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		personAttributeExtension.setExtension(Arrays.asList(personAttributeTypeExtension, valueExtension));
		return personAttributeExtension;
	}
	
	private void setValueExtension(Extension valueExtension, PersonAttribute personAttribute) {
		String format = personAttribute.getAttributeType().getFormat();
		String value = personAttribute.getValue();
		
		switch (format) {
			case JAVA_STRING_FORMAT:
				valueExtension.setValue(new StringType(value));
				break;
			case JAVA_BOOLEAN_FORMAT:
				valueExtension.setValue(new BooleanType(value));
				break;
			case OPENMRS_LOCATION_FORMAT:
				handleLocationFormat(valueExtension, value);
				break;
			case OPENMRS_CONCEPT_FORMAT:
				handleConceptFormat(valueExtension, value);
				break;
			default:
				log.warn("extension has unsupported patient attribute type format: {}", format);
				valueExtension.setValue(null);
		}
	}
	
	private void handleLocationFormat(Extension valueExtension, String locationId) {
		if (locationId == null) {
			return;
		}
		
		Location openmrsLocation = locationService.getLocation(Integer.parseInt(locationId));
		
		if (openmrsLocation != null) {
			String locationUUID = openmrsLocation.getUuid();
			Reference locationReference = new Reference().setType(LOCATION_TYPE)
			        .setReference(LOCATION_TYPE + "/" + locationUUID);
			locationReference.setDisplay(openmrsLocation.getDisplayString());
			valueExtension.setValue(locationReference);
		}
	}
	
	private void handleConceptFormat(Extension valueExtension, String conceptId) {
		if (conceptId == null) {
			return;
		}
		
		Concept concept = conceptService.getConcept(conceptId);
		if (concept != null) {
			CodeableConcept codeableConcept = conceptTranslator.toFhirResource(concept);
			valueExtension.setValue(codeableConcept);
		}
		
	}
	
	private boolean isValidPatientAttributeExtension(Extension extension) {
		return extension != null && extension.hasUrl()
		        && extension.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE) && extension.hasExtension()
		        && extension.getExtension().size() == 2
		        && extension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE) != null
		        && extension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE) != null
		        && extension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE).hasValue()
		        && extension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE).hasValue();
	}
	
	private String extractAttributeTypeName(Extension personAttributeExtension) {
		return personAttributeExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE).getValue()
		        .toString();
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
