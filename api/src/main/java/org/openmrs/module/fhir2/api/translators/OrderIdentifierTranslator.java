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

import javax.annotation.Nonnull;

import java.util.List;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.Order;

public interface OrderIdentifierTranslator extends OpenmrsFhirTranslator<Order, Identifier> {
	
	@Override
	public Identifier toFhirResource(@Nonnull Order order);
	
	/**
	 * Translates the order number and, if present, the accession number of the given order into a list
	 * of FHIR Identifiers.
	 *
	 * @param order the order to translate
	 * @return a list containing the order number identifier and, if present, the accession number
	 *         identifier
	 */
	public List<Identifier> toFhirIdentifiers(@Nonnull Order order);
	
}
