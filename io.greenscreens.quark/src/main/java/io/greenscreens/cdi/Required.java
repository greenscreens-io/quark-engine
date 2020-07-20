/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.cdi;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ PARAMETER })
public @interface Required {

}
