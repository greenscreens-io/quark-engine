/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import com.ibm.as400.access.AS400;

import io.greenscreens.jt400.JT400ExtFactory;
import io.greenscreens.jt400.interfaces.IJT400Program;

/**
 * Represents QDCRDEVD program
 */
public interface IQDCRDEVD extends IJT400Program<QDCRDEVD> {

	public static IQDCRDEVD create(final AS400 as400) {
		return JT400ExtFactory.create(as400, IQDCRDEVD.class);
	}

}
