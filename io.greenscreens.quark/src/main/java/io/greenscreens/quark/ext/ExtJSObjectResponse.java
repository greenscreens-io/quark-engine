/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.ext;


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

	
	public static class Builder<T> {
		
		private T data;
		
		private boolean success;
		private String msg;
		private String code;
		private Throwable exception;
		private Type type = Type.INFO;

        public Builder() {}
        
        public Builder<T> setStatus(final boolean status) {
        	this.success = status;
        	return this;
        }

        public Builder<T> setMessage(final String message) {
        	this.msg = message;
        	return this;
        }
        
        public Builder<T> setCode(final String code) {
        	this.code = code;
        	return this;
        }
        
		public Builder<T> setData(T data) {
			this.data = data;
			return this;
		}
        
        public ExtJSObjectResponse<T> build() {
        	final ExtJSObjectResponse<T> resp = new ExtJSObjectResponse<>(success, msg);
        	resp.setCode(code);
        	resp.setType(type);
        	resp.setData(data);
        	if (exception != null) {
        		resp.setError(exception, msg);
        	}
        	return resp;
        }
        
        public static  <K> Builder<K> create(final Class<K> type) {
        	return new Builder<>();
        }

	}

}
