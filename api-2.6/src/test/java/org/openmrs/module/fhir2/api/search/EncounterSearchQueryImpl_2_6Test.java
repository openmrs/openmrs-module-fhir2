package org.openmrs.module.fhir2.api.search;


import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class EncounterSearchQueryImpl_2_6Test extends BaseModuleContextSensitiveTest {

    public static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";

    public static final int START_INDEX = 0;

    public static final int END_INDEX = 10;

    @Autowired
    private FhirEncounterDao dao;

    @Autowired
    private EncounterTranslator<org.openmrs.Encounter> translator;

    @Autowired
    private SearchQueryInclude<Encounter> searchQueryInclude;

    @Autowired
    private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;


    private List<IBaseResource> get(IBundleProvider results) {
        return results.getResources(START_INDEX, END_INDEX);
    }

    private IBundleProvider search(SearchParameterMap theParams) {
        return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
    }

    @Before
    public void setup() {
        executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
        updateSearchIndex();
    }


    @Test
    public void searchForEncounters_shouldReverseIncludeMedicationRequestsAndAssociatedMedicationDispensesWithReturnedResults() {
        TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
        HashSet<Include> revIncludes = new HashSet<>();
        revIncludes.add(new Include("MedicationRequest:encounter"));
        revIncludes.add(new Include("MedicationDispense:authorizingPrescription", true));

        SearchParameterMap theParams = new SearchParameterMap()
                .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
                .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);

        IBundleProvider results = search(theParams);

        assertThat(results, notNullValue());
        assertThat(results.size(), equalTo(1));

        List<IBaseResource> resultList = get(results);

        // TODO add the appropriate assertions here

        assertThat(results, notNullValue());
        assertThat(resultList.size(), equalTo(9)); // reverse included resources added as part of the result list
        assertThat(resultList.subList(1, 9), everyItem(allOf(is(instanceOf(MedicationRequest.class)),
                hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID)))))));
    }
}
