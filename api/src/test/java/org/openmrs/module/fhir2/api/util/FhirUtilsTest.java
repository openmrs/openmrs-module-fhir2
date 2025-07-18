package org.openmrs.module.fhir2.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Test;

public class FhirUtilsTest {

    @Test
    public void getOpenmrsConditionType_shouldIdentifyDiagnosis() {
        Condition condition = new Condition();
        CodeableConcept category = new CodeableConcept();
        category.addCoding(new Coding("http://terminology.hl7.org/CodeSystem/condition-category", "encounter-diagnosis", null));
        condition.addCategory(category);

        Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);

        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.DIAGNOSIS));
    }

    @Test
    public void getOpenmrsConditionType_shouldDefaultToCondition() {
        Condition condition = new Condition();

        Optional<FhirUtils.OpenmrsConditionType> result = FhirUtils.getOpenmrsConditionType(condition);

        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get(), equalTo(FhirUtils.OpenmrsConditionType.CONDITION));
    }
}
