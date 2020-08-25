/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web.data;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;

/**
 * Object to be converted into JSON structure. {type :'ws' , sid : session_id ,
 * tid : transaction_id, timeout : 0 , ....}
 */
public class WebResponse extends ExtJSDirectResponse<JsonNode> {

	public WebResponse() {
		super(null, null);
	}

	public WebResponse(ExtJSDirectRequest<JsonNode> request, Object response) {
		super(request, response);
	}

}
