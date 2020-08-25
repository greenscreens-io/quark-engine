/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.ext;

import java.util.Collection;

/**
 * ExtJS array response structure.
 */
public class ExtJSResponseList<T> extends ExtJSResponse {

	private static final long serialVersionUID = 1L;

	private Collection<T> data;
	private int total;
	private int page;

	public ExtJSResponseList() {
		super();
	}

	public ExtJSResponseList(final boolean success) {
		super(success);
	}

	public ExtJSResponseList(final boolean success, final String message) {
		super(success, message);
	}

	public ExtJSResponseList(final Throwable exception, final String message) {
		super(exception, message);
	}

	public final Collection<T> getData() {
		return data;
	}

	public final void setData(final Collection<T> data) {
		this.data = data;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public static class Builder<T> {
		
		private Collection<T> data;
		
		private boolean success;
		private String msg;
		private String code;
		private Throwable exception;
		private Type type = Type.INFO;

		private int total;
		private int page;
		
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
        
		public Builder<T> setData(Collection<T> data) {
			this.data = data;
			return this;
		}
        
        public Builder<T> setTotal(final int total) {
        	this.total = total;
        	return this;
        }
        
        public Builder<T> setPage(final int page) {
        	this.page = page;
        	return this;
        }        
        
        public ExtJSResponseList<T> build() {
        	final ExtJSResponseList<T> resp = new ExtJSResponseList<>(success, msg);
        	resp.setCode(code);
        	resp.setType(type);
        	resp.setData(data);
        	resp.setTotal(total);
        	resp.setPage(page);
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
