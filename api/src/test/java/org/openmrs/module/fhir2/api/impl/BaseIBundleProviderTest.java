/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;

public abstract class BaseIBundleProviderTest<T extends IBaseResource> {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	protected List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	IBundleProvider getQueryResults(T resource) {
		
		return new IBundleProvider() {
			
			@Override
			public IPrimitiveType<Date> getPublished() {
				return InstantType.withCurrentTime();
			}
			
			@Nonnull
			@Override
			public List<IBaseResource> getResources(int i, int i1) {
				return Collections.singletonList(resource);
			};
			
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
