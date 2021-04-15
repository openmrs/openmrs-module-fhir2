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

import javax.annotation.Nonnull;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMediaDaoImpl extends BaseFhirDao<Obs> implements FhirMediaDao {
	
	@Autowired
	private ObsService obsService;
	
	@Override
	public Obs get(@Nonnull String uuid) {
		return obsService.getObsByUuid(uuid);
	}
	
	@Override
	public Obs createOrUpdate(@Nonnull Obs newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.MEDIA_STATUS:
					entry.getValue().forEach(status -> handleStatus(criteria, (TokenAndListParam) status.getParam()));
					break;
				case FhirConstants.MEDIA_TYPE:
					entry.getValue().forEach(type -> handleMediaType(criteria, (TokenAndListParam) type.getParam()));
					break;
				case FhirConstants.MEDIA_SUBJECT:
					entry.getValue()
					        .forEach(subject -> handleMediaSubject(criteria, (ReferenceAndListParam) subject.getParam()));
					break;
				case FhirConstants.MEDIA_ENCOUNTER_REFERENCE:
					break;
				case FhirConstants.MEDIA_CREATED_DATE_TIME:
					entry.getValue().forEach(
					    createdTime -> handleDate(createdTime.getPropertyName(), (DateParam) createdTime.getParam())
					            .ifPresent(criteria::add));
					break;
				case FhirConstants.MEDIA_CONTENT_TYPE:
					break;
				case FhirConstants.CONTENT_DATA:
					break;
				case FhirConstants.CONTENT_TITLE:
					break;
				case FhirConstants.CONTENT_DATE_OF_CREATION:
					entry.getValue().forEach(contentDateOfCreation -> handleDate(contentDateOfCreation.getPropertyName(),
					    (DateParam) contentDateOfCreation.getParam()));
					break;
			}
		});
	}
	
	//	private void handleStatus(Criteria criteria, TokenAndListParam status) {
	//		if(status != null){
	//			if(lacksAlias(criteria, "st")){
	//				criteria.createAlias("status", "st");
	//				handleAndListParam(status, (tag) -> Optional.of(eq("st.status", tag.getValue()))).ifPresent(criteria::add);
	//			}
	//		}
	//	}
	
	private void handleStatus(Criteria criteria, TokenAndListParam status) {
		//		handleAndListParam(status, (data) -> propertyLike("status", status)).ifPresent(criteria::add);
	}
	
	private void handleMediaType(Criteria criteria, TokenAndListParam mediaType) {
		if (mediaType != null) {
			if (lacksAlias(criteria, "mt")) {
				criteria.createAlias("mediaType", "mt");
				handleAndListParam(mediaType, (tag) -> Optional.of(eq("mt.type", tag.getValue()))).ifPresent(criteria::add);
			}
		}
	}
	
	private void handleMediaSubject(Criteria criteria, ReferenceAndListParam mediaSubject) {
		if (mediaSubject != null) {
			if (lacksAlias(criteria, "ms")) {
				criteria.createAlias("mediaSubject", "ms");
				handleAndListParam(mediaSubject, (tag) -> Optional.of(eq("ms.subject", tag.getValue())))
				        .ifPresent(criteria::add);
				
			}
		}
	}
}
