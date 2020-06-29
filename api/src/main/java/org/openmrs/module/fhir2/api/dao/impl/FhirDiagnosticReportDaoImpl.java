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

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Obs;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleEncounterReference("e", (ReferenceAndListParam) param.getParam())
					        .ifPresent(c -> criteria.createAlias("encounter", "e").add(c)));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam(), "person"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCodedConcept(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange("dateCreated", (DateRangeParam) param.getParam()).ifPresent(criteria::add));
					break;
			}
		});
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteria, "c")) {
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
