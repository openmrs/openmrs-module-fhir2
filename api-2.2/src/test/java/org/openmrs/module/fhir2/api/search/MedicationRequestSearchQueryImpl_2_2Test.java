package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class MedicationRequestSearchQueryImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final String INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImpl_2_2Test_initial_data.xml";
	
	private static final String DRUG_ORDER_WITH_FULFILLER_STATUS_COMPLETED_UUID = "ac463525-9b1a-44f2-99f1-0d6a10d5b60d";
	
	@Autowired
	private MedicationRequestTranslator translator;
	
	@Autowired
	private FhirMedicationRequestDao dao;
	
	@Autowired
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByFulfillerStatus() {
		// there are no tests in the test data set with "received" fulfiller status
		TokenAndListParam fulfillerStatus = new TokenAndListParam().addAnd(new TokenParam("received"));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER,
		    "fulfillerStatus", fulfillerStatus);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		assertThat(resultList, hasSize(equalTo(0)));
		
		// there is one tests in the test data set with "completed" fulfiller status
		fulfillerStatus = new TokenAndListParam().addAnd(new TokenParam("completed"));
		theParams = new SearchParameterMap().addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER, "fulfillerStatus",
		    fulfillerStatus);
		results = search(theParams);
		
		assertThat(results, notNullValue());
		resultList = get(results);
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(DRUG_ORDER_WITH_FULFILLER_STATUS_COMPLETED_UUID));
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<MedicationRequest> get(IBundleProvider results) {
		return results.getAllResources().stream().filter(it -> it instanceof MedicationRequest)
		        .map(it -> (MedicationRequest) it).collect(Collectors.toList());
	}
	
}
