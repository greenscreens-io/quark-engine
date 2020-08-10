/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.ext;

import java.util.List;

/**
 * Class representing ExtJS Direct request. It is used for decoding received
 * JSON data from ExtJS into Java class instance
 * 
 * @param <T>
 */
public class ExtJSDirectRequest<T> {

	// {"action":"DemoForm","method":"submit","data":[{"id":"0","username":"asfsa","password":"asdfv","email":"sadfv","rank":"345"}],"type":"rpc","tid":1}

	private String action;
	private String method;
	private String namespace;
	private String type;
	private String tid;
	private List<T> data;

	public final String getNamespace() {
		return namespace;
	}

	public final void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	public final String getAction() {
		return action;
	}

	public final void setAction(final String action) {
		this.action = action;
	}

	public final String getMethod() {
		return method;
	}

	public final void setMethod(final String method) {
		this.method = method;
	}

	public final String getType() {
		return type;
	}

	public final void setType(final String type) {
		this.type = type;
	}

	public final String getTid() {
		return tid;
	}

	public final void setTid(final String tid) {
		this.tid = tid;
	}

	public final List<T> getData() {
		return data;
	}

	public final void setData(final List<T> data) {
		this.data = data;
	}

	public final T getDataByIndex(final int index) {

		T value = null;

		if (data != null && !data.isEmpty()) {
			value = data.get(index);
		}

		return value;
	}

	@Override
	public String toString() {
		return "ExtJSDirectRequest [action=" + action + ", method=" + method + ", namespace=" + namespace + ", type="
				+ type + ", tid=" + tid + ", data=" + data + "]";
	}

}
