package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;

import javax.annotation.Nonnull;

public interface DurationUnitTranslator extends ToFhirTranslator<DrugOrder, Timing.UnitsOfTime> {

	@Override
	Timing.UnitsOfTime toFhirResource(@Nonnull DrugOrder drugOrder);
}
