/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static lombok.AccessLevel.PACKAGE;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Bundle30_40;
import org.hl7.fhir.convertors.conv30_40.DiagnosticReport30_40;
import org.hl7.fhir.convertors.conv30_40.Observation30_40;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.Bundle;
import org.openmrs.module.fhir2.api.FhirBatchService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component("batchFhirResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class BatchFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	FhirBatchService batchService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {return Bundle.class;}

	@Read
	@SuppressWarnings("unused")
	public Bundle getBatchById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Bundle batch = batchService.get(id.getIdPart());
		if (batch == null) {
			throw new ResourceNotFoundException("Could not find observation with Id " + id.getIdPart());
		}

		return Bundle30_40.convertBundle(batch);
	}

	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createBatch(@ResourceParam Bundle bundle) {
		return FhirProviderUtils.buildCreate(Bundle30_40.convertBundle(batchService.create(Bundle30_40.convertBundle(bundle))));
	}

	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateBatch(@IdParam IdType id, @ResourceParam Bundle bundle) {
		String idPart = null;

		if (id != null) {
			idPart = id.getIdPart();
		}

		return FhirProviderUtils.buildUpdate(Bundle30_40.convertBundle(batchService.update(idPart, Bundle30_40.convertBundle(bundle))));
	}

	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteBatch(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Bundle batch = batchService.delete(id.getIdPart());
		if (batch == null) {
			throw new ResourceNotFoundException("Could not find medication to delete with id " + id.getIdPart());
		}

		return FhirProviderUtils.buildDelete(Bundle30_40.convertBundle(batch));
	}

	@Search
	public IBundleProvider searchForBatch(
			@OptionalParam(name = Bundle.SP_IDENTIFIER) TokenAndListParam identifier,
			@OptionalParam(name = Bundle.SP_TYPE) TokenAndListParam batchType) {
		return new SearchQueryBundleProviderR3Wrapper(batchService.searchBatches(identifier, batchType));
	}
}
