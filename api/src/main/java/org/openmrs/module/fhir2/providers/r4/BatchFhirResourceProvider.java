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

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirBatchService;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.model.FhirBatch;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class BatchFhirResourceProvider implements IResourceProvider {

	@Autowired
	private FhirBatchService service;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Bundle.class;
	}

	@Read
	public Bundle getBatchById(@IdParam @Nonnull IdType id) {
		Bundle bundle = service.get(id.getIdPart());

		if (bundle == null) {
			throw new ResourceNotFoundException("Could not find Diagnostic Report with Id " + id.getIdPart());
		}

		return bundle;
	}

	@Create
	public MethodOutcome createBatch(@ResourceParam Bundle bundle) {
		return FhirProviderUtils.buildCreate(service.create(bundle));
	}

	@Update
	public MethodOutcome updateBatch(@IdParam IdType id, @ResourceParam Bundle bundle) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}

		return FhirProviderUtils.buildUpdate(service.update(id.getIdPart(), bundle));
	}

	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteBatch(@IdParam @Nonnull IdType id) {
		Bundle bundle = service.delete(id.getIdPart());
		if (bundle == null) {
			throw new ResourceNotFoundException("Could not find medication to delete with id " + id.getIdPart());
		}
		return FhirProviderUtils.buildDelete(bundle);
	}

	@Search
	public IBundleProvider searchForBatch(
			@OptionalParam(name = org.hl7.fhir.dstu3.model.Bundle.SP_IDENTIFIER) TokenAndListParam identifier,
			@OptionalParam(name = org.hl7.fhir.dstu3.model.Bundle.SP_TYPE) TokenAndListParam batchType) {

		return service.searchBatches(identifier, batchType);
	}
}
