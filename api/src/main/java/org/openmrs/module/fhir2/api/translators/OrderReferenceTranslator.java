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

/**
 * This interface represents a generic means of resolving an Openmrs order to either a service
 * request or medication request reference.
 * 
 * @Since 2.8.1
 */
public interface OrderReferenceTranslator extends OpenmrsFhirTranslator<Order, Reference> {}
