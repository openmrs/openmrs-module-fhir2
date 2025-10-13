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

import static org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40.convertResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.providers.util.TaskVersionConverter;

public class SearchQueryBundleProviderR3Wrapper implements IBundleProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final IBundleProvider bundleProvider;
	
	public SearchQueryBundleProviderR3Wrapper(IBundleProvider bundleProvider) {
		this.bundleProvider = bundleProvider;
	}
	
	@Override
	public String getCurrentPageId() {
		return bundleProvider.getCurrentPageId();
	}
	
	@Override
	public String getNextPageId() {
		return bundleProvider.getNextPageId();
	}
	
	@Override
	public String getPreviousPageId() {
		return bundleProvider.getPreviousPageId();
	}
	
	@Override
	public IPrimitiveType<Date> getPublished() {
		return bundleProvider.getPublished();
	}
	
	@Override
	@Nonnull
	public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
		return bundleProvider.getResources(theFromIndex, theToIndex).stream().map(this::transformToR3)
		        .filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Override
	@Nullable
	public String getUuid() {
		return bundleProvider.getUuid();
	}
	
	@Override
	public Integer preferredPageSize() {
		return bundleProvider.preferredPageSize();
	}
	
	@Override
	@Nullable
	public Integer size() {
		return bundleProvider.size();
	}
	
	private IBaseResource transformToR3(IBaseResource resource) {
		if (resource instanceof org.hl7.fhir.dstu3.model.Resource) {
			return resource;
		} else if (resource instanceof Resource) {
			if (resource instanceof Task) {
				return TaskVersionConverter.convertTask((Task) resource);
			}
			
			if (resource instanceof Condition) {
				Condition condition = ((Condition) resource).copy();
				if (FhirUtils.getOpenmrsConditionType(condition)
				        .filter(type -> type == FhirUtils.OpenmrsConditionType.DIAGNOSIS).isPresent()) {
					condition.setClinicalStatus(null);
					return convertResource(condition);
				}
			}
			
			return convertResource((Resource) resource);
		}
		
		return null;
	}
}
