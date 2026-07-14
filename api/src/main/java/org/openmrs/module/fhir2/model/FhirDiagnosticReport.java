/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.model;

import static org.openmrs.module.fhir2.api.util.GeneralUtils.replaceContents;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_diagnostic_report")
public class FhirDiagnosticReport extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "diagnostic_report_id")
	private Integer id;
	
	@Column(nullable = false, columnDefinition = "varchar(50)")
	@Enumerated(EnumType.STRING)
	private DiagnosticReportStatus status;
	
	@ManyToOne
	@JoinColumn(name = "concept_id", referencedColumnName = "concept_id", nullable = false)
	private Concept code;
	
	@ManyToOne
	@JoinColumn(name = "subject_id", referencedColumnName = "patient_id")
	private Patient subject;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id", referencedColumnName = "encounter_id")
	private Encounter encounter;
	
	@Column(name = "issued")
	private Date issued;
	
	@OneToMany
	@JoinTable(name = "fhir_diagnostic_report_performers", joinColumns = @JoinColumn(name = "diagnostic_report_id"), inverseJoinColumns = @JoinColumn(name = "provider_id"))
	private final Set<Provider> performers = new LinkedHashSet<>();
	
	public void setPerformers(Set<Provider> performers) {
		replaceContents(this.performers, performers);
	}
	
	@OneToMany
	@JoinTable(name = "fhir_diagnostic_report_results", joinColumns = @JoinColumn(name = "diagnostic_report_id"), inverseJoinColumns = @JoinColumn(name = "obs_id"))
	private final Set<Obs> results = new LinkedHashSet<>();
	
	public void setResults(Set<Obs> results) {
		replaceContents(this.results, results);
	}
	
	/**
	 * @Since 2.8.1
	 * @param conclusion of results
	 * @return conclusion of results
	 */
	@Column(name = "conclusion", length = 1024)
	private String conclusion;
	
	/**
	 * @Since 2.8.1
	 * @param orders references to service requests the report is based on
	 * @return orders the report is based on
	 */
	@OneToMany
	@JoinTable(name = "fhir_diagnostic_report_service_request", joinColumns = @JoinColumn(name = "diagnostic_report_id"), inverseJoinColumns = @JoinColumn(name = "order_id"))
	private final Set<Order> orders = new LinkedHashSet<>();
	
	public void setOrders(Set<Order> orders) {
		replaceContents(this.orders, orders);
	}
	
	public enum DiagnosticReportStatus {
		REGISTERED,
		PARTIAL,
		PRELIMINARY,
		AMENDED,
		CANCELLED,
		FINAL,
		UNKNOWN
	}
}
