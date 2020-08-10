/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo;

import javax.enterprise.event.Observes;
import javax.websocket.server.ServerEndpoint;

import io.greenscreens.websocket.WebSocketConfigurator;
import io.greenscreens.websocket.WebSocketService;
import io.greenscreens.websocket.WebsocketBroadcastEvent;
import io.greenscreens.websocket.WebsocketDecoder;
import io.greenscreens.websocket.WebsocketDecoderBinary;
import io.greenscreens.websocket.WebsocketEncoder;
import io.greenscreens.websocket.WebsocketEncoderBinary;

/**
 * WebSocket service registered at DemoURLConstants.WSOCKET 
 */
@ServerEndpoint(
		value = DemoURLConstants.WSOCKET,
		configurator = WebSocketConfigurator.class,
        decoders = { WebsocketDecoder.class, WebsocketDecoderBinary.class}, 
        encoders = { WebsocketEncoder.class, WebsocketEncoderBinary.class})
public class DemoAPIWebSocketService extends WebSocketService {
	
	public void broadcast(@Observes final WebsocketBroadcastEvent wsEvent) {
    	super.broadcast(wsEvent.getData());
    }
	
}
