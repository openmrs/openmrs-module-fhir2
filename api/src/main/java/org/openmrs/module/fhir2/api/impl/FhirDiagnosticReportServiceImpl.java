/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirDiagnosticReportServiceImpl extends BaseFhirService<DiagnosticReport, org.openmrs.Obs> implements FhirDiagnosticReportService {
	
	@Autowired
	FhirDiagnosticReportDao dao;
	
	@Autowired
	DiagnosticReportTranslator translator;
	
	@Override
	public DiagnosticReport getDiagnosticReportByUuid(String uuid) {
		return translator.toFhirResource(dao.getObsGroupByUuid(uuid));
	}
	
	@Override
	public DiagnosticReport saveDiagnosticReport(DiagnosticReport diagnosticReport) {
		return translator.toFhirResource(dao.saveObsGroup(translator.toOpenmrsType(diagnosticReport)));
	}
	
	@Override
	public DiagnosticReport updateDiagnosticReport(String uuid, DiagnosticReport diagnosticReport) {
		if (diagnosticReport.getId() == null) {
			throw new InvalidRequestException("Diagnostic Report resource is missing id.");
		}
		
		if (!diagnosticReport.getId().equals(uuid)) {
			throw new InvalidRequestException("Diagnostic Report id and provided id do not match.");
		}
		
		Obs obsGroup = dao.getObsGroupByUuid(uuid);
		
		if (obsGroup == null) {
			throw new MethodNotAllowedException("No Diagnostic Report found to update.");
		}
		
		return translator.toFhirResource(dao.saveObsGroup(translator.toOpenmrsType(obsGroup, diagnosticReport)));
	}
	
	@Override
	protected FhirDao<Obs> getDao() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected OpenmrsFhirTranslator<Obs, DiagnosticReport> getTranslator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DiagnosticReport get(String uuid) {
		// TODO Auto-generated method stub
		return super.get(uuid);
	}
	
	@Override
	public DiagnosticReport create(DiagnosticReport newResource) {
		// TODO Auto-generated method stub
		return super.create(newResource);
	}
	
	@Override
	public DiagnosticReport update(String uuid, DiagnosticReport updatedResource) {
		// TODO Auto-generated method stub
		return super.update(uuid, updatedResource);
	}
	
}
