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
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.openmrs.BaseOpenmrsData;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_contact_point_map")
public class FhirContactPointMap extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1742113L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "fhir_contact_point_map_id")
	private Integer id;
	
	@Column(name = "attribute_type_domain", nullable = false)
	private String attributeTypeDomain;
	
	@Column(name = "attribute_type_id", nullable = false)
	private Integer attributeTypeId;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ContactPointSystem system;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ContactPointUse use;
	
	private Integer rank;
}
