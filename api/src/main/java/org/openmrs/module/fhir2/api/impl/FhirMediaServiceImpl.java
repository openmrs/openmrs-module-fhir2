package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.module.fhir2.api.FhirMediaService;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FhirMediaServiceImpl implements FhirMediaService {
    @Override
    public Observation get(@Nonnull String uuid) {
        return null;
    }

    @Override
    public List<Observation> get(@Nonnull Collection<String> uuids) {
        return null;
    }

    @Override
    public Observation create(@Nonnull Observation newResource) {
        return null;
    }

    @Override
    public Observation update(@Nonnull String uuid, @Nonnull Observation updatedResource) {
        return null;
    }

    @Override
    public Observation delete(@Nonnull String uuid) {
        return null;
    }

    @Override
    public IBundleProvider searchForLocations(TokenAndListParam status, TokenAndListParam type, ReferenceAndListParam subject, ReferenceAndListParam encounterReference, DateRangeParam createdDateTime, TokenAndListParam contentType, StringAndListParam contentDataType, StringAndListParam contentTitle, DateRangeParam contentCreated, DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include> revIncludes, SortSpec sort) {
        return null;
    }
}
