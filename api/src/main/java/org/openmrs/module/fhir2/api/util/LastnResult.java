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

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

/**
 * The class stores the result of $lastn type operations
 */
@Data
public class LastnResult<T> {
	
	private final T id;
	
	private final Date datetime;
	
	private final Map<String, Object> attributes;
	
	/**
	 * Constructor to initialize the LastnResult object with resource uuid, effective datetime and a map
	 * of other attributes
	 * 
	 * @param id the resource id
	 * @param datetime the resource's effective datetime
	 * @param attributes the other attributes of the resource
	 */
	public LastnResult(T id, Date datetime, Map<String, Object> attributes) {
		Objects.requireNonNull(id, "id must not be null");
		this.id = id;
		this.datetime = datetime;
		this.attributes = attributes;
	}
	
	/**
	 * Constructor to initialize the LastnResult object with resource id and effective datetime
	 * 
	 * @param objects objects[0] holds the resource id objects[1] holds the resources's effective
	 *            datetime
	 */
	public LastnResult(Object[] objects) {
		Objects.requireNonNull(objects[0], "id must not be null");
		Objects.requireNonNull(objects[1], "datetime must not be null");
		id = (T) objects[0];
		datetime = (Date) objects[1];
		attributes = null;
	}
}
