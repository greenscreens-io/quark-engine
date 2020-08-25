/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web.data;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;

/**
 * Class used to map JSON structure describing ExtJS websocket request.
 */
public class WebRequest extends ExtJSDirectRequest<JsonNode> {

	public WebRequest() {
		super();
	}

}
