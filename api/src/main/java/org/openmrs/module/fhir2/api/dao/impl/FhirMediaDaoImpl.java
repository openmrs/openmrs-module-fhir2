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

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
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

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.hibernate.criterion.Restrictions.eq;

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
					entry.getValue().forEach(createdTime -> handleDate(createdTime.getPropertyName(),
							(DateParam) createdTime.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.MEDIA_CONTENT_TYPE:
					entry.getValue().forEach(contentType -> handleMediaContentType(criteria, (StringAndListParam) contentType.getParam()));
					break;
				case FhirConstants.CONTENT_DATA:
					entry.getValue().forEach(contentData -> handleContentData(criteria, (StringAndListParam) contentData.getParam()));
					break;
				case FhirConstants.CONTENT_TITLE:
					entry.getValue().forEach(contentTitle -> handleContentTitle(criteria, (StringAndListParam) contentTitle.getParam()));
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
		if(mediaType != null){
			if(lacksAlias(criteria, "mt")){
				criteria.createAlias("mediaType", "mt");
				handleAndListParam(mediaType, (tag) -> Optional.of(eq("mt.type", tag.getValue()))).ifPresent(criteria::add);
			}
		}
	}

	private void handleMediaSubject(Criteria criteria, ReferenceAndListParam mediaSubject) {
		if(mediaSubject != null){
			if(lacksAlias(criteria, "ms")){
				criteria.createAlias("mediaSubject", "ms");
				handleAndListParam(mediaSubject, (tag) -> Optional.of(eq("ms.subject", tag.getValue()))).ifPresent(criteria::add);

			}
		}
	}

	private void handleMediaEncounterReference(Criteria criteria, ReferenceAndListParam encounterReference) {
		if(encounterReference != null){
			if(lacksAlias(criteria, "er")){
				criteria.createAlias("mediaEncounterReference", "er");
				handleAndListParam(encounterReference, (tag) -> Optional.of(eq("er.encounter", tag.getValue()))).ifPresent(criteria::add);
			}
		}
	}

//	private void handleMediaContentType(Criteria criteria, TokenAndListParam mediaContentType) {
//		if(mediaContentType != null){
//			if(lacksAlias(criteria, "ty")){
//				criteria.createAlias("mediaContentType", "ty");
//				handleAndListParam(mediaContentType, (tag) -> Optional.of(eq("ty.modality", tag.getValue()))).ifPresent(criteria::add);
//			}
//		}
//	}

	private void handleMediaContentType(Criteria criteria, StringAndListParam mediaContentType) {
		handleAndListParam(mediaContentType, (type) -> propertyLike("comments", type)).ifPresent(criteria::add);
	}

//	private void handleContentData(Criteria criteria, StringAndListParam contentData) {
//		if(contentData != null){
//			if(lacksAlias(criteria, "cd")){
//				criteria.createAlias("mediaContentData", "cd");
//				handleAndListParam(contentData, (tag) -> Optional.of(eq("cd.content", tag.getValue()))).ifPresent(criteria::add);
//			}
//		}
//	}


	private void handleContentData(Criteria criteria, StringAndListParam contentData) {
		handleAndListParam(contentData, (data) -> propertyLike("contentData", data)).ifPresent(criteria::add);
	}

	private void handleContentTitle(Criteria criteria, StringAndListParam contentTitle) {
		handleAndListParam(contentTitle, (title) -> propertyLike("contentTitle", title)).ifPresent(criteria::add);
	}
}
