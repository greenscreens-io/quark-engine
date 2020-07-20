/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple util class for string handling
 */
public enum Util {
	;

	private static final Logger LOG = LoggerFactory.getLogger(Util.class);

	final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	/**
	 * Converts byte array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(final byte[] bytes) {

		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;

		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Convert char array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String charsToHex(final char[] bytes) {

		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;

		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j];
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Converts hex string into byte array
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(final String s) {

		final int len = s.length();
		final byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Internal text to boolean conversion
	 * 
	 * @param value
	 * @return
	 */
	public static boolean toBoolean(final String value) {

		if (value == null) {
			return false;
		}

		if (Boolean.TRUE.toString().equals(value.trim().toLowerCase())) {
			return true;
		}

		if (Boolean.FALSE.toString().equals(value.trim().toLowerCase())) {
			return false;
		}

		return false;
	}

	/**
	 * Internal text to int conversion
	 * 
	 * @param value
	 * @return
	 */
	public static int toInt(final String value) {

		int val = 0;

		try {
			val = Integer.parseInt(normalize(value, "0").trim());
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return val;
	}

	/**
	 * Internal text to long conversion
	 * 
	 * @param value
	 * @return
	 */
	public static long toLong(final String value) {

		long val = 0;

		try {
			val = Long.parseLong(normalize(value, "0").trim());
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return val;
	}

	/**
	 * Prevent null string
	 * 
	 * @param data
	 * @return
	 */
	public static String normalize(final String data) {
		return normalize(data, "");
	}

	/**
	 * Prevent null string
	 * 
	 * @param data
	 * @return
	 */
	public static String normalize(final String data, final String def) {
		return Optional.ofNullable(data).orElse(def);
	}

	/**
	 * Get string length, null support
	 * 
	 * @param data
	 * @return
	 */
	public static int length(final String data) {
		return Optional.ofNullable(data).orElse("").length();
	}

	public static boolean isEqual(final String val1, final String val2) {
		return normalize(val1).equals(normalize(val2));
	}

	/**
	 * Convert generic object to given type result
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getObject(Object object) {
		if (object == null) {
			return null;
		}
		return (T) object;
	}

	/**
	 * Clone ByteBuffer
	 * 
	 * @param original
	 * @return
	 */
	public static ByteBuffer clone(ByteBuffer original) {
		ByteBuffer clone = ByteBuffer.allocate(original.capacity());
		original.rewind();// copy from the beginning
		clone.put(original);
		original.rewind();
		clone.flip();
		return clone;
	}

	/**
	 * Close any closable objects like stream
	 * 
	 * @param closeable
	 */
	public static void close(final AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				LOG.error(e.getMessage());
				LOG.debug(e.getMessage(), e);
			}
		}
	}

	/**
	 * Convert post request data to string
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String getBodyAsString(HttpServletRequest request) throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = request.getReader();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}

		return sb.toString();
	}

	/**
	 * Blank padding for AES algorithm
	 * 
	 * @param source
	 * @return
	 */
	public static String padString(final String source, int size) {

		final char paddingChar = ' ';
		final int x = source.length() % size;
		final int padLength = size - x;

		final StringBuffer sb = new StringBuffer(source);

		for (int i = 0; i < padLength; i++) {
			sb.append(paddingChar);
		}

		return sb.toString();
	}

}
