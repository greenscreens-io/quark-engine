/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.websocket;

import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.JsonDecoder;
import io.greenscreens.Util;
import io.greenscreens.security.IAesKey;
import io.greenscreens.security.Security;
import io.greenscreens.websocket.data.IWebSocketResponse;
import io.greenscreens.websocket.data.WebSocketInstruction;

/**
 * Internal encoder for WebSocket ExtJS response
 */
enum WebsocketUtil {
	;

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketUtil.class);


	static public final String encode(final IWebSocketResponse data) throws EncodeException {

		String response = null;

		try {

			final ObjectMapper mapper = JsonDecoder.getJSONEngine();

			if (mapper != null) {
				final IAesKey key = data.getKey();
				data.setKey(null);

				response = mapper.writeValueAsString(data);
				response = encrypt(response, key);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new EncodeException(data, e.getMessage(), e);
		}

		if (response == null) {
			response = "";
		}

		return response;
	}

	static private final String encrypt(final String data, final IAesKey crypt) throws Exception {

		if (crypt == null) {
			return data;
		}

		final byte[] iv = Security.getRandom(crypt.getCipher().getBlockSize());
		final String enc = crypt.encrypt(data, iv);
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("iv", Util.bytesToHex(iv));
		node.put("d", enc);
		node.put("cmd", WebSocketInstruction.ENC.toString());
		final String json = JsonDecoder.getJSONEngine().writeValueAsString(node);
		return json;
	}

}
