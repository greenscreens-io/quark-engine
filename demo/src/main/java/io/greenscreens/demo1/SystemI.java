/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo1;

import java.io.IOException;

import javax.enterprise.inject.Vetoed;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;

/**
 * Custom AS400 class to support validated flag
 */
@Vetoed
public class SystemI extends AS400 {

	private static final long serialVersionUID = 1L;

	private boolean valid;

	public SystemI() {
		super();
	}

	public SystemI(String arg0, String arg1, String arg2) {
		super(arg0, arg1, arg2);
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean authenticate(String arg0, String arg1) throws AS400SecurityException, IOException {
		valid = super.authenticate(arg0, arg1);
		return valid;
	}

	@Override
	public void disconnectAllServices() {
		valid = false;
		super.disconnectAllServices();
	}

}
