/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.ext.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Name method parameters for validation.
 * If validation fails, response error will contain parameter name 
 */
@Retention(RUNTIME)
@Target({ PARAMETER, TYPE_PARAMETER })
public @interface ExtName {

	String value();
}
