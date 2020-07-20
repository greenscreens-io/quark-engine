/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import javax.websocket.CloseReason;

/**
 * Class holding event data  
 */
public class WebsocketEvent {

    private final WebSocketSession session;
    private final WebSocketEventStatus eventStatus;
    private final Throwable throwable;
    private final CloseReason reason;

    public WebsocketEvent(final WebSocketSession session, final WebSocketEventStatus eventStatus, final CloseReason reason) {
        super();
        this.session = session;
        this.eventStatus = eventStatus;
        this.throwable = null;
        this.reason = reason;
    }
    
    public WebsocketEvent(final WebSocketSession session, final WebSocketEventStatus eventStatus) {
        super();
        this.session = session;
        this.eventStatus = eventStatus;
        this.throwable = null;
        this.reason = null;
    }

    public WebsocketEvent(final WebSocketSession session, final WebSocketEventStatus eventStatus, final Throwable throwable) {
        super();
        this.session = session;
        this.eventStatus = eventStatus;
        this.throwable = throwable;
        this.reason = null;
    }

    public final WebSocketSession getWebSocketSession() {
        return session;
    }

    public final WebSocketEventStatus getEventStatus() {
        return eventStatus;
    }

    public final Throwable getThrowable() {
        return throwable;
    }

	public CloseReason getReason() {
		return reason;
	}
    
}
