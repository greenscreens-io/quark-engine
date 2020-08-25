/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

/**
 * Error list with codes and descriptions
 */
public enum QuarkErrors {

	E0000("E0000", "Invalid encryption data"), 
	E0001("E0001", "Requested Service not found"),
	E0002("E0002", "Incomming parameters are invalid"),

	E9999("E9999", "General error");

	private String code;

	private String message;

	private QuarkErrors(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getString() {
		return String.format("%s : %s", code, message);
	}

}
