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

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMediaDaoImpl extends BaseFhirDao<Obs> implements FhirMediaDao {

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
					entry.getValue().forEach(subject -> handleMediaSubject(criteria, (ReferenceAndListParam) subject.getParam()));
					break;
				case FhirConstants.MEDIA_ENCOUNTER_REFERENCE:
					entry.getValue().forEach(encounter -> handleMediaEncounterReference(criteria, (ReferenceAndListParam) encounter.getParam()));
					break;
				case FhirConstants.MEDIA_CREATED_DATE_TIME:
					entry.getValue().forEach(createdDateTime -> handleMediaCreatedDate(criteria, (DateRangeParam) createdDateTime.getParam()));
					break;
				case FhirConstants.MEDIA_CONTENT_TYPE:
					entry.getValue().forEach(contentType -> handleMediaContentType(criteria, (TokenAndListParam) contentType.getParam()));
					break;
				case FhirConstants.CONTENT_DATA:
					entry.getValue().forEach(contentData -> handleContentData(criteria, (StringAndListParam) contentData.getParam()));
					break;
				case FhirConstants.CONTENT_TITLE:
					entry.getValue().forEach(contentTitle -> handleContentTitle(criteria, (StringAndListParam) contentTitle.getParam()));
					break;
				case FhirConstants.CONTENT_DATE_OF_CREATION:
					entry.getValue().forEach(contentDateOfCreation -> handleContentDateOfCreation(criteria, (DateRangeParam) contentDateOfCreation.getParam()));
					break;
				
			}
		});
	}

	private void handleContentDateOfCreation(Criteria criteria, DateRangeParam contentDateOfCreation) {

	}

	private void handleContentTitle(Criteria criteria, StringAndListParam contentTitle) {
	}

	private void handleContentData(Criteria criteria, StringAndListParam contentData) {
	}

	private void handleMediaContentType(Criteria criteria, TokenAndListParam mediaContentType) {
	}

	private void handleMediaCreatedDate(Criteria criteria, DateRangeParam mediaCreatedDate) {
	}

	private void handleMediaEncounterReference(Criteria criteria, ReferenceAndListParam encounterReference) {
	}

	private void handleMediaSubject(Criteria criteria, ReferenceAndListParam mediaSubject) {
	}

	private void handleMediaType(Criteria criteria, TokenAndListParam mediaType) {
	}

	private void handleStatus(Criteria criteria, TokenAndListParam status) {
	}
}
