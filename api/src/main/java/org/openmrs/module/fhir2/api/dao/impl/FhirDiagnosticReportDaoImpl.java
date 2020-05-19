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

import javax.validation.constraints.NotNull;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hl7.fhir.r4.model.DiagnosticReport;
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
	
	@Override
	public Collection<Obs> searchForDiagnosticReports(ReferenceAndListParam encounterReference,
	        ReferenceAndListParam patientReference, DateRangeParam issueDate, TokenAndListParam code, SortSpec sort) {
		
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(Obs.class);
		
		handleEncounterReference("e", encounterReference).ifPresent(c -> criteria.createAlias("encounter", "e").add(c));
		handlePatientReference(criteria, patientReference, "person");
		handleCodedConcept(criteria, code);
		handleDateRange("dateCreated", issueDate).ifPresent(criteria::add);
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (!containsAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	@Override
	protected String paramToProp(@NotNull String paramName) {
		switch (paramName) {
			case DiagnosticReport.SP_ISSUED:
				return "dateCreated";
			default:
				return null;
		}
	}
	
}
