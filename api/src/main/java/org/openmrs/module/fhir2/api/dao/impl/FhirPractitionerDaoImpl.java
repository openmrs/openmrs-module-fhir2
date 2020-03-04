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

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPractitionerDaoImpl implements FhirPractitionerDao {
	
	@Inject
	@Named("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Provider getProviderByUuid(String uuid) {
		return (Provider) sessionFactory.getCurrentSession().createCriteria(Provider.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Provider> findProviderByName(String name) {
		return sessionFactory.getCurrentSession().createCriteria(Provider.class)
		        .add(and(eq("name", name), eq("retired", false))).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Provider> findProviderByIdentifier(String identifier) {
		return sessionFactory.getCurrentSession().createCriteria(Provider.class)
		        .add(Restrictions.eq("identifier", identifier)).list();
	}
	
	@Override
	public List<ProviderAttribute> getActiveAttributesByPractitionerAndAttributeTypeUuid(Provider provider,
	        String providerAttributeTypeUuid) {
		return (List<ProviderAttribute>) sessionFactory.getCurrentSession().createCriteria(ProviderAttribute.class)
		        .createAlias("provider", "p", JoinType.INNER_JOIN, eq("p.id", provider.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", providerAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
}
