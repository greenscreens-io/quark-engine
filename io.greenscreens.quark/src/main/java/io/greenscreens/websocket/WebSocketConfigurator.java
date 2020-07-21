/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import io.greenscreens.Util;
import io.greenscreens.web.TnConstants;
import io.greenscreens.web.listener.SessionCollector;

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

	@Override
	public final String getNegotiatedSubprotocol(final List<String> supported, final List<String> requested) {
		return TnConstants.WEBSOCKET_SUBPROTOCOL;
	}

	private String findChallenge(final HandshakeRequest request) {
		final List<String> list = request.getParameterMap().get("q");
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	private int findSessionToken(final HandshakeRequest request) {

		int token = 0;
		String val = null;

		final List<String> cookiesStr = request.getHeaders().get("cookie");
		Scanner scan = null;
		String[] pair = null;

		if (cookiesStr == null) {
			return -1;
		}

		for (String cookieStr : cookiesStr) {

			if (val != null) {
				break;
			}

			try {

				scan = new Scanner(cookieStr);
				scan.useDelimiter(";");

				while (scan.hasNext()) {

					pair = scan.next().split("=");

					if ("X-Authorization".equals(pair[0].trim())) {
						val = pair[1];
						break;
					}
				}

			} finally {
				Util.close(scan);
			}

		}

		if (val == null) {
			final List<String> list = request.getParameterMap().get("t");
			if (list != null && list.size() > 0) {
				val = list.get(0);
			}
		}

		if (val != null) {
			try {
				token = Integer.parseInt(val);
			} catch (Exception e) {

			}
		}

		return token;
	}

	/**
	 * modifyHandshake() is called before getEndpointInstance()!
	 */
	@Override
	public final void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request,
			final HandshakeResponse response) {

		super.modifyHandshake(sec, request, response);

		response.getHeaders().put("Accept-Language", LANG);

		HttpSession httpSession = (HttpSession) request.getHttpSession();
		final Map<String, Object> map = sec.getUserProperties();

		if (httpSession == null) {
			int token = findSessionToken(request);
			httpSession = SessionCollector.get(token);
		}

		if (httpSession != null) {
			map.put(HttpSession.class.getCanonicalName(), httpSession);
		}

		map.put(Locale.class.getCanonicalName(), getLocale(request));
		map.put(TnConstants.WEBSOCKET_PATH, sec.getPath());
		
		final String challenge = findChallenge(request);
		if (challenge != null) {
			map.put(TnConstants.WEBSOCKET_CHALLENGE, challenge);
		}
	}

	/**
	 * Store current browser locale
	 * 
	 * @param request
	 * @return
	 */
	private Locale getLocale(final HandshakeRequest request) {
		// Accept-Language:hr,en-US;q=0.8,en;q=0.6
		final Map<String, List<String>> map = request.getHeaders();
		final List<String> params = map.get("Accept-Language");

		Locale locale = Locale.ENGLISH;

		if (params != null && !params.isEmpty()) {
			String data = params.get(0);
			data = data.split(";")[0];
			data = data.split(",")[0];
			locale = new Locale(data);
		}

		return locale;
	}

}
