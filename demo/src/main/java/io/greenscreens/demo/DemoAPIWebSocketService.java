/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.demo;

import javax.enterprise.event.Observes;
import javax.websocket.server.ServerEndpoint;

import io.greenscreens.quark.websocket.WebSocketConfigurator;
import io.greenscreens.quark.websocket.WebSocketService;
import io.greenscreens.quark.websocket.WebsocketBroadcastEvent;
import io.greenscreens.quark.websocket.WebsocketDecoder;
import io.greenscreens.quark.websocket.WebsocketDecoderBinary;
import io.greenscreens.quark.websocket.WebsocketEncoder;
import io.greenscreens.quark.websocket.WebsocketEncoderBinary;
import io.greenscreens.quark.websocket.WebsocketEvent;

@ServerEndpoint(
		value = DemoURLConstants.WSOCKET,
		configurator = WebSocketConfigurator.class,
        decoders = { WebsocketDecoder.class, WebsocketDecoderBinary.class},
        encoders = { WebsocketEncoder.class, WebsocketEncoderBinary.class})
public class DemoAPIWebSocketService extends WebSocketService {

	public void broadcast(@Observes final WebsocketBroadcastEvent wsEvent) {
		super.broadcast(wsEvent.getData());
	}

	/**
	 * Events triggered when WebSocket started / stopped
	 * @param wsEvent
	 */
	public void state(@Observes final WebsocketEvent wsEvent) {

		switch (wsEvent.getEventStatus()) {
		case CLOSE:

			break;
		case START:

			break;
		default:
			break;
		}
	}

}
