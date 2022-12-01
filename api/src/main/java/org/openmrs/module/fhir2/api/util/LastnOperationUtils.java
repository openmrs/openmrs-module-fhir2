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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The LastNOperationUtils class has functions which perform the same operations in $lastn and
 * $lastn-encounters operation
 */
public final class LastnOperationUtils {
	
	/**
	 * @param list List of resources
	 * @param max The value of `N`, which specifies the maximum count of distinct effective datetime
	 * @return The list of resource uuids
	 */
	public static <T> List<T> getTopNRankedIds(List<LastnResult<T>> list, int max) {
		list.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));
		List<T> results = new ArrayList<>(list.size());
		
		int currentRank = 0;
		
		for (int var = 0; var < list.size() && currentRank < max; var++) {
			currentRank++;
			results.add(list.get(var).getId());
			Date currentDate = list.get(var).getDatetime();
			
			if (var == list.size() - 1) {
				return results;
			}
			
			//Adding all objects which have the same Datetime as the current object Datetime since they will have the same rank
			Date nextDate = list.get(var + 1).getDatetime();
			while (nextDate.equals(currentDate)) {
				results.add(list.get(var + 1).getId());
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
