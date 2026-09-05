/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import org.hl7.fhir.r4.model.Immunization;

public interface FhirImmunizationService extends FhirService<Immunization> {
	
	IBundleProvider searchImmunizations(ReferenceAndListParam patientParam, SortSpec sort);
	
	/**
	 * Search immunizations with date range support for reminder functionality
	 * 
	 * @param patientParam patient reference parameter
	 * @param dateRangeParam date range parameter for vaccination dates
	 * @param sort sort specification
	 * @return bundle provider with immunization results
	 */
	IBundleProvider searchImmunizations(ReferenceAndListParam patientParam, DateRangeParam dateRangeParam, SortSpec sort);
	
	/**
	 * Get all immunizations for a specific patient (for patient profile integration)
	 * 
	 * @param patientUuid patient UUID
	 * @return list of immunizations for the patient
	 */
	List<Immunization> getImmunizationsByPatient(String patientUuid);
	
	/**
	 * Get upcoming vaccinations that are due within the specified number of days
	 * 
	 * @param days number of days to look ahead
	 * @param patientUuid optional patient UUID to filter by patient
	 * @return list of immunizations that are due soon
	 */
	List<Immunization> getUpcomingVaccinations(int days, String patientUuid);
	
	/**
	 * Get missed vaccinations (past due date)
	 * 
	 * @param patientUuid optional patient UUID to filter by patient
	 * @return list of immunizations that are past due
	 */
	List<Immunization> getMissedVaccinations(String patientUuid);
	
	/**
	 * Get vaccination coverage statistics
	 * 
	 * @param startDate start date for coverage period
	 * @param endDate end date for coverage period
	 * @param patientUuid optional patient UUID to filter by patient
	 * @return vaccination coverage report data
	 */
	VaccinationCoverageReport getVaccinationCoverage(Date startDate, Date endDate, String patientUuid);
	
	/**
	 * Data class for vaccination coverage reporting
	 */
	class VaccinationCoverageReport {
		
		private long totalPatients;
		
		private long vaccinatedPatients;
		
		private long totalVaccinations;
		
		private double coveragePercentage;
		
		private List<VaccinationTypeCoverage> typeCoverage;
		
		public VaccinationCoverageReport() {
		}
		
		public long getTotalPatients() {
			return totalPatients;
		}
		
		public void setTotalPatients(long totalPatients) {
			this.totalPatients = totalPatients;
		}
		
		public long getVaccinatedPatients() {
			return vaccinatedPatients;
		}
		
		public void setVaccinatedPatients(long vaccinatedPatients) {
			this.vaccinatedPatients = vaccinatedPatients;
		}
		
		public long getTotalVaccinations() {
			return totalVaccinations;
		}
		
		public void setTotalVaccinations(long totalVaccinations) {
			this.totalVaccinations = totalVaccinations;
		}
		
		public double getCoveragePercentage() {
			return coveragePercentage;
		}
		
		public void setCoveragePercentage(double coveragePercentage) {
			this.coveragePercentage = coveragePercentage;
		}
		
		public List<VaccinationTypeCoverage> getTypeCoverage() {
			return typeCoverage;
		}
		
		public void setTypeCoverage(List<VaccinationTypeCoverage> typeCoverage) {
			this.typeCoverage = typeCoverage;
		}
	}
	
	/**
	 * Data class for vaccination type coverage statistics
	 */
	class VaccinationTypeCoverage {
		
		private String vaccineType;
		
		private String vaccineCode;
		
		private long totalGiven;
		
		private long patientsVaccinated;
		
		public VaccinationTypeCoverage() {
		}
		
		public String getVaccineType() {
			return vaccineType;
		}
		
		public void setVaccineType(String vaccineType) {
			this.vaccineType = vaccineType;
		}
		
		public String getVaccineCode() {
			return vaccineCode;
		}
		
		public void setVaccineCode(String vaccineCode) {
			this.vaccineCode = vaccineCode;
		}
		
		public long getTotalGiven() {
			return totalGiven;
		}
		
		public void setTotalGiven(long totalGiven) {
			this.totalGiven = totalGiven;
		}
		
		public long getPatientsVaccinated() {
			return patientsVaccinated;
		}
		
		public void setPatientsVaccinated(long patientsVaccinated) {
			this.patientsVaccinated = patientsVaccinated;
		}
	}
	
}
