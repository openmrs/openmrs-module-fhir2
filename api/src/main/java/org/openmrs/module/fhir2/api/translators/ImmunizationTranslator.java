/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.openmrs.Obs;

public interface ImmunizationTranslator extends OpenmrsFhirUpdatableTranslator<Obs, org.hl7.fhir.r4.model.Immunization> {
	
	public static final String immunizationGroupingConcept = "CIEL:1421";
	
	public static final String[] immunizationConcepts = { "CIEL:984", "CIEL:1410", "CIEL:1418", "CIEL:1419", "CIEL:1420",
	        "CIEL:165907" };
	
	public static final String ciel984 = immunizationConcepts[0];
	
	public static final String ciel1410 = immunizationConcepts[1];
	
	public static final String ciel1418 = immunizationConcepts[2];
	
	public static final String ciel1419 = immunizationConcepts[3];
	
	public static final String ciel1420 = immunizationConcepts[4];
	
	public static final String ciel165907 = immunizationConcepts[5];
	
	/**
	 * Maps an OpenMRS immunization obs construct to a {@link org.hl7.fhir.r4.model.Immunization}
	 *
	 * @param openMrsImmunization the OpenMRS immunization to translate
	 * @return the corresponding FHIR immunization resource
	 */
	@Override
	org.hl7.fhir.r4.model.Immunization toFhirResource(Obs openMrsImmunization);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Immunization} to an OpenMRS immunization obs construct
	 *
	 * @param fhirImmunization the FHIR immunization to translate
	 * @return the corresponding OpenMRS immunization obs construct
	 */
	@Override
	Obs toOpenmrsType(org.hl7.fhir.r4.model.Immunization fhirImmunization);
}
