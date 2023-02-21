package org.openmrs.module.fhir2.api.dao.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirEncounterDaoImpl_2_2 extends FhirEncounterDaoImpl implements FhirEncounterDao {
	
	@Override
	protected Criterion generateNotCompletedOrderQuery(String path) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return Restrictions.or(Restrictions.isNull(path + "fulfillerStatus"),
		    Restrictions.ne(path + "fulfillerStatus", org.openmrs.Order.FulfillerStatus.COMPLETED));
		
	}
}
