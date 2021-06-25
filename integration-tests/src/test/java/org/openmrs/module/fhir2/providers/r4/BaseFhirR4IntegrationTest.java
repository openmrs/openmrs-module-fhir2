/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.model.GroupMember;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class BaseFhirR4IntegrationTest<T extends IResourceProvider, U extends DomainResource> extends BaseFhirIntegrationTest<T, U> {
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	@Override
	public String getServletName() {
		return "fhir2Servlet";
	}
	
	@Override
	public FhirContext getFhirContext() {
		return FHIR_CONTEXT;
	}
	
	@Override
	public FhirRestServlet getRestfulServer() {
		return new FhirRestServlet();
	}
	
	@Before
	@Override
	public void setup() throws Exception {
		getFhirContext().registerCustomType(GroupMember.class);
		super.setup();
	}
	
	@Override
	public void describeOperationOutcome(Description mismatchDescription, IBaseOperationOutcome baseOperationOutcome) {
		if (baseOperationOutcome instanceof OperationOutcome) {
			OperationOutcome operationOutcome = (OperationOutcome) baseOperationOutcome;
			if (operationOutcome.hasIssue()) {
				mismatchDescription.appendText(" with message ");
				mismatchDescription.appendValue(
				    operationOutcome.getIssue().stream().map(OperationOutcome.OperationOutcomeIssueComponent::getDiagnostics)
				            .collect(Collectors.joining(". ")));
			}
		}
	}
	
	@Override
	public Class<? extends IBaseOperationOutcome> getOperationOutcomeClass() {
		return OperationOutcome.class;
	}
	
	@Override
	public U removeNarrativeAndContained(U item) {
		@SuppressWarnings("unchecked")
		U newItem = (U) item.copy();
		newItem.setText(null);
		newItem.setContained(null);
		return newItem;
	}
	
	@Override
	public Bundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (Bundle) super.readBundleResponse(response);
	}
	
	@Override
	public OperationOutcome readOperationOutcome(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (OperationOutcome) super.readOperationOutcome(response);
	}
	
	public static Matcher<Bundle.BundleEntryComponent> hasResource(Matcher<? extends IDomainResource> matcher) {
		return new HasResourceMatcher(matcher);
	}
	
	private static class HasResourceMatcher extends TypeSafeMatcher<Bundle.BundleEntryComponent> {
		
		private final Matcher<? extends IDomainResource> matcher;
		
		public HasResourceMatcher(Matcher<? extends IDomainResource> matcher) {
			this.matcher = matcher;
		}
		
		@Override
		protected boolean matchesSafely(Bundle.BundleEntryComponent item) {
			return matcher.matches(item.getResource());
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("a bundle component with a resource that ").appendDescriptionOf(matcher);
		}
		
		@Override
		protected void describeMismatchSafely(Bundle.BundleEntryComponent item, Description mismatchDescription) {
			matcher.describeMismatch(item.getResource(), mismatchDescription);
		}
	}
	
	protected static Matcher<List<Bundle.BundleEntryComponent>> isSortedAndWithinMax(Integer max) {
		return new IsSortedAndWithinMax(max);
	}
	
	private static class IsSortedAndWithinMax extends TypeSafeDiagnosingMatcher<List<Bundle.BundleEntryComponent>> {
		
		private int max;
		
		IsSortedAndWithinMax(int max) {
			this.max = max;
		}
		
		@Override
		protected boolean matchesSafely(List<Bundle.BundleEntryComponent> entries, Description mismatchDescription) {
			List<Observation> observations = entries.stream().map(Bundle.BundleEntryComponent::getResource)
			        .filter(it -> it instanceof Observation).map(it -> (Observation) it).collect(Collectors.toList());
			
			Set<String> codeList = new HashSet<>();
			
			for (int var = 0; var < observations.size(); var++) {
				String currentConcept = observations.get(var).getCode().getCodingFirstRep().getCode();
				
				//check if Bundle is returned grouped code wise
				// if an observation arrives whose concept wise list is already created, then that means this observation is
				//out of place
				//so bundle is not returned grouped wise
				if (codeList.contains(currentConcept)) {
					mismatchDescription.appendText("an Observation which was not grouped correctly")
					        .appendValue(observations.get(var).getId());
					return false;
				} else {
					
					codeList.add(currentConcept);
					
					String currentDateTimeType = observations.get(var).getEffectiveDateTimeType().toString();
					
					int distinctObsDateTime = 1;
					
					if (var == observations.size() - 1) {
						return true;
					}
					
					String nextConcept = observations.get(var + 1).getCode().getCodingFirstRep().getCode();
					String nextDateTimeType = observations.get(var + 1).getEffectiveDateTimeType().toString();
					
					while (nextConcept.equals(currentConcept)) {
						//if nextDatetime is greater than currentDateTime, then the list is not sorted in decreasing order, which was required
						if (currentDateTimeType.compareTo(nextDateTimeType) < 0) {
							mismatchDescription.appendText("an Observation which is not placed in sorted order")
							        .appendValue(observations.get(var + 1).getId());
							return false;
						}
						
						if (!currentDateTimeType.equals(nextDateTimeType)) {
							distinctObsDateTime++;
						}
						var++;
						
						if (var + 1 == observations.size()) {
							//if count of distinct obsDatetime is within max
							if (distinctObsDateTime <= max) {
								return true;
							}
							return false;
						}
						
						nextConcept = observations.get(var + 1).getCode().getCodingFirstRep().getCode();
						
						currentDateTimeType = nextDateTimeType;
						nextDateTimeType = observations.get(var + 1).getEffectiveDateTimeType().toString();
					}
					
					if (distinctObsDateTime > max) {
						mismatchDescription.appendText("a code group which contains more observations than specified in max")
						        .appendValue(currentConcept);
						return false;
					}
				}
			}
			
			return true;
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText(
			    "Result is grouped code wise, sorted from recent to older and distinct obsDatetime is within value of max");
		}
	}
}
