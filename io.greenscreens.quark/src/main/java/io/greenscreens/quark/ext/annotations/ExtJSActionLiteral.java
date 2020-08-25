/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.ext.annotations;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;

/**
 * Internally used annotation wrapper used by CDI to find targeted bean methods
 */
@SuppressWarnings("all")
public final class ExtJSActionLiteral extends AnnotationLiteral<ExtJSAction> implements ExtJSAction {

	private static final long serialVersionUID = 1L;

	private final String namespace;
	private final String action;

	public ExtJSActionLiteral(final String namespace, final String action) {
		super();
		this.action = action;
		this.namespace = namespace;
	}

	@Override
	@Nonbinding
	public String action() {
		return action;
	}

	@Override
	@Nonbinding
	public String namespace() {
		return namespace;
	}

}
