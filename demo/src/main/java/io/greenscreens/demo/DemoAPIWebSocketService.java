/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.demo;

import javax.websocket.server.ServerEndpoint;
import io.greenscreens.ext.annotations.ExtJSEndpoint;
import io.greenscreens.ext.annotations.ExtJSSession;
import io.greenscreens.websocket.WebSocketConfigurator;
import io.greenscreens.websocket.WebSocketService;
import io.greenscreens.websocket.WebsocketDecoder;
import io.greenscreens.websocket.WebsocketEncoder;

@ExtJSEndpoint
@ExtJSSession(required = false)
@ServerEndpoint(
		value = DemoURLConstants.WSOCKET,
		configurator = WebSocketConfigurator.class,
        decoders = { WebsocketDecoder.class}, 
        encoders = { WebsocketEncoder.class})
public class DemoAPIWebSocketService extends WebSocketService {
		
}
