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
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.ConceptSource;

/**
 * This class provides a means of mappings between the way FHIR represents different code systems
 * and how they are represented in OpenMRS's concept dictionary.
 * <p/>
 * In FHIR, concepts are identified by the "code system" they belong to and this "code system" is,
 * in turn, normally represented by a URL. For example, concepts drawn from LOINC are usually
 * identified by the URL "http://loinc.org".
 * <p/>
 * In order to properly map between the representation of concepts in FHIR, e.g., <pre>{@code
 *  {
 *      "system": "http://loinc.org",
 *      "code": "44564-3"
 *  }
 * }</pre> and the way concepts are represented in the OpenMRS concept dictionary (using a
 * {@link ConceptSource}, a {@link org.openmrs.ConceptReferenceTerm}, and a
 * {@link org.openmrs.ConceptReferenceTermMap}), we need a way of translating this URL into a
 * {@link ConceptSource}, which is the mapping this class provides.
 * <p />
 * While it's less common, occasionally FHIR will use URNs rather than URLs to represent code
 * systems, e.g., in order to support legacy HL7 code systems or value sets that are uniquely
 * identified by OIDs. These are also supported by this class, even though we follow FHIR in calling
 * the property "url".
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_concept_source")
public class FhirConceptSource extends BaseOpenmrsMetadata {
	
	private static final long serialVersionUID = 1742113L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "fhir_concept_source_id")
	private Integer id;
	
	@OneToOne
	@JoinColumn(name = "concept_source_id")
	private ConceptSource conceptSource;
	
	@Column(nullable = false)
	private String url;
}
