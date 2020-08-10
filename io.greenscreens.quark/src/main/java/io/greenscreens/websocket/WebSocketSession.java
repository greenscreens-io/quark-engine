/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.websocket;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.security.IAesKey;
import io.greenscreens.web.TnConstants;
import io.greenscreens.websocket.data.IWebSocketResponse;
import io.greenscreens.websocket.data.WebSocketInstruction;
import io.greenscreens.websocket.data.WebSocketResponse;

/**
 * Class for holding WebSocket session data. Purpose of this class is similar to
 * HttpSession
 */
public class WebSocketSession implements Session {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketSession.class);

	private final Session session;

	// private ExtJSDirectRequest<?> request;

	public WebSocketSession(final Session session) {
		super();
		this.session = session;
	}

	public WebSocketSession(final Session session, final HttpSession httpSession) {

		this.session = session;

		if (httpSession != null) {
			session.getUserProperties().put(HttpSession.class.getCanonicalName(), httpSession);
		}
	}

	@Override
	public final void addMessageHandler(final MessageHandler arg0) throws IllegalStateException {
		session.addMessageHandler(arg0);
	}

	@Override
	public final void close() throws IOException {

		if (session.isOpen()) {

			final IWebSocketResponse response = new WebSocketResponse(WebSocketInstruction.BYE);

			try {
				session.getBasicRemote().sendObject(response);
			} catch (EncodeException e) {
				LOG.error(e.getMessage());
				LOG.trace(e.getMessage(), e);
			}
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, ""));
		}
	}

	@Override
	public final void close(final CloseReason arg0) throws IOException {
		if (session.isOpen()) {
			session.close(arg0);
		}
	}

	public final ServletContext getContext() {
		return getHttpSession().getServletContext();
	}

	public final boolean sendResponse(final IWebSocketResponse wsResponse, final boolean async) {

		if (wsResponse == null) {
			return false;
		}

		if (!session.isOpen()) {
			LOG.warn("Websocket response not sent, session is closed for {}!", this);
			try {
				close(new CloseReason(CloseCodes.CANNOT_ACCEPT, ""));
			} catch (IOException e) {
				LOG.warn(e.getMessage());
				LOG.debug(e.getMessage(), e);
			}
			return false;
		}

		boolean success = true;

		try {
			
			final Map<String, Object> props = session.getUserProperties();
			if (props.containsKey( TnConstants.HTTP_SEESION_ENCRYPT )) {
				final IAesKey aes = (IAesKey) props.get(TnConstants.HTTP_SEESION_ENCRYPT);
				wsResponse.setKey(aes);	
			}
			
			if (async) {
				session.getAsyncRemote().sendObject(wsResponse);
			} else {
				session.getBasicRemote().sendObject(wsResponse);
			}							

		} catch (IllegalStateException e) {
			// session invalidated
			LOG.warn(e.getMessage());
			LOG.debug(e.getMessage(), e);
			success = false;
		} catch (Exception e) {
			success = false;
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return success;
	}

	@Override
	public final WebSocketContainer getContainer() {
		return session.getContainer();
	}

	@Override
	public final String getId() {
		return session.getId();
	}

	@Override
	public final int getMaxBinaryMessageBufferSize() {
		return session.getMaxBinaryMessageBufferSize();
	}

	@Override
	public final long getMaxIdleTimeout() {
		return session.getMaxIdleTimeout();
	}

	@Override
	public final int getMaxTextMessageBufferSize() {
		return session.getMaxTextMessageBufferSize();
	}

	@Override
	public final Set<MessageHandler> getMessageHandlers() {
		return session.getMessageHandlers();
	}

	@Override
	public final List<Extension> getNegotiatedExtensions() {
		return session.getNegotiatedExtensions();
	}

	@Override
	public final String getNegotiatedSubprotocol() {
		return session.getNegotiatedSubprotocol();
	}

	@Override
	public final Set<Session> getOpenSessions() {
		return session.getOpenSessions();
	}

	@Override
	public final Map<String, String> getPathParameters() {
		return session.getPathParameters();
	}

	@Override
	public final String getProtocolVersion() {
		return session.getProtocolVersion();
	}

	@Override
	public final String getQueryString() {
		return session.getQueryString();
	}

	@Override
	public final Map<String, List<String>> getRequestParameterMap() {
		return session.getRequestParameterMap();
	}

	@Override
	public final URI getRequestURI() {
		return session.getRequestURI();
	}

	@Override
	public final Principal getUserPrincipal() {
		return session.getUserPrincipal();
	}

	@Override
	public final Map<String, Object> getUserProperties() {
		return session.getUserProperties();
	}

	@Override
	public final boolean isOpen() {
		return session.isOpen();
	}

	@Override
	public final boolean isSecure() {
		return session.isSecure();
	}

	@Override
	public final void removeMessageHandler(final MessageHandler arg0) {
		session.removeMessageHandler(arg0);
	}

	@Override
	public final void setMaxBinaryMessageBufferSize(final int arg0) {
		session.setMaxBinaryMessageBufferSize(arg0);
	}

	@Override
	public final void setMaxIdleTimeout(final long arg0) {
		session.setMaxIdleTimeout(arg0);
	}

	@Override
	public final void setMaxTextMessageBufferSize(final int arg0) {
		session.setMaxTextMessageBufferSize(arg0);
	}

	public final HttpSession getHttpSession() {
		return (HttpSession) session.getUserProperties().get(HttpSession.class.getCanonicalName());
	}

	public final boolean isValidHttpSession() {

		final HttpSession httpSession = getHttpSession();

		if (httpSession == null) {
			return false;
		}

		try {
			final String attr = (String) httpSession.getAttribute(TnConstants.HTTP_SEESION_STATUS);
			return Boolean.TRUE.toString().equals(attr);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public final boolean equals(final Object obj) {

		boolean status = false;

		if (obj instanceof WebSocketSession) {

			try {

				final Field f = WebSocketSession.class.getField("session");
				f.setAccessible(true);

				final Object o = f.get(obj);
				status = session.equals(o);

			} catch (Exception e) {
				status = false;
				LOG.debug(e.getMessage(), e);
			}

		}

		return status;
	}

	@Override
	public final int hashCode() {
		return session.hashCode();
	}

	@Override
	public final Async getAsyncRemote() {
		return session.getAsyncRemote();
	}

	@Override
	public final Basic getBasicRemote() {
		return session.getBasicRemote();
	}

	@Override
	public <T> void addMessageHandler(final Class<T> arg0, final Whole<T> arg1) {

	}

	@Override
	public <T> void addMessageHandler(final Class<T> arg0, final Partial<T> arg1) {

	}

}
