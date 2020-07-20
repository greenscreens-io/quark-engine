/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.ext;

public class ExtEncrypt {

	private String d;
	private String k;
	private int v;

	public String getD() {
		return d;
	}

	public void setD(final String d) {
		this.d = d;
	}

	public String getK() {
		return k;
	}

	public void setK(final String k) {
		this.k = k;
	}

	public int getV() {
		return v;
	}

	public void setV(final int v) {
		this.v = v;
	}

	@Override
	public String toString() {
		return "ExtEncrypt [d=" + d + ", k=" + k + ", v=" + v + "]";
	}

}
