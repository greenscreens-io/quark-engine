/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.websocket.EncodeException;
import javax.websocket.server.HandshakeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.Util;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;

/**
 * Internal encoder for WebSocket ExtJS response
 */
public enum WebsocketUtil {
	;

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketUtil.class);

	/**
	 * Encrypt message for websocket response
	 * 
	 * @param data
	 * @return
	 * @throws EncodeException
	 */
	static final String encode(final IWebSocketResponse data) throws EncodeException {

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

	/**
	 * Encrypt data with AES for encrypted response
	 * 
	 * @param data
	 * @param crypt
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * Parse browser received cookie strings
	 * 
	 * @param cookies
	 * @return
	 */
	static public Map<String, String> parseCookies(final List<String> cookies) {


		if (cookies == null) {
			return Collections.emptyMap();
		}

		final Map<String, String> map = new HashMap<>();
		Scanner scan = null;
		String[] pair = null;

		for (String cookie : cookies) {

			try {

				scan = new Scanner(cookie);
				scan.useDelimiter(";");

				while (scan.hasNext()) {
					pair = scan.next().split("=");
					if (pair.length > 1) {
						map.put(pair[0], pair[1]);
					}
				}

			} finally {
				Util.close(scan);
			}

		}

		return Collections.unmodifiableMap(map);
	}

	/**
	 * Get request header from websocket
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	static public String findHeader(final HandshakeRequest request, final String key) {

		final Map<String, List<String>> map = request.getHeaders();
		final List<String> params = map.get(key);

		if (params != null && !params.isEmpty()) {
			return params.get(0);
		}

		return null;
	}

	/**
	 * Generic method to find URL query parameter
	 * @param request
	 * @param name
	 * @return
	 */
	 static public String findQuery(final HandshakeRequest request, final String name) {
		final List<String> list = request.getParameterMap().get(name);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * Store current browser locale
	 * 
	 * Accept-Language:hr,en-US;q=0.8,en;q=0.6
	 * 
	 * @param request
	 * @return
	 */
	 static public Locale getLocale(final HandshakeRequest request) {

		String data = WebsocketUtil.findHeader(request, "Accept-Language");
		Locale locale = Locale.ENGLISH;

		if (data != null) {
			data = data.split(";")[0];
			data = data.split(",")[0];
			locale = new Locale(data);
		}

		return locale;
	}

}
