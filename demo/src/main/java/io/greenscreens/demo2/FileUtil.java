/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo2;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public enum FileUtil {
;

	/**
	 * Helper method to copy stream data
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	static public void copyStream(final InputStream is, final OutputStream os) throws Exception {

		final byte[] buffer = new byte[1024];

		while(is.read(buffer) > -1) {
			os.write(buffer);
		}

		os.flush();
	}

	/**
	 * Helper method to close resource
	 * @param obj
	 */
	static public void close(final Closeable obj) {

		if (obj == null) return;

		try {
			obj.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
