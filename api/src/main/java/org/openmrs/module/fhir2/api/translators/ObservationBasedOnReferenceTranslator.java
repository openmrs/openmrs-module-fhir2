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

import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Order;

public interface ObservationBasedOnReferenceTranslator extends OpenmrsFhirTranslator<Order, Reference> {
	
	/**
	 * Maps an {@link Order} to an {@link org.hl7.fhir.r4.model.Reference}
	 *
	 * @param order the OpenMRS order element to translate
	 * @return
	 */
	@Override
	Reference toFhirResource(Order order);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Reference} to an {@link Order}
	 *
	 * @param reference the resource to map
	 * @return
	 */
	@Override
	Order toOpenmrsType(Reference reference);
}
