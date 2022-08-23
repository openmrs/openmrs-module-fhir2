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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.module.fhir2.api.util.FhirUtils;

/**
 * FHIR Reference - https://www.hl7.org/fhir/references.html
 */
@Data
@Entity
@Table(name = "fhir_reference")
public class FhirReference extends BaseOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	public FhirReference() {
		setName("");
	}
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "reference_id")
	private Integer id;
	
	@Column(name = "target_type")
	private String type;
	
	@Column(name = "target_uuid")
	private String targetUuid;
	
	@Column(name = "reference")
	private String reference;
	
	public void setReference(String reference) {
		this.reference = reference;
		
		if (type == null) {
			Optional<String> possibleType = FhirUtils.referenceToType(reference);
			possibleType.ifPresent(t -> type = t);
		}
		
		Optional<String> possibleUuid = FhirUtils.referenceToId(reference);
		possibleUuid.ifPresent(uuid -> targetUuid = uuid);
	}
}
