/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import static ca.uhn.fhir.util.StringUtil.toUtf8String;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import com.github.dnault.xmlpatch.Patcher;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class XmlPatchUtils {
	
	/**
	 * Handles xml patch operations ("application/xml-patch+xml")
	 */
	public static <T extends IBaseResource> T applyXmlPatch(FhirContext theCtx, T theResourceToUpdate, String thePatchBody) {
		
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) theResourceToUpdate.getClass();
		
		String inputResource = theCtx.newXmlParser().encodeResourceToString(theResourceToUpdate);
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try {
			Patcher.patch(new ByteArrayInputStream(inputResource.getBytes(Constants.CHARSET_UTF8)),
			    new ByteArrayInputStream(thePatchBody.getBytes(Constants.CHARSET_UTF8)), result);
		}
		catch (IOException e) {
			throw new InternalErrorException(e);
		}
		
		String resultString = toUtf8String(result.toByteArray());
		T retVal = theCtx.newXmlParser().parseResource(clazz, resultString);
		
		return retVal;
	}
	
}
