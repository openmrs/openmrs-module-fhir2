package org.openmrs.module.fhir2.api;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;

import javax.annotation.Nonnull;
import java.util.HashSet;

public interface FhirMediaService extends FhirService<Observation>{
    Observation get(@Nonnull String uuid);

    IBundleProvider searchForLocations(TokenAndListParam status, TokenAndListParam type, ReferenceAndListParam subject,
                                       ReferenceAndListParam encounterReference, DateRangeParam createdDateTime, TokenAndListParam contentType, StringAndListParam contentDataType,
                                       StringAndListParam contentTitle, DateRangeParam contentCreated, DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include> revIncludes,
                                       SortSpec sort);
}
