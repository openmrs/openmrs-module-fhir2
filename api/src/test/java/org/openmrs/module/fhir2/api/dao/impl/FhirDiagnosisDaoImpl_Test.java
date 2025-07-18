package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirDiagnosisDaoImpl_Test extends BaseFhirContextSensitiveTest {

    private static final String DIAGNOSIS_UUID = "11111111-2222-3333-4444-555555555555";

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    @Autowired
    private PatientService patientService;

    private FhirDiagnosisDaoImpl dao;

    @Before
    public void setUp() {
        dao = new FhirDiagnosisDaoImpl();
        dao.setSessionFactory(sessionFactory);
    }

    @Test
    public void createOrUpdate_shouldSaveDiagnosis() {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setUuid(DIAGNOSIS_UUID);
        diagnosis.setPatient(patientService.getPatient(2));

        dao.createOrUpdate(diagnosis);

        Diagnosis result = dao.get(DIAGNOSIS_UUID);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), equalTo(DIAGNOSIS_UUID));
    }
}
