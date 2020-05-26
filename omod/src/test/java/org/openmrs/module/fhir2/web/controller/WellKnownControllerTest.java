/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
///**
// * The contents of this file are subject to the OpenMRS Public License
// * Version 1.0 (the "License"); you may not use this file except in
// * compliance with the License. You may obtain a copy of the License at
// * http://license.openmrs.org
// *
// * Software distributed under the License is distributed on an "AS IS"
// * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// * License for the specific language governing rights and limitations
// * under the License.
// *
// * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
// */
//package org.openmrs.module.fhir2.web.controller;
//
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.HashMap;
//import java.util.List;
//
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ContextConfiguration;
//
//public class WellKnownControllerTest {
//
//	@Autowired
//	private WellKnownController controller;
//
//	@Test
//	public void shouldReturnOkStatus() {
//		assertTrue(controller.getConfigurationData("R4").getStatusCode() == HttpStatus.OK);
//	}
//
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldGetMetadata() {
//		ResponseEntity<HashMap<String, Object>> res = controller.getConfigurationData("R4");
//		HashMap<String, Object> body = res.getBody();
//
//		assertTrue(body.containsKey("authorization_endpoint"));
//	}
//}
