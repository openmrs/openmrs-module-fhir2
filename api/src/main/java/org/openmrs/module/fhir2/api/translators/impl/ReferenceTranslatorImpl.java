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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.fhir2.FhirReference;
import org.openmrs.module.fhir2.api.translators.ReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ReferenceTranslatorImpl implements ReferenceTranslator {
	
	@Override
	public Reference toFhirResource(FhirReference openmrsTask) {
		Reference fhirReference = null;
		
		if (openmrsTask != null) {
			fhirReference = new Reference();
			fhirReference.setType(openmrsTask.getType());
			fhirReference.setReference(openmrsTask.getReference());
		}
		
		return fhirReference;
	}
	
	@Override
	public FhirReference toOpenmrsType(Reference fhirReference) {
		FhirReference openmrsReference = null;
		
		if (fhirReference != null) {
			openmrsReference = new FhirReference();
			openmrsReference.setType(fhirReference.getType());
			openmrsReference.setReference(fhirReference.getReference());
			openmrsReference.setName(fhirReference.getType() + "/" + fhirReference.getReference());
		}
		
		return openmrsReference;
	}
	
	@Override
	public FhirReference toOpenmrsType(FhirReference openmrsReference, Reference fhirReference) {
		if (fhirReference != null) {
			if (openmrsReference == null) {
				openmrsReference = new FhirReference();
			}
			
			openmrsReference.setType(fhirReference.getType());
			openmrsReference.setReference(fhirReference.getReference());
			openmrsReference.setName(fhirReference.getType() + "/" + fhirReference.getReference());
		}
		
		return openmrsReference;
	}
}
