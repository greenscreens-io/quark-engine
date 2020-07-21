/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.demo;

import javax.enterprise.event.Observes;
import javax.websocket.server.ServerEndpoint;

import io.greenscreens.websocket.WebSocketConfigurator;
import io.greenscreens.websocket.WebSocketService;
import io.greenscreens.websocket.WebsocketBroadcastEvent;
import io.greenscreens.websocket.WebsocketDecoder;
import io.greenscreens.websocket.WebsocketEncoder;

@ServerEndpoint(
		value = DemoURLConstants.WSOCKET,
		configurator = WebSocketConfigurator.class,
        decoders = { WebsocketDecoder.class}, 
        encoders = { WebsocketEncoder.class})
public class DemoAPIWebSocketService extends WebSocketService {
	
	public void broadcast(@Observes final WebsocketBroadcastEvent wsEvent) {
    	super.broadcast(wsEvent.getData());
    }
	
}
