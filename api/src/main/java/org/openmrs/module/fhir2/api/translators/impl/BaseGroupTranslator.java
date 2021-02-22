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

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.FhirConstants;

public abstract class BaseGroupTranslator {
	
	public Group toFhirResource(@Nonnull Cohort cohort) {
		notNull(cohort, "Cohort object should not be null");
		Group group = new Group();
		group.setId(cohort.getUuid());
		group.setActive(!cohort.getVoided());
		
		/*
		 * Apparently, cohort.description is a required field
		 */
		group.addExtension(new Extension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION)
		        .setValue(new StringType(cohort.getDescription())));
		
		// Not sure about this, It's either actual or descriptive
		// I will set actual - true temporarily as it required - valid resource.
		group.setActual(true);
		
		// Set to always person for now
		group.setType(Group.GroupType.PERSON);
		group.setName(cohort.getName());
		
		return group;
	}
	
	public Cohort toOpenmrsType(@Nonnull Cohort existingCohort, @Nonnull Group group) {
		notNull(group, "group resource object should not be null");
		notNull(existingCohort, "ExistingCohort object should not be null");
		
		if (group.hasId()) {
			existingCohort.setUuid(group.getId());
		}
		
		if (group.hasName()) {
			existingCohort.setName(group.getName());
		}
		
		if (group.hasActive()) {
			existingCohort.setVoided(!group.getActive());
		}
		
		Extension extension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		if (extension != null && extension.hasValue()) {
			existingCohort.setDescription(extension.getValue().toString());
		}
		
		return existingCohort;
	}
}
