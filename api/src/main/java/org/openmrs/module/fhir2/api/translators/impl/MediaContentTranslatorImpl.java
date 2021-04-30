/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaContentTranslator;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@Setter(AccessLevel.PACKAGE)
public class MediaContentTranslatorImpl extends BaseReferenceHandlingTranslator implements MediaContentTranslator {

	@Override
	public Media toFhirResource(@Nonnull Obs data) {
		if(data == null){
           return null;
		}

		Media mediaContent = new Media();
		mediaContent.setContent(new Attachment().setContentType(data.getValueText()));
		mediaContent.setContent(new Attachment().setDataElement(new Base64BinaryType()
				.setValue(data.getComplexData().getData().toString().getBytes())));
		mediaContent.setContent(new Attachment().setTitle(data.getComment()));
		mediaContent.setContent(new Attachment().setCreation(data.getDateCreated()));
		return mediaContent;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource) {
		existingObject.setValueText(resource.getContent().getContentType());
		existingObject.setValueComplex(resource.getContent().getDataElement().getValueAsString());
		existingObject.setComment(resource.getContent().getTitle());
		existingObject.setDateCreated(resource.getCreatedDateTimeType().getValue());
		return existingObject;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Media resource) {
		return toOpenmrsType(new Obs(), resource);
	}
}
