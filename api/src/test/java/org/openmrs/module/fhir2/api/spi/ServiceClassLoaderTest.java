package org.openmrs.module.fhir2.api.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Set;

import org.junit.Test;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.impl.FhirObservationServiceImpl;

public class ServiceClassLoaderTest {

	@Test
	public void shouldLoadDefinedService() {
		ServiceClassLoader<FhirService> fhirServiceServiceClassLoader = new ServiceClassLoader<>(FhirService.class);

		Set<Class<? extends FhirService>> services = fhirServiceServiceClassLoader.load();

		assertThat(services, hasSize(greaterThanOrEqualTo(1)));
		assertThat(services, hasItem(equalTo(FhirObservationServiceImpl.class)));
	}
}
