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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.RelationshipType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.RelationshipTranslator;
import org.springframework.stereotype.Component;

@Component
public class RelationshipTranslatorImpl implements RelationshipTranslator {
	
	@Override
	public CodeableConcept toFhirResource(RelationshipType relationshipType) {
		if (relationshipType == null) {
			return null;
		}
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(relationshipType.getbIsToA());
		coding.setSystem(FhirConstants.OPENMRS_FHIR_EXT_RELATIONSHIP_TYPE);
		codeableConcept.addCoding(coding);
		
		return codeableConcept;
	}
}
