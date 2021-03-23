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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Patient;

/*
 * This class is model based on https://www.hl7.org/fhir/flag.html
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_flags")
public class FhirFlag extends BaseOpenmrsMetadata {
	
	// Based on https://www.hl7.org/fhir/valueset-flag-priority.html
	public enum FlagPriority {
		NONE,
		LOW,
		MEDIUM,
		HIGH
	}
	
	// Based on https://www.hl7.org/fhir/valueset-flag-status.html
	public enum FlagStatus {
		ACTIVE,
		INACTIVE,
		ENTERED_IN_ERROR,
		NULL
	}
	
	private static final long serialVersionUID = 3L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "flag_id")
	private Integer id;
	
	@Column(name = "priority", nullable = false)
	@Enumerated(EnumType.STRING)
	private FlagPriority priority;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private FlagStatus status;
	
	@Column(name = "flag")
	private String flag;
	
	@Column(name = "start_date")
	private Date startDate;
	
	@Column(name = "end_date")
	private Date endDate;
	
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false, name = "patient_id")
	private Patient patient;
	
}
