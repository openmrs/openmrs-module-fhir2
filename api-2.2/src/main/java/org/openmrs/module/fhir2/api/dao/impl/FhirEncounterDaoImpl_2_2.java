/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirEncounterDaoImpl_2_2 extends FhirEncounterDaoImpl implements FhirEncounterDao {
	
	@Override
	protected Criterion generateNotCompletedOrderQuery(String path) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return Restrictions.or(Restrictions.isNull(path + "fulfillerStatus"),
		    Restrictions.ne(path + "fulfillerStatus", org.openmrs.Order.FulfillerStatus.COMPLETED));
		
	}
	
	@Override
	protected Criterion generateFulfillerStatusRestriction(String path, String fulfillerStatus) {
		
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return Restrictions.eq(path + "fulfillerStatus", Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase()));
	}
	
	@Override
	protected Criterion generateNotFulfillerStatusRestriction(String path, String fulfillerStatus) {
		
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return Restrictions.or(Restrictions.isNull(path + "fulfillerStatus"),
		    Restrictions.ne(path + "fulfillerStatus", Order.FulfillerStatus.valueOf(fulfillerStatus.toUpperCase())));
	}
}
