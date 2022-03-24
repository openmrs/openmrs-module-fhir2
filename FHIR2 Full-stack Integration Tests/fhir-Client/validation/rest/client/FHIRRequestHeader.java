/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.fhir2.validation.rest.client;
/**
 * This class represents an HTTP request header that will be used as part of 
 * a FHIR Client operation invocation.
 * 
 * @author sharif
 */
public class FHIRRequestHeader {
    private String name;
    private Object value;
    
    public FHIRRequestHeader() {
    }

    public FHIRRequestHeader(String name, Object value) {
        setName(name);
        setValue(value);
    }
    // getters and setters method
        /**
     * Returns the name of the request header.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the request header.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of the request header.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the request header.
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * This static method can be used as a shortcut for instantiating a new FHIRRequestHeader.
     * @param name the name of the request header 
     * @param value the value of the request header
     * @return a new FHIRRequestHeader instance
     */
    public static FHIRRequestHeader header(String name, Object value) {
        return new FHIRRequestHeader(name, value);
    }

    
}
