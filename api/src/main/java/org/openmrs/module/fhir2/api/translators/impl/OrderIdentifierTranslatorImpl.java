/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.Order;
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrderIdentifierTranslatorImpl implements OrderIdentifierTranslator {
	
	private static String ORDER_ID_TYPE = "Order Number";
	
	@Override
	public Identifier toFhirResource(@Nonnull Order order) {
		
		Identifier orderIdentifier = new Identifier();
		
		orderIdentifier.setType(new CodeableConcept().setText(ORDER_ID_TYPE));
		
		orderIdentifier.setValue(order.getOrderNumber());
		
		orderIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
		
		return orderIdentifier;
	}
	
}
