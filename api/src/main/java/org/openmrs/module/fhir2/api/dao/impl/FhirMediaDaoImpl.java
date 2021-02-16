/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMediaDaoImpl extends BaseFhirDao<Obs> implements FhirMediaDao, ComplexObsHandler {

    @Override
    protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
        theParams.getParameters().forEach(entry ->{
            switch (entry.getKey()) {
                case FhirConstants.MEDIA_STATUS:
                    entry.getValue().forEach(param ->);
                    break;
                case FhirConstants.MEDIA_TYPE:
                    break;
                case FhirConstants.MEDIA_SUBJECT:
                    break;
                case FhirConstants.MEDIA_ENCOUNTER_REFERENCE:
                    break;
                case FhirConstants.MEDIA_CREATED_DATE_TIME:
                    break;
                case FhirConstants.MEDIA_CONTENT_TYPE:
                    break;
                case FhirConstants.CONTENT_DATA:
                    break;
                case FhirConstants.CONTENT_TITLE:
                    break;
                case FhirConstants.CONTENT_DATE_OF_CREATION:
                    break;


            }
        });
    }

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        return null;
    }

    @Override
    public Obs getObs(Obs obs, String s) {
        return null;
    }

    @Override
    public boolean purgeComplexData(Obs obs) {
        return false;
    }

    @Override
    public String[] getSupportedViews() {
        return new String[0];
    }

    @Override
    public boolean supportsView(String s) {
        return false;
    }

    @Override
    public Obs createOrUpdate(@Nonnull Obs newEntry) {
        return super.createOrUpdate(newEntry);
    }

    @Override
    public Obs delete(@Nonnull String uuid) {
        return super.delete(uuid);
    }

    @Override
    public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
        return super.getSearchResultUuids(theParams);
    }
}
