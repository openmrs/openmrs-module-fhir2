package org.openmrs.module.fhir2.api.dao.impl;

import java.util.Optional;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirMedicationRequestDaoImpl_2_2 extends FhirMedicationRequestDaoImpl implements FhirMedicationRequestDao {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleFulfillerStatus((TokenAndListParam) param.getParam()).ifPresent(criteria::add));
			}
		});
		super.setupSearchParams(criteria, theParams);
	}
	
	private Optional<Criterion> handleFulfillerStatus(TokenAndListParam tokenAndListParam) {
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				return Optional.of(
				    generateFulfillerStatusRestriction(Order.FulfillerStatus.valueOf(token.getValue().toUpperCase())));
			}
			return Optional.empty();
		});
	}
	
	protected Criterion generateFulfillerStatusRestriction(Order.FulfillerStatus fulfillerStatus) {
		return generateFulfillerStatusRestriction("", fulfillerStatus);
	}
	
	protected Criterion generateFulfillerStatusRestriction(String path, Order.FulfillerStatus fulfillerStatus) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return Restrictions.eq(path + "fulfillerStatus", fulfillerStatus);
	}
	
}
