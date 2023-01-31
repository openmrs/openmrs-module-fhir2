package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class MedicationRequestStatusTranslatorImpl_2_2 implements MedicationRequestStatusTranslator {
	
	@Override
	public MedicationRequest.MedicationRequestStatus toFhirResource(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		
		if (drugOrder.getFulfillerStatus() != null
		        && drugOrder.getFulfillerStatus().equals(Order.FulfillerStatus.COMPLETED)) {
			return MedicationRequest.MedicationRequestStatus.COMPLETED;
		} else if (drugOrder.isActive()) {
			return MedicationRequest.MedicationRequestStatus.ACTIVE;
		} else if (drugOrder.isDiscontinuedRightNow() || drugOrder.getVoided()) {
			return MedicationRequest.MedicationRequestStatus.CANCELLED;
		} else if (drugOrder.isExpired()) {
			return MedicationRequest.MedicationRequestStatus.STOPPED;
		}
		return MedicationRequest.MedicationRequestStatus.UNKNOWN;
	}
	
}
