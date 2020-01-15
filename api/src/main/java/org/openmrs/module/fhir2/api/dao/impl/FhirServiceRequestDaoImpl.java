/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.FhirException;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestDaoImpl implements FhirServiceRequestDao {
	
	@Inject
	private OrderService orderService;
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public TestOrder getTestOrderByUuid(String uuid) {
		return (TestOrder) sessionFactory.getCurrentSession().createCriteria(TestOrder.class)
		        .add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
}
