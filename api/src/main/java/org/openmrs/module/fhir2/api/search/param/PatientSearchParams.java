package org.openmrs.module.fhir2.api.search.param;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

import java.util.HashSet;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PatientSearchParams extends BaseResourceSearchParams {
	
	private StringAndListParam name;
	
	private StringAndListParam given;
	
	private StringAndListParam family;
	
	private TokenAndListParam identifier;
	
	private TokenAndListParam gender;
	
	private DateRangeParam birthDate;
	
	private DateRangeParam deathDate;
	
	private TokenAndListParam deceased;
	
	private StringAndListParam city;
	
	private StringAndListParam state;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam country;
	
	@Builder
	public PatientSearchParams(StringAndListParam name, StringAndListParam given, StringAndListParam family,
	    TokenAndListParam identifier, TokenAndListParam gender, DateRangeParam birthDate, DateRangeParam deathDate,
	    TokenAndListParam deceased, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	    StringAndListParam country, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, null, revIncludes);
		
		this.name = name;
		this.given = given;
		this.family = family;
		this.identifier = identifier;
		this.gender = gender;
		this.birthDate = birthDate;
		this.deathDate = deathDate;
		this.deceased = deceased;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.NAME_PROPERTY, getName())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, getGiven())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, getFamily())
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, getIdentifier())
		        .addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender", getGender())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "birthdate", getBirthDate())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "deathDate", getDeathDate())
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, getDeceased())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, getCity())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, getState())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, getPostalCode())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, getCountry());
	}
}
