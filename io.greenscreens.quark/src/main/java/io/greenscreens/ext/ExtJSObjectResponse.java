/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.ext;

/**
 * ExtJs standard response structure used by other extended response classes
 * 
 * { "success": false, "msg": "", "error": "", "stack": "" }
 */
public class ExtJSObjectResponse<T> extends ExtJSResponse {

	private static final long serialVersionUID = 1L;

	private T data;

	public ExtJSObjectResponse() {
		super();
	}

	public ExtJSObjectResponse(boolean success) {
		super(success);
	}

	public ExtJSObjectResponse(boolean success, String message) {
		super(success, message);
	}

	public ExtJSObjectResponse(Throwable exception, String message) {
		super(exception, message);
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
