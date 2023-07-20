/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.openmrs.module.fhir2.FhirConstants.CODED_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_GROUPING_CONCEPT;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;
import org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper;
import org.openmrs.module.fhir2.api.util.JsonPatchUtils;
import org.openmrs.module.fhir2.api.util.XmlPatchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirImmunizationServiceImpl extends BaseFhirService<Immunization, Obs> implements FhirImmunizationService {
	
	@Autowired
	@Getter
	private ImmunizationTranslator translator;
	
	@Autowired
	@Getter
	private FhirObservationDao dao;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private ImmunizationObsGroupHelper helper;
	
	@Autowired
	private SearchQueryInclude<Immunization> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Obs, Immunization, FhirObservationDao, ImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;
	
	@Override
	public Immunization create(@Nonnull Immunization newImmunization) {
		if (newImmunization == null) {
			throw new InvalidRequestException("A resource of type Immunization must be supplied");
		}
		
		Obs obs = translator.toOpenmrsType(newImmunization);
		
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		
		validateObject(obs);
		
		obs = obsService.saveObs(obs, "Created when translating a FHIR Immunization resource.");
		
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization update(@Nonnull String uuid, @Nonnull Immunization updatedImmunization) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (updatedImmunization == null) {
			throw new InvalidRequestException("Resource cannot be null.");
		}
		
		if (updatedImmunization.getId() == null) {
			throw new InvalidRequestException("Immunization resource is missing id.");
		}
		
		if (!updatedImmunization.getIdElement().getIdPart().equals(uuid)) {
			throw new InvalidRequestException("Immunization id does not match resource id.");
		}
		
		Obs existingImmunization = dao.get(uuid);
		
		if (existingImmunization == null) {
			throw resourceNotFound(uuid);
		}
		
		Obs obs = translator.toOpenmrsType(existingImmunization, updatedImmunization);
		
		validateObject(obs);
		
		return translator.toFhirResource(obsService.saveObs(obs, "Updated as part of a FHIR update"));
	}
	
	@Override
	public Immunization patch(@Nonnull String uuid, @Nonnull PatchTypeEnum patchType, @Nonnull String body,
	        RequestDetails requestDetails) {
		
		if (uuid == null) {
			throw new InvalidRequestException("id cannot be null");
		}
		
		Obs existingImmunizationObs = dao.get(uuid);
		
		if (existingImmunizationObs == null) {
			throw resourceNotFound(uuid);
		}
		
		OpenmrsFhirTranslator<Obs, Immunization> translator = getTranslator();
		
		Immunization existingImmunization = translator.toFhirResource(existingImmunizationObs);
		Immunization updatedImmunizationFhirObject = null;
		
		switch (patchType) {
			case JSON_PATCH:
				if (isJsonMergePatch(requestDetails)) {
					updatedImmunizationFhirObject = JsonPatchUtils.applyJsonMergePatch(fhirContext, existingImmunization,
					    body);
				} else {
					updatedImmunizationFhirObject = JsonPatchUtils.applyJsonPatch(fhirContext, existingImmunization, body);
				}
				break;
			case XML_PATCH:
				updatedImmunizationFhirObject = XmlPatchUtils.applyXmlPatch(fhirContext, existingImmunization, body);
				break;
		}
		
		return applyUpdate(existingImmunizationObs, updatedImmunizationFhirObject);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		Obs obs = dao.get(uuid);
		
		if (obs == null) {
			throw resourceNotFound(uuid);
		}
		
		obsService.voidObs(obs, "Voided via FHIR API");
	}
	
	@Override
	public IBundleProvider searchImmunizations(ReferenceAndListParam patientParam, SortSpec sort) {
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(PATIENT_REFERENCE_SEARCH_HANDLER, patientParam);
		
		TokenAndListParam conceptParam = new TokenAndListParam();
		TokenParam token = new TokenParam();
		token.setValue(Integer.toString(helper.concept(IMMUNIZATION_GROUPING_CONCEPT).getId()));
		conceptParam.addAnd(token);
		
		theParams.addParameter(CODED_SEARCH_HANDLER, conceptParam);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	protected Immunization applyUpdate(Obs existingObject, Immunization updatedResource) {
		OpenmrsFhirTranslator<Obs, Immunization> translator = getTranslator();
		
		org.openmrs.Obs updatedObject;
		if (translator instanceof OpenmrsFhirUpdatableTranslator) {
			UpdatableOpenmrsTranslator<org.openmrs.Obs, Immunization> updatableOpenmrsTranslator = (OpenmrsFhirUpdatableTranslator<org.openmrs.Obs, Immunization>) translator;
			updatedObject = updatableOpenmrsTranslator.toOpenmrsType(existingObject, updatedResource);
		} else {
			updatedObject = translator.toOpenmrsType(updatedResource);
		}
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(Context.getObsService().saveObs(updatedObject, "Updated via the FHIR2 API"));
	}
	
	@Override
	protected void validateObject(Obs obs) {
		super.validateObject(obs);
		helper.validateImmunizationObsGroup(obs);
	}
}
