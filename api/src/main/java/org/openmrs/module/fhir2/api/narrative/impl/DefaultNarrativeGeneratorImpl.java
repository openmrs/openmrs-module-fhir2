/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.narrative.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Narrative;
import org.openmrs.module.fhir2.api.narrative.DefaultNarrativeGenerator;

public class DefaultNarrativeGeneratorImpl implements DefaultNarrativeGenerator {
	
	@Override
	public Narrative generateDefaultNarrative(IBaseResource iBaseResource) {
		if (resourcesWithHAPIDefaultNarratives.contains(iBaseResource.getClass().getSimpleName())) {
			return generateHAPIDefaultNarrative(iBaseResource);
		}
		return null;
	}
	
	@Override
	public Narrative generateHAPIDefaultNarrative(IBaseResource iBaseResource) {
		FhirContext fhirContext = FhirContext.forR4();
		fhirContext.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		String narrativeStr = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(iBaseResource);
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(narrativeStr);
		JsonObject narrativeText = jsonObject.getAsJsonObject("text");
		
		Narrative narrative = new Narrative();
		narrative.setStatusAsString(narrativeText.get("status").getAsString());
		narrative.setDivAsString(narrativeText.get("div").getAsString());
		
		return narrative;
	}
}
