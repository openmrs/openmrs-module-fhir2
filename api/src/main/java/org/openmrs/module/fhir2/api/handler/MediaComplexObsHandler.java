
/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.fhir2.api.handler;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PUBLIC)
@Order(Ordered.LOWEST_PRECEDENCE)
public class MediaComplexObsHandler implements ComplexObsHandler, CustomDatatypeHandler {

    public static final Log log = LogFactory.getLog(Obs.class);

    public static final String HANDLER_TYPE = "MediaHandler";

    public MediaComplexObsHandler(){
        super();
    }

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        PatientService patientService = Context.getPatientService();
        Patient patient = patientService.getPatient(Integer.parseInt(obs.getValueComplex()));
        if(patient == null){
            throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId() + "Desired Patient id :"
            +Integer.parseInt(obs.getValueComplex()) + "cannot be found");
        }

        obs.setValueComplex(obs.getComplexData().getTitle());
        obs.setComplexData(null);
        return obs;
    }

    @Override
    public Obs getObs(Obs obs, String s) {
        Patient patient = null;
        PatientService ps = Context.getPatientService();
        String contentType = obs.getComplexData().getTitle();
        String key = obs.getUuid();

        if(key != null && !StringUtils.isEmpty(key)){
           patient = ps.getPatient(Integer.parseInt(key));
        }

        if(patient != null){
            ComplexData complexData = new ComplexData(contentType, key);
            obs.setComplexData(complexData);
        }
        else {
            log.info("Warning : specified patient cannot be found - returning no ComplexData for " + obs.getObsId());
        }

        return obs;
    }

    @Override
    public boolean purgeComplexData(Obs obs) {
        String contentType = obs.getComplexData().getTitle();
        String key = obs.getUuid();

        if(key != null){
            ComplexData complexData = new ComplexData(contentType, key);
            obs.setComplexData(complexData);
            Context.getObsService().getHandler(obs).purgeComplexData(obs);
        }
        else{
            log.info("Sorry There is no such an observation with "+key);
        }
        return true;
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
    public void setHandlerConfiguration(String s) {

    }

    /**
     * Gets the handler type for each registered handler.
     *
     * @return the handler type
     */
    public String getHandlerType() {
        return MediaComplexObsHandler.HANDLER_TYPE;
    }
}
