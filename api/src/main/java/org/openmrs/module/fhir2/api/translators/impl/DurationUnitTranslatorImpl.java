package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class DurationUnitTranslatorImpl implements DurationUnitTranslator {

	LocalDate dateRetired, dateCreated;

	@Override
	public Timing.UnitsOfTime toFhirResource(@Nonnull DrugOrder drugOrder) {

		try {
			dateRetired = LocalDate
					.parse(new SimpleDateFormat("yyyy-MM-dd").format(drugOrder.getDurationUnits().getDateRetired()));

			dateCreated = LocalDate
					.parse(new SimpleDateFormat("yyyy-MM-dd").format(drugOrder.getDurationUnits().getDateCreated()));
		}
		catch (Exception e) {
			return Timing.UnitsOfTime.NULL;
		}

		if ((dateRetired.getYear() - dateCreated.getYear()) > 0) {
			return Timing.UnitsOfTime.A;
		} else if ((dateRetired.getMonth().getValue() - dateCreated.getMonth().getValue()) > 0) {
			return Timing.UnitsOfTime.MO;
		} else {
			return Timing.UnitsOfTime.D;
		}
	}
}
