/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.ext;

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
        
}
