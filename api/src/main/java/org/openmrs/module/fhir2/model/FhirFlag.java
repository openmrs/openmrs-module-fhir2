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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.openmrs.Auditable;
import org.openmrs.Retireable;
import org.openmrs.User;

/*
 * This class is model based on https://www.hl7.org/fhir/flag.html
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_flags")
public class FhirFlag implements Auditable, Retireable {
	
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
	
	private String flag;
	
	@Column(name = "start_date")
	private Date startDate;
	
	@Column(name = "end_date")
	private Date endDate;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "subject_reference_id", referencedColumnName = "reference_id")
	private FhirReference subjectReference;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", updatable = false)
	protected User creator;
	
	@Column(name = "date_created", nullable = false, updatable = false)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "retired", nullable = false)
	@Field
	private Boolean retired = Boolean.FALSE;
	
	@Column(name = "date_retired")
	private Date dateRetired;
	
	@ManyToOne
	@JoinColumn(name = "retired_by")
	private User retiredBy;
	
	@Column(name = "retire_reason")
	private String retireReason;
	
	@Column(name = "uuid", unique = true, nullable = false, length = 36)
	private String uuid = UUID.randomUUID().toString();
	
	@Override
	public Boolean isRetired() {
		return retired;
	}
	
}
