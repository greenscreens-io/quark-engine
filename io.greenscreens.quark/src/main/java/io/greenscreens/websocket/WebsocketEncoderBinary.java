/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.Util;
import io.greenscreens.websocket.data.WebSocketResponseBinary;

/**
 * Internal encoder for WebSocket ExtJS response
 *
 */
public class WebsocketEncoderBinary implements Encoder.Binary<WebSocketResponseBinary> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEncoderBinary.class);

	@Override
	public final void destroy() {

	}

	@Override
	public final void init(final EndpointConfig arg0) {

	}

	@Override
	public final ByteBuffer encode(final WebSocketResponseBinary data) throws EncodeException {
		
		final String msg = WebsocketUtil.encode(data);
		ByteBuffer buff = null;
		
		if (msg != null && msg.length() > 0) {
			
			try {
				final byte bytes[] = Util.gzip(msg);
				buff = ByteBuffer.wrap(bytes);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				LOG.debug(e.getMessage(), e);
				throw new EncodeException(data, e.getMessage());
			}
		}
		
		return buff;
	}
	
}