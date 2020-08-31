/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import io.greenscreens.quark.Util;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.listener.SessionCollector;

/**
 * Config object for @ServerEndpoint annotation used to intercept WebSocket
 * initialization for custom setup.
 *
 */
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

	private static final List<String> LANG;

	static {
		LANG = new ArrayList<>();
		LANG.add("UTF-8");
	}

	/**
	 * Find query parameter q, which contains request challenge
	 * 
	 * @param request
	 * @return
	 */
	String findChallenge(final HandshakeRequest request) {
		return WebsocketUtil.findQuery(request, "q");
	}

	/**
	 * Find session link token based on custom pairing
	 * 
	 * @param request
	 * @return
	 */
	int findSessionToken(final HandshakeRequest request) {

		final List<String> cookies = request.getHeaders().get("cookie");
		final Map<String, String> map = WebsocketUtil.parseCookies(cookies);

		String val = map.get("X-Authorization");
		if (val == null) {
			val = WebsocketUtil.findQuery(request, "t");
		}

		return Util.toInt(val);
	}

	/**
	 * Find http session attached to websocket
	 * 
	 * @param request
	 * @return
	 */
	public HttpSession findSession(final HandshakeRequest request) {

		HttpSession httpSession = (HttpSession) request.getHttpSession();

		if (httpSession == null) {
			final int token = findSessionToken(request);
			httpSession = SessionCollector.get(token);
		}

		return httpSession;
	}

	/**
	 * Store data to websocket user data
	 * 
	 * @param sec
	 * @param key
	 * @param value
	 */
	public void store(final ServerEndpointConfig sec, final String key, final Object value) {
		if (key != null && value != null) {
			sec.getUserProperties().put(key, value);
		}
	}

	@Override
	public final String getNegotiatedSubprotocol(final List<String> supported, final List<String> requested) {
		return QuarkConstants.WEBSOCKET_SUBPROTOCOL;
	}

	/**
	 * modifyHandshake() is called before getEndpointInstance()!
	 */
	@Override
	public void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request,
			final HandshakeResponse response) {

		super.modifyHandshake(sec, request, response);

		response.getHeaders().put("Accept-Language", LANG);

		final HttpSession httpSession = findSession(request);
		final String challenge = findChallenge(request);
		final Locale locale = WebsocketUtil.getLocale(request);

		store(sec, QuarkConstants.WEBSOCKET_PATH, sec.getPath());
		store(sec, QuarkConstants.WEBSOCKET_CHALLENGE, challenge);
		store(sec, Locale.class.getCanonicalName(), locale);
		store(sec, HttpSession.class.getCanonicalName(), httpSession);
	}

}
