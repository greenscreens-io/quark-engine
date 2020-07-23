/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import io.greenscreens.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 *
 */
public class WebsocketEncoder implements Encoder.Text<WebSocketResponse> {

	@Override
	public final void destroy() {

	}

	@Override
	public final void init(final EndpointConfig arg0) {

	}

	@Override
	public final String encode(final WebSocketResponse data) throws EncodeException {
		return WebsocketUtil.encode(data);
	}

}
