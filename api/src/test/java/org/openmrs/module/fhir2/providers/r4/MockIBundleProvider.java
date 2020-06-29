/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;

public class MockIBundleProvider<U extends IBaseResource> implements IBundleProvider {
	
	private final Date datePublished;
	
	private final List<U> mockResultList;
	
	private final UUID uuid;
	
	private final Integer count;
	
	private final Integer preferredPageSize;
	
	public MockIBundleProvider(List<U> mockResultList, Integer preferredPageSize, Integer count) {
		this.count = count;
		this.uuid = UUID.randomUUID();
		this.datePublished = new Date();
		this.mockResultList = mockResultList;
		this.preferredPageSize = preferredPageSize;
	}
	
	@Override
	public IPrimitiveType<Date> getPublished() {
		return new InstantType(datePublished);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public List<IBaseResource> getResources(int i, int i1) {
		return (List<IBaseResource>) this.mockResultList;
	}
	
	@Nullable
	@Override
	public String getUuid() {
		return String.valueOf(this.uuid);
	}
	
	@Override
	public Integer preferredPageSize() {
		return this.preferredPageSize;
	}
	
	@Nullable
	@Override
	public Integer size() {
		return this.count;
	}
}
