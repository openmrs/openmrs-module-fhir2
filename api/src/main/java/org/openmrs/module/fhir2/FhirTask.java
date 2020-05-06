/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsMetadata;

@Data(staticConstructor = "of")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_task")
public class FhirTask extends BaseOpenmrsMetadata {
	
	// Based on https://www.hl7.org/fhir/task.html v4.0.1
	public enum TaskStatus {
		REQUESTED,
		REJECTED,
		ACCEPTED,
		COMPLETED,
		UNKNOWN
	}
	
	public enum TaskIntent {
		ORDER
	}
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "task_id")
	private Integer id;
	
	/**
	 * The current status of the task.
	 */
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private TaskStatus status;
	
	/**
	 * Indicates the "level" of actionability associated with the Task, i.e. i+R[9]Cs this a proposed
	 * task, a planned task, an actionable task, etc.
	 */
	@Column(name = "intent", nullable = false)
	@Enumerated(EnumType.STRING)
	private TaskIntent intent;
	
	/**
	 * BasedOn refers to a higher-level authorization that triggered the creation of the task. It
	 * references a "request" resource such as a ServiceRequest, MedicationRequest, ServiceRequest,
	 * CarePlan, etc. which is distinct from the "request" resource the task is seeking to fulfill. This
	 * latter resource is referenced by FocusOn. For example, based on a ServiceRequest (= BasedOn), a
	 * task is created to fulfill a procedureRequest ( = FocusOn ) to collect a specimen from a patient.
	 */
	@OneToMany
	@JoinTable(name = "fhir_task_based_on_reference", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "reference_id"))
	private Set<FhirReference> basedOnReferences;
	
	/**
	 * The entity who benefits from the performance of the service specified in the task (e.g., the
	 * patient).
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "for_reference_id", referencedColumnName = "reference_id")
	private FhirReference forReference;
	
	/**
	 * The healthcare event (e.g. a patient and healthcare provider interaction) during which this task
	 * was created.
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "encounter_reference_id", referencedColumnName = "reference_id")
	private FhirReference encounterReference;
	
	/**
	 * Individual organization or Device currently responsible for task execution.
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "owner_reference_id", referencedColumnName = "reference_id")
	private FhirReference ownerReference;
	
	/**
	 * Additional information that may be needed in the execution of the task. see
	 * https://www.hl7.org/fhir/task-definitions.html#Task.input
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id")
	private Set<FhirTaskInput> input;
	
	/**
	 * Outputs produced by the Task. see https://www.hl7.org/fhir/task-definitions.html#Task.output
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id")
	
	private Set<FhirTaskOutput> output;
}
