/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import java.nio.ByteBuffer;

import io.greenscreens.jt400.JT400ExtUtil;
import io.greenscreens.jt400.interfaces.IJT400Format;

/**
 * Builder to build {@link QDCRDEVD} program parameter.
 */
public class Builder {

	ByteBuffer receiver;
	int length;
	String formatName;
	String deviceName;
	ByteBuffer errorCode;

	protected Builder() {}

	public Builder withReceiver(ByteBuffer receiver) {
		this.receiver = receiver;
		return this;
	}

	public Builder withLength(int length) {
		this.length = length;
		return this;
	}

	public Builder withFormatName(String formatName) {
		this.formatName = formatName;
		return this;
	}

	public Builder withDeviceName(String deviceName) {
		this.deviceName = deviceName;
		return this;
	}

	public Builder withErrorCode(ByteBuffer errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	public QDCRDEVD build() {
		return new QDCRDEVD(this);
	}

	/**
	 * Helper method as only relevant args are format and display name.
	 * Also, validate proper format is provided 
	 * @param <T>
	 * @param clazz
	 * @param displayName
	 * @return
	 */
	public static <T extends IJT400Format> QDCRDEVD build(Class<T> clazz, final String displayName) {
		
		if (!JT400ExtUtil.contains(clazz, QDCRDEVD.class)) {
			throw new RuntimeException("Format not supported by parameter definition!");
		}
		
		final int len = JT400ExtUtil.getFormatLength(clazz);
		
		return QDCRDEVD.builder()
				.withReceiver(ByteBuffer.allocate(len))
				.withLength(len)
				.withFormatName(clazz.getName())
				.withDeviceName(displayName)
				.build();
	}

}
