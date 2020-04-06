/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search.param;

import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ca.uhn.fhir.model.api.IQueryParameterAnd;
import ca.uhn.fhir.model.api.IQueryParameterOr;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SearchParameterMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer count;
	
	private SortSpec sortSpec;
	
	private LinkedHashMap<String, List<PropParam<IQueryParameterAnd>>> andParams;
	
	private LinkedHashMap<String, List<PropParam<IQueryParameterOr>>> orParams;
	
	private LinkedHashMap<String, List<PropParam<ReferenceParam>>> referenceParams;
	
	/**
	 * Constructor
	 */
	public SearchParameterMap() {
		this.andParams = new LinkedHashMap<>();
		this.orParams = new LinkedHashMap<>();
		this.referenceParams = new LinkedHashMap<>();
	}
	
	/**
	 * Add {@link ca.uhn.fhir.model.api.IQueryParameterAnd} into linkedHashMap
	 *
	 * @param key keyValue
	 * @param andParam The {@link ca.uhn.fhir.model.api.IQueryParameterAnd} param to added into the
	 *            linkedHashMap
	 */
	public void addAndParam(@NotNull String key, @NotNull IQueryParameterAnd andParam) {
		List<PropParam<IQueryParameterAnd>> ands = this.andParams.getOrDefault(key, new ArrayList<>());
		ands.add(PropParam.<IQueryParameterAnd> builder().param(andParam).build());
		this.andParams.put(key, ands);
	}
	
	/**
	 * Adds {@link ca.uhn.fhir.model.api.IQueryParameterAnd} into {@link java.util.LinkedHashMap}
	 *
	 * @param key Key value
	 * @param propertyName Property name
	 * @param andParam The {@link ca.uhn.fhir.model.api.IQueryParameterAnd} element to be added
	 */
	public void addAndParam(@NotNull String key, @NotNull String propertyName, @NotNull IQueryParameterAnd andParam) {
		List<PropParam<IQueryParameterAnd>> ands = this.andParams.getOrDefault(key, new ArrayList<>());
		ands.add(PropParam.<IQueryParameterAnd> builder().param(andParam).propertyName(propertyName).build());
		this.andParams.put(key, ands);
	}
	
	/**
	 * Gets {@link ca.uhn.fhir.model.api.IQueryParameterAnd} from {@link java.util.LinkedHashMap}
	 *
	 * @param key key value
	 * @return A {@link java.util.List} of {@link ca.uhn.fhir.model.api.IQueryParameterAnd}
	 */
	public List<PropParam<IQueryParameterAnd>> getAndParams(@NotNull String key) {
		return this.andParams.getOrDefault(key, new ArrayList<>());
	}
	
	/**
	 * Add {@link ca.uhn.fhir.model.api.IQueryParameterOr} into linkedHashMap
	 *
	 * @param key keyValue
	 * @param orParam The {@link ca.uhn.fhir.model.api.IQueryParameterOr} param to added into the
	 *            linkedHashMap
	 */
	public void addOrParam(@NotNull String key, @NotNull IQueryParameterOr orParam) {
		List<PropParam<IQueryParameterOr>> ors = this.orParams.getOrDefault(key, new ArrayList<>());
		ors.add(PropParam.<IQueryParameterOr> builder().param(orParam).build());
		this.orParams.put(key, ors);
	}
	
	/**
	 * Adds {@link ca.uhn.fhir.model.api.IQueryParameterOr} param into a {@link java.util.LinkedHashMap}
	 *
	 * @param key Key value
	 * @param propertyName Property name
	 * @param orParam The {@link ca.uhn.fhir.model.api.IQueryParameterOr} param to be added
	 */
	public void addOrParam(@NotNull String key, @NotNull String propertyName, @NotNull IQueryParameterOr orParam) {
		List<PropParam<IQueryParameterOr>> ors = this.orParams.getOrDefault(key, new ArrayList<>());
		ors.add(PropParam.<IQueryParameterOr> builder().param(orParam).propertyName(propertyName).build());
		this.orParams.put(key, ors);
	}
	
	/**
	 * Gets {@link ca.uhn.fhir.model.api.IQueryParameterOr} from {@link java.util.LinkedHashMap}
	 *
	 * @param key key value
	 * @return A {@link java.util.List} of {@link ca.uhn.fhir.model.api.IQueryParameterOr}
	 */
	public List<PropParam<IQueryParameterOr>> getOrParams(@NotNull String key) {
		return this.orParams.getOrDefault(key, new ArrayList<>());
	}
	
	/**
	 * Add {@link ca.uhn.fhir.rest.param.ReferenceParam} into linkedHashMap
	 *
	 * @param key keyValue
	 * @param referenceParam The {@link ca.uhn.fhir.rest.param.ReferenceParam} param to added into the
	 *            linkedHashMap
	 */
	public void addReferenceParam(@NotNull String key, @NotNull ReferenceParam referenceParam) {
		List<PropParam<ReferenceParam>> referenceParams = this.referenceParams.getOrDefault(key, new ArrayList<>());
		referenceParams.add(PropParam.<ReferenceParam> builder().build());
		this.referenceParams.put(key, referenceParams);
	}
	
	/**
	 * Adds {@link ca.uhn.fhir.rest.param.ReferenceParam} param into a {@link java.util.LinkedHashMap}
	 *
	 * @param key Key value
	 * @param propertyName Property name
	 * @param referenceParam The {@link ca.uhn.fhir.rest.param.ReferenceParam} param to be added
	 */
	public void addReferenceParam(@NotNull String key, @NotNull String propertyName,
	        @NotNull ReferenceParam referenceParam) {
		List<PropParam<ReferenceParam>> referenceParams = this.referenceParams.getOrDefault(key, new ArrayList<>());
		referenceParams.add(PropParam.<ReferenceParam> builder().param(referenceParam).propertyName(propertyName).build());
		this.referenceParams.put(key, referenceParams);
	}
	
	/**
	 * Gets {@link ca.uhn.fhir.rest.param.ReferenceParam} from {@link java.util.LinkedHashMap}
	 *
	 * @param key key value
	 * @return A {@link java.util.List} of {@link ca.uhn.fhir.rest.param.ReferenceParam}
	 */
	public List<PropParam<ReferenceParam>> getReferenceParams(@NotNull String key) {
		return this.referenceParams.getOrDefault(key, new ArrayList<>());
	}
	
}
