/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket.data;

import io.greenscreens.quark.security.IAesKey;

public interface IWebSocketResponse {

	String getType();

	void setType(String type);

	String getErrMsg();

	void setErrMsg(String errMsg);

	int getErrId();

	void setErrId(int errId);

	Object getData();

	void setData(Object data);

	WebSocketInstruction getCmd();

	IAesKey getKey();

	void setKey(IAesKey key);

}