package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaContentTranslator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(MockitoJUnitRunner.class)
public class MediaContentTranslatorImplTest {

    @Mock
    private MediaContentTranslator mediaContentTranslator;

    @Before
    public void setUp(){
        mediaContentTranslator = new MediaContentTranslatorImpl();
    }

    @Test
    public void shouldTranslateObsToMediaContent(){
        Obs obs = new Obs();

        Media mediaContent = mediaContentTranslator.toFhirResource(obs);

        assertThat(mediaContent, notNullValue());
    }
}
