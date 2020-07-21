/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket.data;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * WebSocket return structure {type:'ws' , cmd : * , data : *}
 */
public enum WebSocketInstruction {

    WELCO("welco"), 
    API("api"),
    BYE("bye"), 
    ERR("err"), 
    DATA("data"),
    ENC("enc"),
    INS("ins"), // internal instruction
    ECHO("echo")
	;

	private final String text;

	private WebSocketInstruction(final String text) {
		this.text = text;
	}

	@JsonValue
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

}
