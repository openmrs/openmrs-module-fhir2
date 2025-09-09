/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hl7.fhir.r4.model.Patient;

public class IsDeceasedMatcher extends TypeSafeDiagnosingMatcher<Patient> {
	
	@Override
	protected boolean matchesSafely(Patient p, Description description) {
		if (!p.hasDeceased()) {
			description.appendText("patient does not have a deceased attribute");
			return false;
		} else if (p.hasDeceasedBooleanType() && !p.getDeceasedBooleanType().booleanValue()) {
			description.appendText("patient is not marked as deceased");
			return false;
		} else if (p.hasDeceasedDateTimeType() && p.getDeceasedDateTimeType().isEmpty()) {
			description.appendText("patient does not have a deceased date");
			return false;
		} else {
			description.appendText("patient is marked as deceased");
			return true;
		}
	}
	
	@Override
	public void describeTo(Description description) {
		description.appendText("a patient who is deceased");
	}
}
