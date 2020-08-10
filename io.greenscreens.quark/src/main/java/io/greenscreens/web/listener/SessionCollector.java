/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.web.listener;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class SessionCollector implements HttpSessionListener, HttpSessionActivationListener {

	private static final Map<Integer, HttpSession> sessions = new ConcurrentHashMap<Integer, HttpSession>();

	@Override
	public void sessionCreated(final HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.put(session.getId().hashCode(), session);
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent event) {
		final HttpSession ses = event.getSession();
		sessions.remove(ses.getId().hashCode());
	}

	public static Map<Integer, HttpSession> get() {
		return Collections.unmodifiableMap(sessions);
	}

	public static HttpSession get(final String key) {
		return sessions.get(key.hashCode());
	}

	public static HttpSession get(final int key) {
		return sessions.get(key);
	}

	public static void updateSessionTimeout(final int tout) {
		final Iterator<HttpSession> sess = sessions.values().iterator();
		while (sess.hasNext()) {
			sess.next().setMaxInactiveInterval(tout);
		}
	}

}
