/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
public class SearchQuery<O extends FhirDao, T extends ToFhirTranslator & UpdatableOpenmrsTranslator> implements ISearchQuery<O, T> {
	
	@Override
	public IBundleProvider getQueryResults(SearchParameterMap theParams, O dao, T translator) {
		
		return new IBundleProvider() {
			
			@Override
			public IPrimitiveType<Date> getPublished() {
				return InstantType.withCurrentTime();
			}
			
			@Nonnull
			@Override
			@SuppressWarnings("unchecked")
			public List<IBaseResource> getResources(int fromIndex, int toIndex) {
				Criteria criteria = dao.search(theParams);
				if (toIndex - fromIndex > 0) {
					criteria.setFirstResult(fromIndex);
					criteria.setMaxResults(toIndex - fromIndex);
				}
				
				return (List<IBaseResource>) criteria.list().stream().map(translator::toFhirResource)
				        .collect(Collectors.toList());
			}
			
			@Nullable
			@Override
			public String getUuid() {
				return null;
			}
			
			@Override
			public Integer preferredPageSize() {
				return null;
			}
			
			@Nullable
			@Override
			public Integer size() {
				return null;
			}
		};
	}
	
}
