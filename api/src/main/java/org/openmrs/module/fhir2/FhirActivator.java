/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
@SuppressWarnings("unused")
public class FhirActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(FhirActivator.class);
	
	@Override
	public void started() {
		log.info("Started FHIR");
	}
	
	@Override
	public void stopped() {
		log.info("Shutdown FHIR");
	}
	
}
