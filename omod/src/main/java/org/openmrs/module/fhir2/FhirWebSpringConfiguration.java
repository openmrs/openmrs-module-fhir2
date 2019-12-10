package org.openmrs.module.fhir2;

import javax.inject.Inject;

import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirWebSpringConfiguration {
	
	@Bean
	@Inject
	public LoggingInterceptor configureLoggingInterceptor(LoggingInterceptor interceptor) {
		interceptor.setLoggerName(this.getClass().getPackage().getName() + ".accessLog");
		return interceptor;
	}
}
