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

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_task")
public class FhirTask extends BaseOpenmrsMetadata {
	
	/**
	 * Based on <a href="https://www.hl7.org/fhir/task.html">...</a> v4.0.1
	 */
	public enum TaskStatus {
		REQUESTED,
		READY,
		ONHOLD,
		CANCELLED,
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")
	private Integer id;
	
	/**
	 * The current status of the task.
	 */
	@Column(name = "status", nullable = false, columnDefinition = "varchar(255)")
	@Enumerated(EnumType.STRING)
	private TaskStatus status;
	
	/**
	 * Indicates the "level" of actionability associated with the Task, i.e. i+R[9]Cs this a proposed
	 * task, a planned task, an actionable task, etc.
	 */
	@Column(name = "intent", nullable = false, columnDefinition = "varchar(255)")
	@Enumerated(EnumType.STRING)
	private TaskIntent intent;
	
	/**
	 * BasedOn refers to a higher-level authorization that triggered the creation of the task. It
	 * references a "request" resource such as a ServiceRequest, MedicationRequest, ServiceRequest,
	 * CarePlan, etc. which is distinct from the "request" resource the task is seeking to fulfill. This
	 * latter resource is referenced by FocusOn. For example, based on a ServiceRequest (= BasedOn), a
	 * task is created to fulfill a procedureRequest ( = FocusOn ) to collect a specimen from a patient.
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_task_based_on_reference", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "reference_id"))
	private final Set<FhirReference> basedOnReferences = new LinkedHashSet<>();
	
	public void setBasedOnReferences(Set<FhirReference> basedOnReferences) {
		this.basedOnReferences.clear();
		if (basedOnReferences != null) {
			this.basedOnReferences.addAll(basedOnReferences);
		}
	}
	
	/**
	 * The entity who benefits from the performance of the service specified in the task (e.g., the
	 * patient).
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "for_reference_id", referencedColumnName = "reference_id")
	private FhirReference forReference;
	
	/**
	 * The healthcare event (e.g. a patient and healthcare provider interaction) during which this task
	 * was created.
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "encounter_reference_id", referencedColumnName = "reference_id")
	private FhirReference encounterReference;
	
	/**
	 * Individual organization or Device currently responsible for task execution.
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "owner_reference_id", referencedColumnName = "reference_id")
	private FhirReference ownerReference;
	
	/**
	 * Additional information that may be needed in the execution of the task. see
	 * <a href="https://www.hl7.org/fhir/task-definitions.html#Task.input">...</a>
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id")
	private final Set<FhirTaskInput> input = new LinkedHashSet<>();
	
	public void setInput(Set<FhirTaskInput> input) {
		this.input.clear();
		if (input != null) {
			this.input.addAll(input);
		}
	}
	
	/**
	 * Outputs produced by the Task. see
	 * <a href="https://www.hl7.org/fhir/task-definitions.html#Task.output">...</a>
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id")
	private final Set<FhirTaskOutput> output = new LinkedHashSet<>();
	
	public void setOutput(Set<FhirTaskOutput> output) {
		this.output.clear();
		if (output != null) {
			this.output.addAll(output);
		}
	}
	
	/**
	 * The location Where task occurs
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "location_reference_id", referencedColumnName = "reference_id")
	private FhirReference locationReference;
	
	/**
	 * Type of Task
	 */
	@ManyToOne
	@JoinColumn(name = "task_code", referencedColumnName = "concept_id")
	protected Concept taskCode;
	
	/**
	 * Task that this particular task is part of.
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_task_part_of_reference", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "reference_id"))
	private final Set<FhirReference> partOfReferences = new LinkedHashSet<>();
	
	public void setPartOfReferences(Set<FhirReference> partOfReferences) {
		this.partOfReferences.clear();
		if (partOfReferences != null) {
			this.partOfReferences.addAll(partOfReferences);
		}
	}
	
	/**
	 * Actual start time of the execution
	 */
	@Column(name = "execution_start_time")
	private Date executionStartTime;
	
	/**
	 * Actual end time of the execution
	 */
	@Column(name = "execution_end_time")
	private Date executionEndTime;
	
	/**
	 * Comment made about the task
	 */
	@Column(name = "comment")
	private String comment;
	
}
