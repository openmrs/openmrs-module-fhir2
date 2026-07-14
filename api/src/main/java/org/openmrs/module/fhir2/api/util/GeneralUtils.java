/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;

@Slf4j
public final class GeneralUtils {
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	
	public static String inputStreamToString(final InputStream is, final Charset charset) throws IOException {
		// this may over-allocate, but we're only holding it in memory temporarily
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int length;
		while ((length = is.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		return outputStream.toString(charset.name());
	}
	
	public static String resourceToString(final String resource, final Charset charset, final ClassLoader cl)
	        throws IOException {
		return inputStreamToString(Objects.requireNonNull(cl.getResourceAsStream(resource)), charset);
	}
	
	public static boolean isVoidedOrRetired(OpenmrsObject object) {
		if (object instanceof Retireable) {
			return ((Retireable) object).getRetired();
		} else if (object instanceof Voidable) {
			return ((Voidable) object).getVoided();
		}
		
		return false;
	}
	
	/**
	 * Replaces the contents of a persistent collection in place, preserving the underlying instance
	 * (required by Hibernate 6, which manages the collection reference) while tolerating immutable and
	 * {@code null} inputs. Guards against the self-assignment case (source == target), where a naive
	 * {@code clear()} followed by {@code addAll()} would silently wipe the collection.
	 *
	 * @param target the collection to update (must not be {@code null})
	 * @param source the desired contents, or {@code null} to clear the target
	 */
	public static <T> void replaceContents(Collection<T> target, Collection<? extends T> source) {
		if (target == source) {
			return;
		}
		
		target.clear();
		if (source != null) {
			target.addAll(source);
		}
	}
}
