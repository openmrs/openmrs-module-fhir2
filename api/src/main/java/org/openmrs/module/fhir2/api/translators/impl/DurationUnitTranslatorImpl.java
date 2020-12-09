package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

import javax.annotation.Nonnull;

public class DurationUnitTranslatorImpl implements DurationUnitTranslator {

	@Override
	public Timing.UnitsOfTime toFhirResource(@Nonnull DrugOrder drugOrder) {

		String unitUUID = drugOrder.getDurationUnits().getUuid();

		switch (unitUUID) {

			case "162583":
				return Timing.UnitsOfTime.S;
			case "1733":
				return Timing.UnitsOfTime.MIN;
			case "1822":
				return Timing.UnitsOfTime.H;
			case "1072":
				return Timing.UnitsOfTime.D;
			case "1073":
				return Timing.UnitsOfTime.WK;
			case "1074":
				return Timing.UnitsOfTime.MO;
			case "1734":
				return Timing.UnitsOfTime.A;
			default:
				return Timing.UnitsOfTime.NULL;
		}
	}
}
