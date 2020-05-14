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
import org.openmrs.Obs;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirDiagnosticReportDaoImpl extends BaseFhirDao<Obs> implements FhirDiagnosticReportDao {
	
	@Override
	public Obs createOrUpdate(Obs newObs) throws DAOException {
		if (!newObs.isObsGrouping()) {
			throw new IllegalArgumentException("Provided Obs must be an Obs grouping.");
		}
		
		return super.createOrUpdate(newObs);
	}
	
}
