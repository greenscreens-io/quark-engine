/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import java.nio.ByteBuffer;

import com.ibm.as400.access.AS400DataType;

import io.greenscreens.jt400.annotations.Id;
import io.greenscreens.jt400.annotations.Input;
import io.greenscreens.jt400.annotations.JT400Argument;
import io.greenscreens.jt400.annotations.JT400Program;
import io.greenscreens.jt400.annotations.Output;
import io.greenscreens.jt400.interfaces.IJT400Params;

/**
 * Definition for QDCRDEVD program arguments
 */
@JT400Program(
		library = "QSYS",
		program = "QDCRDEVD",
		arguments = 5,
		formats = {
			DEVD0100.class,
			DEVD0600.class,
			DEVD0300.class
		})
public class QDCRDEVD implements IJT400Params {

	@Id(0) @Output
	@JT400Argument(type = AS400DataType.TYPE_BYTE_ARRAY)
	ByteBuffer receiver;

	@Id(1) @Input
	@JT400Argument(type = AS400DataType.TYPE_BIN4)
	int length;

	@Id(2) @Input
	@JT400Argument(length = 10, type = AS400DataType.TYPE_TEXT)
	String formatName;

	@Id(3) @Input
	@JT400Argument(length = 10, type = AS400DataType.TYPE_TEXT)
	String deviceName;

	@Id(4) @Input @Output
	@JT400Argument(type = AS400DataType.TYPE_BYTE_ARRAY)
	ByteBuffer errorCode;

	public ByteBuffer getReceiver() {
		return receiver;
	}

	public void setReceiver(ByteBuffer receiver) {
		this.receiver = receiver;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getFormatName() {
		return formatName;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public ByteBuffer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ByteBuffer errorCode) {
		this.errorCode = errorCode;
	}

	public QDCRDEVD() {}

	protected QDCRDEVD(final Builder builder) {
		this.receiver = builder.receiver;
		this.length = builder.length;
		this.formatName = builder.formatName;
		this.deviceName = builder.deviceName;
		this.errorCode = builder.errorCode;
	}

	/**
	 * Creates builder to build {@link QDCRDEVD}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Helper builder
	 * @param clazz
	 * @param displayName
	 * @return
	 */
	public static QDCRDEVD build(final Class<DEVD0100> clazz, final String displayName) {
		return Builder.build(clazz, displayName);
	}

}
