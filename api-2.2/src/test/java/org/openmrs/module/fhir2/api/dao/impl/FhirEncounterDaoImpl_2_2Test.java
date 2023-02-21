package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasOrListParam;
import ca.uhn.fhir.rest.param.HasParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirEncounterDaoImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final Integer ENCOUNTER_WITH_DRUG_ORDERS_ID = 3;
	
	private static final String ENCOUNTER_WITH_DRUG_ORDERS = "6519d653-393b-4118-9c83-a3715b82d4ac"; // 3 in standard test dataset
	
	private static final Integer ENCOUNTER_WITH_NO_DRUG_ORDERS_ID = 4;
	
	private static final String ENCOUNTER_WITH_NO_DRUG_ORDERS = "eec646cb-c847-45a7-98bc-91c8c4f70add"; // 4 in standard test dataset
	
	private static final Integer ENCOUNTER_WITH_ONLY_CANCELlED_DRUG_ORDER = 2003;
	
	private static final Integer ENCOUNTER_WITH_ONLY_EXPIRED_DRUG_ORDER = 2004;
	
	private static final Integer ENCOUNTER_WITH_ONLY_COMPLETED_DRUG_ORDER = 2005;
	
	private static final String ENCOUNTER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImpl_2_2Test_initial_data.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirEncounterDaoImpl_2_2 dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirEncounterDaoImpl_2_2();
		dao.setSessionFactory(sessionFactory);
		
		executeDataSet(ENCOUNTER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldOnlyReturnEncountersThatHaveAssociatedMedicationRequests() {
		Encounter withNoDrugOrders = dao.get(ENCOUNTER_WITH_NO_DRUG_ORDERS);
		assertThat(withNoDrugOrders, notNullValue());
		assertThat("Orders is empty", withNoDrugOrders.getOrders().isEmpty());
		
		Encounter withDrugOrders = dao.get(ENCOUNTER_WITH_DRUG_ORDERS);
		assertThat(withDrugOrders, notNullValue());
		assertThat("Orders is not empty", !withDrugOrders.getOrders().isEmpty());
		for (Order order : withDrugOrders.getOrders()) {
			assertThat(order.getClass(), equalTo(DrugOrder.class));
		}
		
		HasOrListParam hasOrListParam = new HasOrListParam();
		hasOrListParam.add(new HasParam("MedicationRequest", "encounter", "intent", "order"));
		HasAndListParam hasAndListParam = new HasAndListParam();
		hasAndListParam.addAnd(hasOrListParam);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER,
		    hasAndListParam);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		assertThat("Encounter with Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_DRUG_ORDERS_ID)));
		assertThat("Encounter with only cancelled Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_ONLY_CANCELlED_DRUG_ORDER)));
		assertThat("Encounter with only expired Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_ONLY_EXPIRED_DRUG_ORDER)));
		assertThat("Encounter with only completed Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_ONLY_COMPLETED_DRUG_ORDER)));
		assertThat("Encounter without Drug Orders is not returned", matchingResourceIds,
		    not(hasItem(equalTo(ENCOUNTER_WITH_NO_DRUG_ORDERS_ID))));
	}
	
	@Test
	public void shouldExcludedEncountersWithOnlyCompletedMedicationRequestsWhenReturnEncountersThatHaveAssociatedMedicationRequests() {
		Encounter withNoDrugOrders = dao.get(ENCOUNTER_WITH_NO_DRUG_ORDERS);
		assertThat(withNoDrugOrders, notNullValue());
		assertThat("Orders is empty", withNoDrugOrders.getOrders().isEmpty());
		
		Encounter withDrugOrders = dao.get(ENCOUNTER_WITH_DRUG_ORDERS);
		assertThat(withDrugOrders, notNullValue());
		assertThat("Orders is not empty", !withDrugOrders.getOrders().isEmpty());
		for (Order order : withDrugOrders.getOrders()) {
			assertThat(order.getClass(), equalTo(DrugOrder.class));
		}
		
		HasOrListParam hasOrListParam = new HasOrListParam();
		hasOrListParam.add(new HasParam("MedicationRequest", "encounter", "status:not", "completed"));
		HasAndListParam hasAndListParam = new HasAndListParam();
		hasAndListParam.addAnd(hasOrListParam);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER,
		    hasAndListParam);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		assertThat("Encounter with Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_DRUG_ORDERS_ID)));
		assertThat("Encounter with only cancelled Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_ONLY_CANCELlED_DRUG_ORDER)));
		assertThat("Encounter with only expired Drug Orders is returned", matchingResourceIds,
		    hasItem(equalTo(ENCOUNTER_WITH_ONLY_EXPIRED_DRUG_ORDER)));
		assertThat("Encounter with only completed Drug Orders is returned", matchingResourceIds,
		    not(hasItem(equalTo(ENCOUNTER_WITH_ONLY_COMPLETED_DRUG_ORDER))));
		assertThat("Encounter without Drug Orders is not returned", matchingResourceIds,
		    not(hasItem(equalTo(ENCOUNTER_WITH_NO_DRUG_ORDERS_ID))));
	}
	
}
