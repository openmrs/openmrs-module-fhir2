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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * FHIR Task.input - https://www.hl7.org/fhir/task-definitions.html#Task.input
 */
@Data(staticConstructor = "of")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "fhir_task_input")
public class FhirTaskInput extends FhirTaskParam {
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "task_input_id")
	private Integer id;
}
