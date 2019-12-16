package org.openmrs.module.fhir2.util;

import java.util.Collection;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class FhirUtils {
	
	public static <T extends Resource> Bundle convertSearchResultsToBundle(Collection<T> resources) {
		Bundle bundle = FhirUtils.convertIterableToBundle(resources);
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setTotal(resources.size());
		return bundle;
	}
	
	public static <T extends Resource> Bundle convertIterableToBundle(Iterable<T> resources) {
		Bundle bundle = new Bundle();
		for (T resource : resources) {
			Bundle.BundleEntryComponent entry = bundle.addEntry();
			entry.setResource(resource);
		}
		
		return bundle;
	}
	
}
