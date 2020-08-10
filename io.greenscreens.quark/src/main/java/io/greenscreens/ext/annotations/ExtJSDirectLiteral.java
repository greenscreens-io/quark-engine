/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.ext.annotations;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Internally used annotation wrapper used by CDI to find targeted beans
 */
@SuppressWarnings("all")
public class ExtJSDirectLiteral extends AnnotationLiteral<ExtJSDirect> implements ExtJSDirect {

	private static final long serialVersionUID = 1L;

	String[] paths = {};

	public ExtJSDirectLiteral(String[] paths) {
		super();
		this.paths = paths;
	}

	public String[] paths() {
		return paths;
	}

}
