/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.JsonDecoder;
import io.greenscreens.websocket.data.WebSocketRequest;

/**
 * Internal JSON decoder for WebSocket ExtJS request
 *
 */
public class WebsocketDecoder implements Decoder.Text<WebSocketRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketDecoder.class);

	@Override
	public final WebSocketRequest decode(final String message) throws DecodeException {

		WebSocketRequest wsMessage = null;
		LOG.trace("WebSocket request {}", message);

		try {
			final JsonDecoder<WebSocketRequest> jd = new JsonDecoder<>(WebSocketRequest.class, message);
			wsMessage = jd.getObject();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new DecodeException(message, e.getMessage(), e);
		}

		return wsMessage;
	}

	@Override
	public final boolean willDecode(final String message) {

		boolean decode = false;

		if (message != null) {
			decode = message.trim().startsWith("{") && message.trim().endsWith("}");
		}

		return decode;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(final EndpointConfig arg0) {

	}

}
