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

import static org.hibernate.criterion.Restrictions.eq;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.openmrs.Obs;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirDiagnosticReportDaoImpl extends BaseFhirDao<Obs> implements FhirDiagnosticReportDao {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	@Qualifier("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public Obs getObsGroupByUuid(String uuid) {
		return (Obs) sessionFactory.getCurrentSession().createCriteria(Obs.class).createAlias("groupMembers", "group")
		        .add(eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public Obs saveObsGroup(Obs obs) throws DAOException {
		if (!obs.isObsGrouping()) {
			throw new IllegalArgumentException("Provided Obs must be an Obs grouping.");
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(obs);
		
		return obs;
	}
	
	@Override
	public Obs get(String uuid) {
		// TODO Auto-generated method stub
		return super.get(uuid);
	}
	
	@Override
	public Obs createOrUpdate(Obs newEntry) {
		// TODO Auto-generated method stub
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	public Obs delete(String uuid) {
		// TODO Auto-generated method stub
		return super.delete(uuid);
	}
	
	@Override
	public Long getResultCounts(SearchParameterMap theParams) {
		// TODO Auto-generated method stub
		return super.getResultCounts(theParams);
	}
	
	@Override
	public Integer getPreferredPageSize() {
		// TODO Auto-generated method stub
		return super.getPreferredPageSize();
	}
	
	@Override
	public Collection<Obs> search(SearchParameterMap theParams) {
		// TODO Auto-generated method stub
		return super.search(theParams);
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		// TODO Auto-generated method stub
		super.setupSearchParams(criteria, theParams);
	}
}
