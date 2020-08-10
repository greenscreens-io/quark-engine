/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo1;

import java.util.Enumeration;

import javax.enterprise.inject.Produces;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.ibm.as400.access.AS400;

/**
 * Custom session producer which will always create http session
 * Also, will destroy all as400 object connections if exists upon session destruction.
 */
@WebListener
public class SessionProducer implements ServletRequestListener, HttpSessionListener {

    private static final ThreadLocal<HttpSession> SESSIONS = new ThreadLocal<>();
    
    @Override
	public void sessionCreated(final HttpSessionEvent se) {}

    /**
     * We want to ensure to release as400 resources if session is destroyed 
     */
	@Override
	public void sessionDestroyed(final HttpSessionEvent se) {

		final HttpSession session = se.getSession();
		final Enumeration<String> keys = session.getAttributeNames();
		
		while (keys.hasMoreElements()) {
			
			String key = keys.nextElement();
			Object obj = session.getAttribute(key);
		
			if (obj != null && obj instanceof AS400) {
				((AS400)obj).disconnectAllServices();	
			}
			
		}
		
	}

	@Override
    public void requestDestroyed(final ServletRequestEvent sre) {    	
        SESSIONS.remove();
    }

    @Override
    public void requestInitialized(final ServletRequestEvent sre) {
        SESSIONS.set(HttpServletRequest.class.cast(sre.getServletRequest()).getSession());
    }

    @Produces @Autoinit
    protected HttpSession getSession() {
        return SESSIONS.get();
    }

}