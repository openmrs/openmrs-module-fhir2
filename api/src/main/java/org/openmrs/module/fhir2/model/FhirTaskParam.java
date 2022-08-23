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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class FhirTaskParam extends BaseOpenmrsMetadata {
	
	@ManyToOne
	@JoinColumn(name = "task_id")
	protected FhirTask task;
	
	@ManyToOne
	@JoinColumn(name = "type_id", referencedColumnName = "concept_id", nullable = false)
	protected Concept type;
	
	@Column(name = "value_datetime")
	protected Date valueDatetime;
	
	@Column(name = "value_numeric")
	protected Double valueNumeric;
	
	@Column(name = "value_text")
	protected String valueText;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "value_reference_id")
	protected FhirReference valueReference;
}
