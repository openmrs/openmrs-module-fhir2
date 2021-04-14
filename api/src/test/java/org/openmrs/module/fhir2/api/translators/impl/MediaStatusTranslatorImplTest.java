package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class MediaStatusTranslatorImplTest {

    private MediaStatusTranslatorImpl mediaStatusTranslatorImpl;

    @Before
    public void setUp(){ mediaStatusTranslatorImpl = new MediaStatusTranslatorImpl();}

    @Test
    public void shouldMapMediaStatusToObsStatus(){
        Obs obs = new Obs();

        Media.MediaStatus status = mediaStatusTranslatorImpl.toFhirResource(obs);

        assertThat(status, is(Media.MediaStatus.NULL));
    }


}
