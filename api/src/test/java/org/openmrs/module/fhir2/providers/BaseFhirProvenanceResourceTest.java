/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import java.util.Arrays;
import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Provenance;
import org.openmrs.module.fhir2.FhirConstants;

public abstract class BaseFhirProvenanceResourceTest<T extends DomainResource> {
	
	private static final String UPDATE = "Update";
	
	private static final String REVISE = "revise";
	
	private static final String CREATE = "CREATE";
	
	private static final String CREATE_DISPLAY = "create";
	
	public void setProvenanceResources(T resource) {
		resource.setContained(Arrays.asList(onCreateDataOperation(), onUpdateDataOperation()));
	}
	
	private Provenance onUpdateDataOperation() {
		Coding coding = new Coding();
		coding.setCode(UPDATE);
		coding.setDisplay(REVISE);
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		
		return new Provenance().setActivity(new CodeableConcept().addCoding(coding)).setRecorded(new Date());
	}
	
	private Provenance onCreateDataOperation() {
		Coding coding = new Coding();
		coding.setCode(CREATE);
		coding.setDisplay(CREATE_DISPLAY);
		coding.setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION);
		
		return new Provenance().setActivity(new CodeableConcept().addCoding(coding)).setRecorded(new Date());
	}
	
}
