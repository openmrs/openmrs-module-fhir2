package org.openmrs.module.fhir2.api.dao;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public final class FhirDaoUtils {
	
	public static Criterion createActiveOrderCriterion(String path) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		// ACTIVE = date activated less than or equal to today, date stopped null or in the future, auto expire date null or in the future
		return Restrictions.and(Restrictions.le(path + "dateActivated", new Date()),
		    Restrictions.or(Restrictions.isNull(path + "dateStopped"), Restrictions.gt(path + "dateStopped", new Date())),
		    Restrictions.or(Restrictions.isNull(path + "autoExpireDate"),
		        Restrictions.gt(path + "autoExpireDate", new Date())));
	}
}
