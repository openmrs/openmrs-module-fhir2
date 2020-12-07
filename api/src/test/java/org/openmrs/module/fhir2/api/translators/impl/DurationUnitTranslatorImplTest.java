package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(MockitoJUnitRunner.class)
public class DurationUnitTranslatorImplTest {

	private DrugOrder drugOrder;

	private Timing.UnitsOfTime result;

	private DurationUnitTranslator durationUnitTranslator;

	private Concept concept;

	@Before
	public void setup() {

		durationUnitTranslator = new DurationUnitTranslatorImpl();

		drugOrder = new DrugOrder();
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.APRIL, 16);
		concept = new Concept();
		concept.setDateCreated(calendar.getTime());
		drugOrder.setDurationUnits(concept);
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsNull() {

		result = durationUnitTranslator.toFhirResource(drugOrder);
		assertThat(result, equalTo(Timing.UnitsOfTime.NULL));

		result = durationUnitTranslator.toFhirResource(new DrugOrder());
		assertThat(result, equalTo(Timing.UnitsOfTime.NULL));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsDay() {

		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.APRIL, 20);
		concept.setDateRetired(calendar.getTime());
		drugOrder.setDurationUnits(concept);
		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.D));
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMonth() {

		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 29);
		concept.setDateRetired(calendar.getTime());
		drugOrder.setDurationUnits(concept);
		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.MO));
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsYear() {

		Calendar calendar = Calendar.getInstance();
		calendar.set(2021, Calendar.APRIL, 20);
		concept.setDateRetired(calendar.getTime());
		drugOrder.setDurationUnits(concept);
		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.A));
	}
}
