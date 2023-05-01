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

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.hl7.fhir.instance.model.api.IBaseResource;

;

public class JsonPatchUtils {
	
	public static <T extends IBaseResource> T apply(FhirContext theCtx, T theResourceToUpdate, String thePatchBody) {
		// Parse the patch
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, false);
		
		JsonFactory factory = mapper.getFactory();
		
		final JsonMergePatch patch;
		
		try {
			JsonNode jsonPatchNode = mapper.readTree(factory.createParser(thePatchBody));
			JsonNode originalJsonDocument = mapper
			        .readTree(theCtx.newJsonParser().encodeResourceToString(theResourceToUpdate));
			
			// https://github.com/java-json-tools/json-patch
			patch = JsonMergePatch.fromJson(jsonPatchNode);
			JsonNode after = patch.apply(originalJsonDocument);
			
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) theResourceToUpdate.getClass();
			
			String postPatchedContent = mapper.writeValueAsString(after);
			
			IParser fhirJsonParser = theCtx.newJsonParser();
			fhirJsonParser.setParserErrorHandler(new StrictErrorHandler());
			
			T retVal;
			try {
				retVal = fhirJsonParser.parseResource(clazz, postPatchedContent);
			}
			catch (DataFormatException e) {
				String resourceId = theResourceToUpdate.getIdElement().toUnqualifiedVersionless().getValue();
				String resourceType = theCtx.getResourceDefinition(theResourceToUpdate).getName();
				resourceId = defaultString(resourceId, resourceType);
				String msg = theCtx.getLocalizer().getMessage(JsonPatchUtils.class, "failedToApplyPatch", resourceId,
				    e.getMessage());
				throw new InvalidRequestException(msg);
			}
			return retVal;
			
		}
		catch (IOException | JsonPatchException theE) {
			throw new InvalidRequestException(theE);
		}
		
	}
	
}
