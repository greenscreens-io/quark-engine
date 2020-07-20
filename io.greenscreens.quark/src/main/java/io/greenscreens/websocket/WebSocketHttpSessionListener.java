/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import io.greenscreens.web.TnConstants;

/**
 * Set session status flags for WebSOcket service to be able to detect if session is destroyed.
 * For example, in SPA apps, user can logout, but on second browser tab websocket is still active, 
 * so WS has to know session is ended to prevent invalid calls. 
 */
@WebListener
public final class WebSocketHttpSessionListener implements HttpSessionListener, ServletContextListener {

    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        final HttpSession httpSession = event.getSession();
        httpSession.setAttribute(TnConstants.HTTP_SEESION_STATUS, Boolean.TRUE.toString());
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession httpSession = event.getSession();
        httpSession.setAttribute(TnConstants.HTTP_SEESION_STATUS, Boolean.FALSE.toString());
    }

	@Override
	public void contextDestroyed(final ServletContextEvent event) {}

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		//TnConstants.initialize(event.getServletContext());
	}

}
