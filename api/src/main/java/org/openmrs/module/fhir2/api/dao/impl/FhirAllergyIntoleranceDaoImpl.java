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
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.or;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
public class FhirAllergyIntoleranceDaoImpl extends BaseFhirDao<Allergy> implements FhirAllergyIntoleranceDao {
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public Allergy createOrUpdate(Allergy allergy) {
		Allergy savedAllergy = super.createOrUpdate(allergy);
		
		for (AllergyReaction reaction : allergy.getReactions()) {
			getSessionFactory().getCurrentSession().saveOrUpdate(reaction);
		}
		
		return savedAllergy;
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam(), "patient"));
					break;
				case FhirConstants.CATEGORY_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleAllergenCategory("allergen.allergenType", (TokenAndListParam) param.getParam())
					            .ifPresent(criteria::add));
					break;
				case FhirConstants.ALLERGEN_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleAllergen(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.SEVERITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleSeverity(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleManifestation(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.BOOLEAN_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleBoolean("voided", convertStringStatusToBoolean((TokenAndListParam) param.getParam()))
					            .ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	@Override
	protected Optional<Criterion> getCriteriaForLastUpdated(DateRangeParam param) {
		List<Optional<Criterion>> criterionList = new ArrayList<>();
		
		criterionList.add(handleDateRange("dateVoided", param));
		
		criterionList.add(Optional.of(
		    and(toCriteriaArray(Stream.of(Optional.of(isNull("dateVoided")), handleDateRange("dateChanged", param))))));
		
		criterionList.add(Optional.of(and(toCriteriaArray(Stream.of(Optional.of(isNull("dateVoided")),
		    Optional.of(isNull("dateChanged")), handleDateRange("dateCreated", param))))));
		
		return Optional.of(or(toCriteriaArray(criterionList)));
	}
	
	private void handleManifestation(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("reactions", "r");
			criteria.createAlias("r.reaction", "rc");
			
			handleCodeableConcept(criteria, code, "rc", "rcm", "rcrt").ifPresent(criteria::add);
		}
	}
	
	private void handleAllergen(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("allergen.codedAllergen", "ac");
			
			handleCodeableConcept(criteria, code, "ac", "acm", "acrt").ifPresent(criteria::add);
		}
	}
	
	private void handleSeverity(Criteria criteria, TokenAndListParam severityParam) {
		if (severityParam == null) {
			return;
		}
		Map<String, String> severityConceptUuids = globalPropertyService.getGlobalProperties(
		    FhirConstants.GLOBAL_PROPERTY_MILD, FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
		
		criteria.createAlias("severity", "sc");
		
		handleAndListParam(severityParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceSeverity severity = AllergyIntolerance.AllergyIntoleranceSeverity
				        .fromCode(token.getValue());
				switch (severity) {
					case MILD:
						return Optional.of(eq("sc.uuid", severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD)));
					case MODERATE:
						return Optional.of(eq("sc.uuid", severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE)));
					case SEVERE:
						return Optional.of(eq("sc.uuid", severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE)));
					case NULL:
						return Optional.of(eq("sc.uuid", severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_OTHER)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		}).ifPresent(criteria::add);
	}
	
	private Optional<Criterion> handleAllergenCategory(String propertyName, TokenAndListParam categoryParam) {
		if (categoryParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(categoryParam, token -> {
			try {
				AllergyIntolerance.AllergyIntoleranceCategory category = AllergyIntolerance.AllergyIntoleranceCategory
				        .fromCode(token.getValue());
				switch (category) {
					case FOOD:
						return Optional.of(eq(propertyName, AllergenType.FOOD));
					case MEDICATION:
						return Optional.of(eq(propertyName, AllergenType.DRUG));
					case ENVIRONMENT:
						return Optional.of(eq(propertyName, AllergenType.ENVIRONMENT));
					case NULL:
						return Optional.of(eq(propertyName, AllergenType.OTHER));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.empty();
		});
		
	}
	
	@Override
	protected String paramToProp(@NotNull String paramName) {
		switch (paramName) {
			case AllergyIntolerance.SP_SEVERITY:
				return "severity";
			default:
				return null;
		}
	}
}
