/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import java.security.SecureRandom;

public class FhirUtils {
	
	private static volatile SecureRandom numberGenerator = null;
	
	private static final long MSB = 0x8000000000000000L;
	
	public static String uniqueUuid() {
		SecureRandom generator = numberGenerator;
		if (generator == null) {
			numberGenerator = generator = new SecureRandom();
		}
		
		return Long.toHexString(MSB | generator.nextLong()) + Long.toHexString(MSB | generator.nextLong());
	}
}
