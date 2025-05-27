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

import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.api.util.FhirUtils.getMetadataTranslation;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.VisitTypeTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VisitTypeTranslatorImpl implements VisitTypeTranslator<VisitType> {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private VisitService visitService;
	
	@Override
	public List<CodeableConcept> toFhirResource(@Nonnull VisitType visitType) {
		if (visitType == null) {
			return null;
		}
		
		CodeableConcept code = new CodeableConcept();
		code.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode(visitType.getUuid())
		        .setDisplay(getMetadataTranslation(visitType));
		return Collections.singletonList(code);
	}
	
	@Override
	public VisitType toOpenmrsType(@Nonnull List<CodeableConcept> visitTypes) {
		if (visitTypes == null || visitTypes.isEmpty()) {
			return null;
		}
		
		Coding visitType = visitTypes.stream().filter(CodeableConcept::hasCoding)
		        .map(cc -> cc.getCoding().stream().filter(Coding::hasSystem)
		                .filter(c -> FhirConstants.VISIT_TYPE_SYSTEM_URI.equals(c.getSystem())).findFirst().orElse(null))
		        .filter(Objects::nonNull).findFirst().orElse(null);
		
		if (visitType == null) {
			return null;
		}
		
		return visitService.getVisitTypeByUuid(visitType.getCode());
	}
}
