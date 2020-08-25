/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.quark;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class QuarkEngine {

	public static BeanManager getBeanManager() {

		final CDI<Object> cdi = CDI.current();

		if (cdi != null) {
			return cdi.getBeanManager();
		}

		return null;
	}

	public static <T> T of(final Class<T> clazz, Annotation...annotations) {
		return CDI.current().select(clazz, annotations).get();
	}

}
