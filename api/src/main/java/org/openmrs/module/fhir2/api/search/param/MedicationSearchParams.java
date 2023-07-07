/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search.param;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MedicationSearchParams extends BaseResourceSearchParams {
	
	private TokenAndListParam code;
	
	private TokenAndListParam dosageForm;
	
	private TokenAndListParam ingredientCode;
	
	@Builder
	public MedicationSearchParams(TokenAndListParam code, TokenAndListParam dosageForm, TokenAndListParam ingredientCode,
	    TokenAndListParam id, DateRangeParam lastUpdated, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, null, null, revIncludes);
		
		this.code = code;
		this.dosageForm = dosageForm;
		this.ingredientCode = ingredientCode;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER, getDosageForm())
		        .addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER, getIngredientCode());
	}
}
