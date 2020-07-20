/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.ext.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Used by API engine to detect entry point
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE })
public @interface ExtJSEndpoint {

}
