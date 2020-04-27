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
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ProvenanceTranslatorImpl<T extends OpenmrsObject & Auditable> extends BaseProvenanceHandlingTranslator implements ProvenanceTranslator<T> {

	@Override
	public Provenance getCreateProvenance(T openMrsObject) {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(openMrsObject.getDateCreated());
		provenance.setActivity(createActivity());
		provenance.addAgent(createAgentComponent(openMrsObject.getCreator()));
		return provenance;
	}

	@Override
	public Provenance getUpdateProvenance(T openMrsObject) {
		if (openMrsObject.getDateChanged() == null && openMrsObject.getChangedBy() == null) {
			return null;
		}
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(openMrsObject.getDateChanged());
		provenance.setActivity(updateActivity());
		provenance.addAgent(createAgentComponent(openMrsObject.getChangedBy()));
		return provenance;
	}
}
