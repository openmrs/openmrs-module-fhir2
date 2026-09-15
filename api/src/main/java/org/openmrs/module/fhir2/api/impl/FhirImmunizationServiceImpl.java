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
import static org.openmrs.module.fhir2.FhirConstants.DATE_RANGE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_GROUPING_CONCEPT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirImmunizationServiceImpl extends BaseFhirService<Immunization, Obs> implements FhirImmunizationService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirObservationDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ImmunizationTranslator translator;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ObsService obsService;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private EncounterService encounterService;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ImmunizationObsGroupHelper helper;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private SearchQueryInclude<Immunization> searchQueryInclude;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.Obs, Immunization, FhirObservationDao, ImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private PatientService patientService;
	
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
		return searchImmunizations(patientParam, null, sort);
	}
	
	@Override
	public IBundleProvider searchImmunizations(ReferenceAndListParam patientParam, DateRangeParam dateRangeParam,
	        SortSpec sort) {
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(PATIENT_REFERENCE_SEARCH_HANDLER, patientParam);
		
		if (dateRangeParam != null) {
			theParams.addParameter(DATE_RANGE_SEARCH_HANDLER, "obsDatetime", dateRangeParam);
		}
		
		TokenAndListParam conceptParam = new TokenAndListParam();
		TokenParam token = new TokenParam();
		token.setValue(Integer.toString(helper.concept(IMMUNIZATION_GROUPING_CONCEPT).getId()));
		conceptParam.addAnd(token);
		
		theParams.addParameter(CODED_SEARCH_HANDLER, conceptParam);
		theParams.setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	public List<Immunization> getImmunizationsByPatient(String patientUuid) {
		if (patientUuid == null) {
			throw new InvalidRequestException("Patient UUID cannot be null");
		}
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setValue(patientUuid);
		patientParam.addAnd(new ReferenceOrListParam().add(referenceParam));
		
		IBundleProvider bundleProvider = searchImmunizations(patientParam, null);
		List<Immunization> immunizations = new ArrayList<>();
		for (int i = 0; i < bundleProvider.size(); i++) {
			immunizations.addAll(bundleProvider.getResources(i, i + 1).stream().filter(Immunization.class::isInstance)
			        .map(Immunization.class::cast).collect(Collectors.toList()));
		}
		
		return immunizations;
	}
	
	@Override
	public List<Immunization> getUpcomingVaccinations(int days, @Nullable String patientUuid) {
		Date today = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		Date futureDate = calendar.getTime();
		
		DateRangeParam dateRangeParam = new DateRangeParam();
		dateRangeParam.setLowerBoundInclusive(today);
		dateRangeParam.setUpperBoundInclusive(futureDate);
		
		ReferenceAndListParam patientParam = null;
		if (patientUuid != null) {
			patientParam = new ReferenceAndListParam();
			ReferenceParam referenceParam = new ReferenceParam();
			referenceParam.setValue(patientUuid);
			patientParam.addAnd(new ReferenceOrListParam().add(referenceParam));
		}
		
		IBundleProvider bundleProvider = searchImmunizations(patientParam, dateRangeParam, null);
		List<Immunization> immunizations = new ArrayList<>();
		
		// Filter for immunizations with next dose date in the future
		for (int i = 0; i < bundleProvider.size(); i++) {
			List<Immunization> resources = bundleProvider.getResources(i, i + 1).stream()
			        .filter(Immunization.class::isInstance).map(Immunization.class::cast).collect(Collectors.toList());
			
			for (Immunization immunization : resources) {
				// Check for next dose date extension
				Extension nextDoseExtension = immunization
				        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_IMMUNIZATION_NEXT_DOSE_DATE);
				if (nextDoseExtension != null && nextDoseExtension.getValue() != null) {
					Date nextDoseDate = ((org.hl7.fhir.r4.model.DateTimeType) nextDoseExtension.getValue()).getValue();
					if (nextDoseDate != null && !nextDoseDate.before(today) && !nextDoseDate.after(futureDate)) {
						immunizations.add(immunization);
					}
				}
			}
		}
		
		return immunizations;
	}
	
	@Override
	public List<Immunization> getMissedVaccinations(@Nullable String patientUuid) {
		Date today = new Date();
		
		DateRangeParam dateRangeParam = new DateRangeParam();
		dateRangeParam.setUpperBoundExclusive(today);
		
		ReferenceAndListParam patientParam = null;
		if (patientUuid != null) {
			patientParam = new ReferenceAndListParam();
			ReferenceParam referenceParam = new ReferenceParam();
			referenceParam.setValue(patientUuid);
			patientParam.addAnd(new ReferenceOrListParam().add(referenceParam));
		}
		
		IBundleProvider bundleProvider = searchImmunizations(patientParam, dateRangeParam, null);
		List<Immunization> immunizations = new ArrayList<>();
		
		// Filter for immunizations with next dose date in the past
		for (int i = 0; i < bundleProvider.size(); i++) {
			List<Immunization> resources = bundleProvider.getResources(i, i + 1).stream()
			        .filter(Immunization.class::isInstance).map(Immunization.class::cast).collect(Collectors.toList());
			
			for (Immunization immunization : resources) {
				// Check for next dose date extension
				Extension nextDoseExtension = immunization
				        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_IMMUNIZATION_NEXT_DOSE_DATE);
				if (nextDoseExtension != null && nextDoseExtension.getValue() != null) {
					Date nextDoseDate = ((org.hl7.fhir.r4.model.DateTimeType) nextDoseExtension.getValue()).getValue();
					if (nextDoseDate != null && nextDoseDate.before(today)) {
						immunizations.add(immunization);
					}
				}
			}
		}
		
		return immunizations;
	}
	
	@Override
	public VaccinationCoverageReport getVaccinationCoverage(Date startDate, Date endDate, @Nullable String patientUuid) {
		VaccinationCoverageReport report = new VaccinationCoverageReport();
		
		DateRangeParam dateRangeParam = new DateRangeParam();
		if (startDate != null) {
			dateRangeParam.setLowerBoundInclusive(startDate);
		}
		if (endDate != null) {
			dateRangeParam.setUpperBoundInclusive(endDate);
		}
		
		ReferenceAndListParam patientParam = null;
		if (patientUuid != null) {
			patientParam = new ReferenceAndListParam();
			ReferenceParam referenceParam = new ReferenceParam();
			referenceParam.setValue(patientUuid);
			patientParam.addAnd(new ReferenceOrListParam().add(referenceParam));
		}
		
		IBundleProvider bundleProvider = searchImmunizations(patientParam, dateRangeParam, null);
		
		List<Immunization> allImmunizations = new ArrayList<>();
		for (int i = 0; i < bundleProvider.size(); i++) {
			allImmunizations.addAll(bundleProvider.getResources(i, i + 1).stream().filter(Immunization.class::isInstance)
			        .map(Immunization.class::cast).collect(Collectors.toList()));
		}
		
		report.setTotalVaccinations(allImmunizations.size());
		
		// Count unique patients
		Map<String, Boolean> patientMap = new HashMap<>();
		Map<String, VaccinationTypeCoverage> typeCoverageMap = new HashMap<>();
		Map<String, Map<String, Boolean>> vaccinePatientMap = new HashMap<>();
		
		for (Immunization immunization : allImmunizations) {
			if (immunization.getPatient() != null && immunization.getPatient().getReference() != null) {
				String patientRef = immunization.getPatient().getReference();
				patientMap.put(patientRef, true);
			}
			
			// Track vaccine type coverage
			if (immunization.getVaccineCode() != null && immunization.getVaccineCode().getCodingFirstRep() != null) {
				String vaccineCode = immunization.getVaccineCode().getCodingFirstRep().getCode();
				String vaccineDisplay = immunization.getVaccineCode().getCodingFirstRep().getDisplay();
				
				VaccinationTypeCoverage typeCoverage = typeCoverageMap.getOrDefault(vaccineCode,
				    new VaccinationTypeCoverage());
				typeCoverage.setVaccineCode(vaccineCode);
				typeCoverage.setVaccineType(vaccineDisplay != null ? vaccineDisplay : vaccineCode);
				typeCoverage.setTotalGiven(typeCoverage.getTotalGiven() + 1);
				
				if (immunization.getPatient() != null && immunization.getPatient().getReference() != null) {
					String patientRef = immunization.getPatient().getReference();
					Map<String, Boolean> patientsForVaccine = vaccinePatientMap.getOrDefault(vaccineCode, new HashMap<>());
					if (!patientsForVaccine.containsKey(patientRef)) {
						patientsForVaccine.put(patientRef, true);
						typeCoverage.setPatientsVaccinated(typeCoverage.getPatientsVaccinated() + 1);
						vaccinePatientMap.put(vaccineCode, patientsForVaccine);
					}
				}
				
				typeCoverageMap.put(vaccineCode, typeCoverage);
			}
		}
		
		report.setVaccinatedPatients(patientMap.size());
		
		// Calculate total patients (if not filtering by patient)
		if (patientUuid == null) {
			// Get total patient count from patient service
			// This is a simplified approach - in production, you might want to filter by active patients
			List<Patient> allPatients = patientService.getAllPatients();
			report.setTotalPatients(allPatients.size());
		} else {
			report.setTotalPatients(1);
		}
		
		// Calculate coverage percentage
		if (report.getTotalPatients() > 0) {
			report.setCoveragePercentage((double) report.getVaccinatedPatients() / report.getTotalPatients() * 100.0);
		} else {
			report.setCoveragePercentage(0.0);
		}
		
		// Set type coverage
		report.setTypeCoverage(new ArrayList<>(typeCoverageMap.values()));
		
		return report;
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
