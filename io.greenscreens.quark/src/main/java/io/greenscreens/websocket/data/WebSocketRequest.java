/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.websocket.data;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import io.greenscreens.ext.ExtJSDirectRequest;
import io.greenscreens.web.TnConstants;

/**
 * Class used to map JSON structure describing ExtJS WebSocket request.
 */
public class WebSocketRequest {

	public final String type = TnConstants.WEBSOCKET_TYPE;

	private WebSocketInstruction cmd; // 'welcome , bye, data' ,
	private int timeout; // set only when cmd=welcome

	private String errMsg;
	private int errId;

	// list of commands - batch
	private ArrayList<ExtJSDirectRequest<JsonNode>> data;

	public final WebSocketInstruction getCmd() {
		return cmd;
	}

	public final void setCmd(final WebSocketInstruction cmd) {
		this.cmd = cmd;
	}

	public final int getTimeout() {
		return timeout;
	}

	public final void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public final String getErrMsg() {
		return errMsg;
	}

	public final void setErrMsg(final String errMsg) {
		this.errMsg = errMsg;
	}

	public final int getErrId() {
		return errId;
	}

	public final void setErrId(final int errId) {
		this.errId = errId;
	}

	public final String getType() {
		return type;
	}

	public final ArrayList<ExtJSDirectRequest<JsonNode>> getData() {
		return data;
	}

	public final void setData(final ArrayList<ExtJSDirectRequest<JsonNode>> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "WebSocketRequest [type=" + type + ", cmd=" + cmd + ", timeout=" + timeout + ", errMsg=" + errMsg
				+ ", errId=" + errId + ", data=" + data + "]";
	}

}
