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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsData;

@Data(staticConstructor = "of")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fhir_task")
public class FhirTask extends BaseOpenmrsData {
	
	// Based on https://www.hl7.org/fhir/task.html v4.0.1
	// TODO: Support this valueset: https://www.hl7.org/fhir/valueset-task-status.html
	public enum TaskStatus {
		REQUESTED,
		REJECTED,
		ACCEPTED,
		COMPLETED
	};
	
	// TODO: Support this valueset: https://www.hl7.org/fhir/valueset-task-intent.html
	public enum TaskIntent {
		ORDER
	}
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "task_id")
	private Integer id;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private TaskStatus status;
	
	@Column(name = "status_reason")
	private String statusReason;
	
	@Column(name = "intent")
	@Enumerated(EnumType.STRING)
	private TaskIntent intent;
	
	/**
	 * Referenced resources represented with relative resource identifier string in the format of
	 * <ResourceName>/<ResourceId>.
	 */
	@Column(name = "based_on")
	private String basedOn;
	
	@ElementCollection
	@CollectionTable(name = "fhir_task_inputs", joinColumns = @JoinColumn(name = "task_input_id"))
	@Column(name = "input")
	private Collection<String> inputs;
	
	@ElementCollection
	@CollectionTable(name = "fhir_task_outputs", joinColumns = @JoinColumn(name = "task_output_id"))
	@Column(name = "output")
	private Collection<String> outputs;
	
	@Column(name = "description")
	private String description;
	
}
