package org.openmrs.module.fhir2.api.search;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class MedicationRequestSearchQueryImpl_2_6Test extends BaseModuleContextSensitiveTest {

    public static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";  // patient 2 in test dataset

    public static final String MEDICATION_REQUEST_UUID = "dfca4077-493c-496b-8312-856ee5d1cc26"; // order 2 in the test database;

    @Autowired
    private MedicationRequestTranslator translator;

    @Autowired
    private FhirMedicationRequestDao dao;

    @Autowired
    private SearchQueryInclude<MedicationRequest> searchQueryInclude;

    @Autowired
    private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;

    @Before
    public void setup() {
        executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
        updateSearchIndex();
    }

    private IBundleProvider search(SearchParameterMap theParams) {
        return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
    }

    private List<MedicationRequest> get(IBundleProvider results) {
        return results.getResources(0, 10).stream().filter(it -> it instanceof MedicationRequest)
                .map(it -> (MedicationRequest) it).collect(Collectors.toList());
    }


    // TODO want do work with drug order 2, with is linked to patient 2 and encounter 6

    // TODO change this test so that it is also requesting and testing associated medication dispenses

    @Test
    public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientUuid() {
        ReferenceAndListParam patientReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));

        SearchParameterMap theParams = new SearchParameterMap();
        theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);

        IBundleProvider results = search(theParams);

        assertThat(results, notNullValue());

        List<MedicationRequest> resultList = get(results);

        assertThat(resultList, not(empty()));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("efca4077-493c-496b-8312-856ee5d1cc27"))));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("2662e6c2-697b-11e3-bd76-0800271c1b75"))));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("e3d621f0-a4d5-47d1-a4e1-5ace3f66d43a"))));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("047b7424-6f33-4357-823c-420f316bb039"))));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("9c21e407-697b-11e3-bd76-0800271c1b75"))));
        assertThat(resultList, hasItem(hasProperty("id", equalTo("0c96f25c-4949-4f72-9931-d808fbc226db"))));
     //   assertThat(resultList, hasItem(hasProperty("id", equalTo("1c96f25c-4949-4f72-9931-d808fbc226de"))));
       // assertThat(resultList, hasItem(hasProperty("id", equalTo("2c96f25c-4949-4f72-9931-d808fbc226df"))));
      //  assertThat(resultList, hasItem(hasProperty("id", equalTo("4c96f25c-4949-4f72-9931-d808fbc226dh"))));


    }

}
