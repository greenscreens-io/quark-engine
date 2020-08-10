/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.websocket;

import java.nio.ByteBuffer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.JsonDecoder;
import io.greenscreens.Util;
import io.greenscreens.websocket.data.WebSocketRequest;

/**
 * Internal JSON decoder for WebSocket ExtJS request
 *
 */
public class WebsocketDecoderBinary implements Decoder.Binary<WebSocketRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketDecoderBinary.class);

	
	@Override
	public boolean willDecode(final ByteBuffer buff) {
		return true;
	}
	
	
	@Override
	public WebSocketRequest decode(final ByteBuffer buffer) throws DecodeException {

		String message = null;
		WebSocketRequest wsMessage = null;

		try {
			message = Util.ungzip(buffer.array());
			final JsonDecoder<WebSocketRequest> jd = new JsonDecoder<>(WebSocketRequest.class, message);
			wsMessage = jd.getObject();
			wsMessage.setBinary(true);			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new DecodeException(message, e.getMessage(), e);
		}

		return wsMessage;
	}


	@Override
	public void destroy() {

	}

	@Override
	public void init(final EndpointConfig arg0) {

	}


}
