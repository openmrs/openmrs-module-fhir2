/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LastnResults {
	
	String uuid;
	
	Date datetime;
	
	Map<String, Object> attributes;
	
	public LastnResults(Object uuid, Object datetime, Map<String, Object> attributes) {
		this.uuid = (String) uuid;
		this.datetime = (Date) datetime;
		this.attributes = attributes;
	}
	
	public LastnResults(Object[] objects) {
		uuid = (String) objects[0];
		datetime = (Date) objects[1];
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Date getDatetime() {
		return datetime;
	}
	
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public static List<String> getTopNRankedUuids(List<LastnResults> list, int max) {
		list.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));
		List<String> results = new ArrayList<>(list.size());
		
		int currentRank = 0;
		
		for (int var = 0; var < list.size() && currentRank < max; var++) {
			currentRank++;
			results.add(list.get(var).getUuid());
			Date currentDate = list.get(var).getDatetime();
			
			if (var == list.size() - 1) {
				return results;
			}
			
			//Adding all objects which have the same Datetime as the current object Datetime since they will have the same rank
			Date nextDate = list.get(var + 1).getDatetime();
			while (nextDate.equals(currentDate)) {
				results.add(list.get(var + 1).getUuid());
				var++;
				
				if (var + 1 == list.size()) {
					return results;
				}
				nextDate = list.get(var + 1).getDatetime();
			}
		}
		return results;
	}
}
