/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import io.greenscreens.quark.websocket.data.IWebSocketResponse;

/**
 * Class holding event data
 */
public class WebsocketBroadcastEvent {

	private final IWebSocketResponse data;

	public WebsocketBroadcastEvent(final IWebSocketResponse data) {
		super();
		this.data = data;
	}

	public IWebSocketResponse getData() {
		return data;
	}

}
