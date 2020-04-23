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
import java.util.Map;
import java.util.Set;

import ca.uhn.fhir.rest.api.SortSpec;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedCaseInsensitiveMap;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SearchParameterMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Getter
	private SortSpec sortSpec;
	
	private LinkedHashMap<String, List<PropParam<?>>> params = new LinkedCaseInsensitiveMap<>();
	
	public SearchParameterMap addParameter(@NotNull String key, @NotNull Serializable param) {
		return addParameter(key, null, param);
	}
	
	public SearchParameterMap addParameter(@NotNull String key, String propertyName, @NotNull Serializable param) {
		List<PropParam<?>> params = this.params.getOrDefault(key, new ArrayList<>());
		params.add(PropParam.builder().param(param).propertyName(propertyName).build());
		this.params.put(key, params);
		return this;
	}
	
	public Set<Map.Entry<String, List<PropParam<?>>>> getParameters() {
		return params.entrySet();
	}
	
	public List<PropParam<?>> getParameters(@NotNull String key) {
		return this.params.getOrDefault(key, new ArrayList<>());
	}
	
	public SearchParameterMap setSortSpec(SortSpec sortSpec) {
		this.sortSpec = sortSpec;
		return this;
	}
}
