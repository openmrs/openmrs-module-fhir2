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

import java.util.Date;

import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;

public class FhirTranslatorUtils {
	
	public static Date getLastUpdated(OpenmrsObject object) {
		if (object instanceof Auditable) {
			Auditable auditable = (Auditable) object;
			
			if (auditable.getDateChanged() != null) {
				return auditable.getDateChanged();
			} else {
				return auditable.getDateCreated();
			}
		}
		
		return null;
	}
	
	public static String getVersionId(OpenmrsObject object) {
		Date lastUpdate = getLastUpdated(object);
		if (lastUpdate == null) {
			return null;
		}
		
		return String.valueOf(lastUpdate.getTime());
	}
	
}
