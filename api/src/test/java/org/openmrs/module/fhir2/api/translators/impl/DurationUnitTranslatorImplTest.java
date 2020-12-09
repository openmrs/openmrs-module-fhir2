package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(MockitoJUnitRunner.class)
public class DurationUnitTranslatorImplTest {

	private DrugOrder drugOrder;

	private Timing.UnitsOfTime result;

	private DurationUnitTranslator durationUnitTranslator;

	private Concept concept;

	private String SECONDS_UUID = "162583";

	private String MINUTES_UUID = "1733";

	private String HOUR_UUID = "1822";

	private String DAYS_UUID = "1072";

	private String WEEKS_UUID = "1073";

	private String MONTHS_UUID = "1074";

	private String YEARS_UUID = "1734";

	private String WRONG_UUID = "2909";

	@Before
	public void setup() {

		durationUnitTranslator = new DurationUnitTranslatorImpl();
		drugOrder = new DrugOrder();
		concept = new Concept();
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsNull() {

		concept.setUuid(WRONG_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);
		assertThat(result, equalTo(Timing.UnitsOfTime.NULL));

		result = durationUnitTranslator.toFhirResource(drugOrder);
		assertThat(result, equalTo(Timing.UnitsOfTime.NULL));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsSeconds() {

		concept.setUuid(SECONDS_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.S));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMinutes() {

		concept.setUuid(MINUTES_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsHours() {

		concept.setUuid(HOUR_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.H));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsWeeks() {

		concept.setUuid(WEEKS_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.WK));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsDay() {

		concept.setUuid(DAYS_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.D));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMonth() {

		concept.setUuid(MONTHS_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.MO));
		assertThat(result, notNullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsYear() {

		concept.setUuid(YEARS_UUID);
		drugOrder.setDurationUnits(concept);

		result = durationUnitTranslator.toFhirResource(drugOrder);

		assertThat(result, equalTo(Timing.UnitsOfTime.A));
		assertThat(result, notNullValue());
	}
}
