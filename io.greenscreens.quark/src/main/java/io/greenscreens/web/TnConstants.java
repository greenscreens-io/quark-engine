/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.web;

/**
 * Constants used by web to store session values.
 */
public enum TnConstants {
    ;
    
    public static final String HTTP_SEESION_STATUS   = "io.greenscreens.session.status";
    public static final String HTTP_SEESION_ENCRYPT  = "io.greenscreens.session.encrypt";
    
    public static final String WEBSOCKET_SESSION     = "io.greenscreens.websocket.session";
    public static final String WEBSOCKET_PATH        = "io.greenscreens.websocket.path";
    public static final String WEBSOCKET_CHALLENGE   = "io.greenscreens.websocket.challenge";
    public static final String WEBSOCKET_SUBPROTOCOL = "ws4is";
    public static final String WEBSOCKET_TYPE        = "ws";
    
	public static final String LOG_BROADCAST_INJECT = "Websocket broadcast event not injected in callback.";
	public static final String LOG_RSA_ERROR = "Decryption error. Dynamic ecryption mode does not allow url reuse.";
	public static final String LOG_URL_OVERLOAD = "URL request is too long";
		
}
