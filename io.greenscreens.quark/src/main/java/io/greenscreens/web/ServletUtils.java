/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.JsonDecoder;
import io.greenscreens.security.IAesKey;
import io.greenscreens.security.Security;

/**
 * General http request utils
 */
public enum ServletUtils {
	;
	
	private static final Logger LOG = LoggerFactory.getLogger(ServletUtils.class);

	/**
	 * Decode encrypted request from query parameters
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRaw(final HttpServletRequest req) throws IOException {
		final String d = req.getParameter("d");
		final String k = req.getParameter("k");
		return decodeRaw(d, k);
	}

	/**
	 * Decode encrypted request from raw strings
	 * 
	 * @param d
	 * @param k
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRaw(final String d, final String k) throws IOException {

		LOG.debug("decodeRaw d: {}, k: {}", d, k);

		JsonNode node = null;

		if (d != null && k != null) {

			try {
				final IAesKey crypt = Security.initAES(k);
				final String data = crypt.decrypt(d);
				LOG.debug("decodeRaw decoded : {}", data);

				node = JsonDecoder.parse(data);

			} catch (BadPaddingException pe) {
				LOG.error(TnConstants.LOG_RSA_ERROR);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				LOG.debug(e.getMessage(), e);
			}

		}

		return node;
	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static JsonNode getPost(final HttpServletRequest request) throws IOException {

		String json = null;
		JsonNode node = null;

		try {
			// read text from form post - must be json
			json = ServletUtils.getFormRequest(request);

			// parse json text to encrypted json object
			node = JsonDecoder.parse(json);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new IOException(e);
		}

		return node;
	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static JsonNode getEncryptedPost(final HttpServletRequest request) throws IOException {

		JsonNode node = null;

		try {

			// parse json text to encrypted json object
			node = getPost(request);

			// get node encrypted values
			final String d = node.get("d").asText();
			final String k = node.get("k").asText();

			// decode encrypted json
			node = ServletUtils.decodeRaw(d, k);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new IOException(e);
		}

		return node;
	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static JsonNode getEncryptedPut(final MultipartMap map) throws IOException {

		JsonNode node = null;

		try {
			final String d = map.getParameter("d");
			final String k = map.getParameter("k");

			node = ServletUtils.decodeRaw(d, k);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new IOException(e);
		}

		return node;
	}

	/**
	 * Send public key and server timestamp
	 * 
	 * @param sts
	 * @param err
	 * @return
	 */
	public static final ObjectNode getResponse() {
		return getResponse(true, null, null);
	}

	/**
	 * Create JSON error response in engine JSON format
	 * 
	 * @param error
	 * @return
	 */
	public static final ObjectNode getResponse(final TnErrors error) {

		if (error == null) {
			return getResponse();
		}

		return getResponse(false, error.getString(), error.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param error
	 * @return
	 */
	public static final ObjectNode getResponse(final boolean sts, final String error) {
		return getResponse(sts, error, TnErrors.E9999.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param err
	 * @param code
	 * @return
	 */
	public static final ObjectNode getResponse(final boolean sts, final String err, final String code) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode root = factory.objectNode();

		root.put("success", sts);
		root.put("ver", 0);
		root.put("ts", System.currentTimeMillis());

		if (!sts) {
			root.put("error", err);
			root.put("code", code);
		}

		return root;
	}

	/**
	 * Set json response data
	 */
	public static final void sendResponse(final HttpServletResponse resp, final JsonNode json) throws IOException {
		resp.setContentType("application/json");
		final PrintWriter out = resp.getWriter();
		out.print(json);
		out.flush();
	}

	/**
	 * Read form post request as string from request stream
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static final String getFormRequest(final HttpServletRequest request) throws IOException {

		final StringBuffer jb = new StringBuffer();
		String line = null;
		final BufferedReader reader = request.getReader();

		while ((line = reader.readLine()) != null) {
			jb.append(line);
		}

		return jb.toString();
	}

	/**
	 * Get upload file name
	 * 
	 * @param part
	 * @return
	 */
	public static final String getFileName(final Part part) {

		if (part == null) {
			return null;
		}

		final String partHeader = part.getHeader("content-disposition");
		String fname = null;

		for (String content : partHeader.split(";")) {
			if (content.trim().startsWith("filename")) {
				fname = content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
				// First fix stupid MSIE behaviour (it passes full client side path along
				// filename).
				fname = fname.substring(fname.lastIndexOf('/') + 1).substring(fname.lastIndexOf('\\') + 1);
				break;
			}
		}

		return fname;
	}

	/**
	 * Get file extension
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileExt(final String file) {

		String ext = null;
		String[] segs = file.split("\\.");

		if (segs.length > 1) {
			ext = segs[segs.length - 1].toLowerCase();
		}

		return ext;
	}

}
