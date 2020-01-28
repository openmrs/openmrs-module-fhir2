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

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirLocationDaoImpl implements FhirLocationDao {
	
	@Inject
	LocationService locationService;
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public Location getLocationByUuid(String uuid) {
		return locationService.getLocationByUuid(uuid);
	}
	
	@Override
	public Collection<Location> findLocationByName(String name) {
		return locationService.getLocations(name);
	}
	
	@Override
	public Collection<Location> findLocationsByCity(String city) {
		return sessionFactory.getCurrentSession().createCriteria(Location.class).add(Restrictions.eq("cityVillage", city))
		        .list();
	}
	
	@Override
	public Collection<Location> findLocationsByCountry(String country) {
		return sessionFactory.getCurrentSession().createCriteria(Location.class).add(Restrictions.eq("country", country))
		        .list();
	}
	
	@Override
	public Collection<Location> findLocationsByPostalCode(String postalCode) {
		return sessionFactory.getCurrentSession().createCriteria(Location.class)
		        .add(Restrictions.eq("postalCode", postalCode)).list();
	}
	
	@Override
	public Collection<Location> findLocationsByState(String state) {
		return sessionFactory.getCurrentSession().createCriteria(Location.class).add(Restrictions.eq("stateProvince", state))
		        .list();
	}
}
