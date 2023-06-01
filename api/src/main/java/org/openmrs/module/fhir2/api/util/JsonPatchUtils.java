package org.openmrs.module.fhir2.api.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import java.io.IOException;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class JsonPatchUtils {
	
	public static <T extends IBaseResource> T apply(FhirContext theCtx, T theResourceToUpdate, String thePatchBody) {
		// Parse the patch
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, false);
		
		JsonFactory factory = mapper.getFactory();
		
		final JsonPatch patch;
		
		try {
			JsonNode jsonPatchNode = mapper.readTree(factory.createParser(thePatchBody));
			JsonNode originalJsonDocument = mapper
					.readTree(theCtx.newJsonParser().encodeResourceToString(theResourceToUpdate));
			
			patch = JsonPatch.fromJson(jsonPatchNode);

			JsonNode after = patch.apply(originalJsonDocument);
			
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) theResourceToUpdate.getClass();
			
			String postPatchedContent = mapper.writeValueAsString(after);
			
			IParser fhirJsonParser = theCtx.newJsonParser();
			fhirJsonParser.setParserErrorHandler(new StrictErrorHandler());
			
			T retVal;
			try {
				retVal = fhirJsonParser.parseResource(clazz, postPatchedContent);
			} catch (DataFormatException e) {
				String resourceId = theResourceToUpdate.getIdElement().toUnqualifiedVersionless().getValue();
				String resourceType = theCtx.getResourceDefinition(theResourceToUpdate).getName();
				resourceId = defaultString(resourceId, resourceType);
				String msg = theCtx.getLocalizer().getMessage(JsonMergePatchUtils.class, "failedToApplyPatch", resourceId,
						e.getMessage());
				throw new InvalidRequestException(msg);
			}
			return retVal;
			
		} catch (IOException | JsonPatchException theE) {
			throw new InvalidRequestException(theE);
		}
		
	}
	
}
