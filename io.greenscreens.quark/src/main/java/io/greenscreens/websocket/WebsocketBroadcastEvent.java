/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import io.greenscreens.websocket.data.WebSocketResponse;

/**
 * Class holding event data  
 */
public class WebsocketBroadcastEvent {

    private final WebSocketResponse data;

	public WebsocketBroadcastEvent(final WebSocketResponse data) {
		super();
		this.data = data;
	}

	public WebSocketResponse getData() {
		return data;
	}

}
