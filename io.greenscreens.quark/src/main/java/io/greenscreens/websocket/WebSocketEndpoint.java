/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.Util;
import io.greenscreens.cdi.BeanManagerUtil;
import io.greenscreens.ext.ExtJSDirectRequest;
import io.greenscreens.ext.ExtJSDirectResponse;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.web.QuarkConstants;
import io.greenscreens.websocket.data.IWebSocketResponse;
import io.greenscreens.websocket.data.WebSocketInstruction;
import io.greenscreens.websocket.data.WebSocketRequest;
import io.greenscreens.websocket.data.WebSocketResponse;
import io.greenscreens.websocket.data.WebSocketResponseBinary;

/**
 * Internal CDI injectable object used by WebSocket endpoint instance. Used to
 * separate internal logic from WebSocketService.
 */
public class WebSocketEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static final String MSG_HTTP_SEESION_REQUIRED = "WebSocket requires valid http session";

	private static final ThreadLocal<WebSocketSession> websocketContextThreadLocal = new ThreadLocal<WebSocketSession>();
	// private static final long MINUTE = 60_000;

	@Inject
	private BeanManagerUtil BM;
	
	@Inject
	private Event<WebsocketEvent> webSocketEvent;

	@Inject
	private WebSocketOperations<JsonNode> wsOperations;

	static private Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

	@Produces
	private WebSocketSession sessionProducer() {
		return websocketContextThreadLocal.get();
	}

	/**
	 * Send messages to all connected parties
	 * 
	 * @param message
	 */
	public void broadcast(final IWebSocketResponse message) {

		if (sessions != null) {

			LOG.trace("Broadcasting message {}", message);

			for (Session session : sessions) {

				try {
					if (session != null && session.isOpen()) {
						session.getBasicRemote().sendObject(message);
						LOG.trace("for session {}", session);
					}
				} catch (Exception e) {
					LOG.error(e.getMessage());
					LOG.debug(e.getMessage(), e);
				}
			}

		}

	}

	/*
	 * PUBLIC SECTION
	 */
	public final void onMessage(final WebSocketRequest message, final Session session) {

		WebSocketSession wsession = null;

		try {

			if (!QuarkConstants.WEBSOCKET_TYPE.equals(message.getType())) {
				return;
			}

			LOG.trace("Received message {} \n      for session : {}", message, session);

			wsession = new WebSocketSession(session);
			websocketContextThreadLocal.set(wsession);
			// do not use - to not to overload for many users
			// webSocketEvent.fire(new WebsocketEvent(wsession,
			// WebSocketEventStatus.MESSAGE));

			final WebSocketInstruction cmd = message.getCmd();
			if (cmd == null)
				return;

			switch (cmd) {
			case WELCO:
				processSimple(wsession, message);
				break;
			case ENC:
				processData(true, wsession, message);
				break;
			case DATA:
				processData(false, wsession, message);
				break;
			case API:
				processSimple(wsession, message);				
			case ECHO:
				processSimple(wsession, message);
				break;
			case BYE:
				processSimple(wsession, message);
				break;
			case ERR:
				break;
			default:
				break;
			}

		} catch (Exception e) {

			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);

			if (wsession != null) {
				wsession.sendResponse(getErrorResponse(e, message.isBinary()), true);
			}

		} finally {
			websocketContextThreadLocal.remove();
		}
	}

	// allow this websocket endpoint only for clients with valid session attached
	public final void onOpen(final Session session, final EndpointConfig config) {

		try {
			LOG.trace("Openning new WebSocket connection : {} ", session);

			final Map<String, Object> sessProps = session.getUserProperties();
			final Map<String, Object> endpProps = config.getUserProperties();

			boolean requireSession = false;

			if (sessProps.containsKey(QuarkConstants.WEBSOCKET_SESSION)) {
				requireSession = (boolean) sessProps.get(QuarkConstants.WEBSOCKET_SESSION);
			}

			final HttpSession httpSession = (HttpSession) endpProps.get(HttpSession.class.getName());
			final WebSocketSession wsession = new WebSocketSession(session, httpSession);

			// disable websocket session timeout due to inactivity
			// session.setMaxIdleTimeout(MINUTE * 30);
			session.setMaxIdleTimeout(0);

			sessProps.put(QuarkConstants.WEBSOCKET_PATH, endpProps.get(QuarkConstants.WEBSOCKET_PATH));

			websocketContextThreadLocal.set(wsession);

			boolean allowed = true;
			String reason = null;

			wsOperations.setRequiredSession(requireSession);

			if (requireSession && !wsession.isValidHttpSession()) {
				allowed = false;
				reason = MSG_HTTP_SEESION_REQUIRED;
			}

			if (allowed == false) {
				LOG.error(reason);
				final CloseCode closeCode = new CloseCode() {

					@Override
					public int getCode() {
						return 4000;
					}
				};
				session.close(new CloseReason(closeCode, ""));

			} else {
				webSocketEvent.fire(new WebsocketEvent(wsession, WebSocketEventStatus.START));
			}

			updateSessions(wsession);

			if (session.getUserProperties().containsKey(QuarkConstants.WEBSOCKET_CHALLENGE)) {
				sendAPI(wsession);	
			}
			
		} catch (IOException e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		} finally {
			websocketContextThreadLocal.remove();
		}

	}

	public final void onClose(final Session session, final CloseReason reason) {

		final WebSocketSession wsession = new WebSocketSession(session);
		LOG.warn("Closing WebSocket session with reason code : {}, Session: {}", reason.getCloseCode().getCode(),
				wsession);

		try {

			websocketContextThreadLocal.set(wsession);
			WebsocketEvent event = new WebsocketEvent(wsession, WebSocketEventStatus.CLOSE, reason);
			webSocketEvent.fire(event);

		} finally {

			websocketContextThreadLocal.remove();
			updateSessions(wsession);

		}
	}

	public final void onError(final Session session, final Throwable throwable) {

		final WebSocketSession wsession = new WebSocketSession(session);

		LOG.error("WebSocket error for session : {},  Message: {}", wsession, throwable.getMessage());

		try {

			websocketContextThreadLocal.set(wsession);
			webSocketEvent.fire(new WebsocketEvent(wsession, WebSocketEventStatus.ERROR, throwable));

		} finally {
			websocketContextThreadLocal.remove();
		}
	}

	/*
	 * PRIVATE SECTION
	 */

	private void updateSessions(final WebSocketSession session) {

		LOG.trace("updateSessions");

		if (!session.isOpen()) {
			sessions.remove(session);
		} else if (!sessions.contains(session)) {
			sessions.add(session);
		}
	}

	private IWebSocketResponse getErrorResponse(final Exception exception, final boolean isBinary) {

		final ExtJSResponse response = new ExtJSResponse(exception, exception.getMessage());
		final IWebSocketResponse wsResponse = newResponse(WebSocketInstruction.ERR, isBinary);
		wsResponse.setData(response);
		wsResponse.setErrMsg(exception.getMessage());
		return wsResponse;
	}
	
	private boolean sendAPI(final WebSocketSession session) {
		
		if (BM == null) return false;
		
		if (!session.getUserProperties().containsKey(QuarkConstants.WEBSOCKET_CHALLENGE)) {
			return false;
		}
		
		final String challenge = (String) session.getUserProperties().get(QuarkConstants.WEBSOCKET_CHALLENGE);
		final WebSocketResponse wsResposne = new WebSocketResponse(WebSocketInstruction.API);		
		
		final ArrayNode api = BM.getAPI();
		final ObjectNode root = Util.buildAPI(api, challenge); 
		wsResposne.setData(root);		
		
		session.sendResponse(wsResposne, true);
		return true;
	}


	private void processSimple(final WebSocketSession session, final WebSocketRequest message) {
		
		final WebSocketInstruction cmd = message.getCmd();
				
		if (WebSocketInstruction.API == cmd && BM != null) {
			sendAPI(session);
		}
		
		final IWebSocketResponse response = newResponse(cmd, message.isBinary());
		session.sendResponse(response, true);	

	}

	private void processData(final boolean encrypted, final WebSocketSession session, final WebSocketRequest wsMessage)
			throws IOException, EncodeException {

		LOG.trace("processData");

		final List<ExtJSDirectResponse<?>> responseList = new ArrayList<ExtJSDirectResponse<?>>();
		final Map<String, Object> map = session.getUserProperties();
		final String wsPath = (String) map.get(QuarkConstants.WEBSOCKET_PATH);
		final List<ExtJSDirectRequest<JsonNode>> requests = wsMessage.getData();

		for (final ExtJSDirectRequest<JsonNode> request : requests) {
			processRequest(encrypted, session, request, wsPath, responseList);
		}

		final IWebSocketResponse wsResponse = newResponse(WebSocketInstruction.DATA, wsMessage.isBinary());	
		wsResponse.setData(responseList);
		session.sendResponse(wsResponse, true);
		
	}
	
	private IWebSocketResponse newResponse(final WebSocketInstruction instruction, final boolean isBinary) {
		if (isBinary) {
			return new WebSocketResponseBinary(instruction);
		}
		return new WebSocketResponse(instruction);
	}

	private void processRequest(final boolean encrypted, final WebSocketSession session,
			final ExtJSDirectRequest<JsonNode> request, final String wsPath,
			final List<ExtJSDirectResponse<?>> responseList) throws IOException, EncodeException {

		LOG.trace("processRequest");

		ExtJSDirectResponse<?> extResponse = null;
		try {

			if (encrypted) {
				extResponse = wsOperations.processEncrypted(request, session, wsPath);
			} else {
				extResponse = wsOperations.process(request, session.getHttpSession(), wsPath);
			}

		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			final ExtJSResponse errorResponse = new ExtJSResponse(e, e.getMessage());
			extResponse = new ExtJSDirectResponse<>(request, errorResponse);
		}

		if (extResponse != null) {
			responseList.add(extResponse);
		}
	}

}
