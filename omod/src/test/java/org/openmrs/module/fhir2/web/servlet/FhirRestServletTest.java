/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.util.OpenmrsClassLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FhirRestServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private ServletConfig mockServletConfig;
    
    @Mock
    private PrintWriter mockWriter;

    private TestableFhirRestServlet servlet;

    @Before
    public void setUp() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);
        
        servlet = new TestableFhirRestServlet();
        
        when(mockServletConfig.getServletContext()).thenReturn(mock(javax.servlet.ServletContext.class));
        when(mockResponse.getWriter()).thenReturn(mockWriter);
        
        servlet.init(mockServletConfig);
    }

    @Test
    public void testServiceSetsContextClassLoader() throws ServletException, IOException {
    	// setup
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getRequestURI()).thenReturn("/fhir");
        
        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        
        assertNotEquals("ContextClassLoader should not be OpenmrsClassLoader initially",
                OpenmrsClassLoader.getInstance().getClass().getName(), initialClassLoader.getClass().getName());

        // replay
        servlet.service(mockRequest, mockResponse);

        // Verify
        ClassLoader newClassLoader = Thread.currentThread().getContextClassLoader();
        
        assertEquals("ContextClassLoader should be OpenmrsClassLoader after service method",
                OpenmrsClassLoader.getInstance().getClass().getName(), newClassLoader.getClass().getName());
    }
    
    class TestableFhirRestServlet extends FhirRestServlet {
        @Override
        public void initialize() {
        }
    }
}