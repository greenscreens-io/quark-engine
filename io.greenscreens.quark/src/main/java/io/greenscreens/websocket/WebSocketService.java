/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.websocket.data.IWebSocketResponse;
import io.greenscreens.websocket.data.WebSocketRequest;

/**
 * Base WebSocket endpoint with ExtJS support. Should not be used directly.
 * Create new class extending this one and annotate new class
 * with @ServerEndpoint
 */
public class WebSocketService {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketService.class);

	@Inject
	private WebSocketEndpoint endpoint;

	// getopensessions does not work across different endpoints
	public void broadcast(final IWebSocketResponse data) {
		endpoint.broadcast(data);
	}

	@OnMessage
	public final void onMessage(final WebSocketRequest message, final Session session) {
		endpoint.onMessage(message, session);
	}

	@OnOpen
	public final void onOpen(final Session session, final EndpointConfig config) {

		endpoint = Optional.ofNullable(endpoint).orElse(getBean(WebSocketEndpoint.class));

		if (endpoint == null) {
			LOG.warn("WebSocketEndpoint not injected. If running in servlet only container, CDI framework is needed.");
			close(session);
			return;
		}

		endpoint.onOpen(session, config);
	}

	@OnClose
	public final void onClose(final Session session, final CloseReason reason) {
		endpoint.onClose(session, reason);
	}

	@OnError
	public final void onError(final Session session, final Throwable t) {
		endpoint.onError(session, t);
	}

	@OnMessage
	public final void onPongMessage(final PongMessage pong, final Session session) {
		/*
		 * final ByteBuffer bb = pong.getApplicationData(); System.out.println(bb);
		 */
	}

	private void close(Closeable closeable) {

		if (closeable == null)
			return;

		try {
			closeable.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public <T> T getBean(Class<T> cls) {

		final CDI<Object> cdi = CDI.current();
		if (cdi != null) {
			final Instance<T> inst = cdi.select(cls);
			if (inst != null) {
				return inst.get();
			}
		}

		return null;
	}

}
