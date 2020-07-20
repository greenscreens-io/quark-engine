/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.web.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.greenscreens.ext.ExtJSDirectRequest;
import io.greenscreens.ext.ExtJSDirectResponse;

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
