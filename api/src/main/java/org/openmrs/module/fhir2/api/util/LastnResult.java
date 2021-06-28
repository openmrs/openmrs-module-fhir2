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

/**
 * The class stores the result of $lastn type operations
 */
import java.util.Date;
import java.util.Map;

import lombok.Data;

@Data
public class LastnResult {
	
	private final String uuid;
	
	private final Date datetime;
	
	private final Map<String, Object> attributes;
	
	/**
	 * Constructor for observations in $lastn operation
	 * 
	 * @param uuid the observation uuid
	 * @param datetime the obsDatetime
	 * @param attributes the other attributes of the observation
	 */
	public LastnResult(Object uuid, Object datetime, Map<String, Object> attributes) {
		this.uuid = (String) uuid;
		this.datetime = (Date) datetime;
		this.attributes = attributes;
	}
	
	/**
	 * Constructor for encounters in $lastn-encounters operation
	 * 
	 * @param objects objects[0] holds the encounter uuid objects[1] holds the encounterDatetime
	 */
	public LastnResult(Object[] objects) {
		uuid = (String) objects[0];
		datetime = (Date) objects[1];
		attributes = null;
	}
}
