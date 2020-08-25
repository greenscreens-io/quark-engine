/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.quark.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.Util;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.ext.ExtEncrypt;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;
import io.greenscreens.quark.web.data.WebRequest;
import io.greenscreens.quark.websocket.WebSocketOperations;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;

/**
 * Servlet to render API structure
 */
public class APIServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(APIServlet.class);

	@Inject
	public BeanManagerUtil BM;

	private WebSocketOperations<JsonNode> wsOperations;

	public APIServlet() {
		super();
		wsOperations = new WebSocketOperations<JsonNode>();
	}

	/**
	 * GET request will export API and public key used for front initialization
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String json = "";

		String challenge = request.getHeader("x-time");

		try {

			final ArrayNode api = BM.getAPI();
			final ObjectNode root = Util.buildAPI(api, challenge);
			json = JsonDecoder.stringify(root);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		response.getWriter().append(json);
	}

	/**
	 * Post request will process non-encrypted / encrypted requests
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String json = "";
		String body = null;
		JsonDecoder<ObjectNode> decoderBase = null;

		ExtJSDirectRequest<JsonNode> req = null;
		ExtJSDirectResponse<JsonNode> res = null;
		ObjectNode node = null;

		try {

			body = Util.getBodyAsString(request);
			decoderBase = new JsonDecoder<>(ObjectNode.class, body);
			node = decoderBase.getObject();

			if (node.has("d") && node.has("k")) {
				req = decrypt(request, node);
			} else {
				req = getRequest(body);
			}

			res = wsOperations.process(req, request.getSession(true), request.getServletPath());

			json = JsonDecoder.stringify(res);
			json = encrypt(request, json);

			response.setContentType("application/json");
			response.getWriter().append(json);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

	}

	/**
	 * Decrypt received encrypted request
	 *
	 * @param request
	 * @param node
	 * @return
	 * @throws Exception
	 */
	private ExtJSDirectRequest<JsonNode> decrypt(final HttpServletRequest request,
	 		final ObjectNode node)
			throws Exception {

		String data = null;
		ExtJSDirectRequest<JsonNode> req = null;

		final ExtEncrypt encrypt = new ExtEncrypt();
		encrypt.setD(node.get("d").asText());
		encrypt.setK(node.get("k").asText());

		final HttpSession session = request.getSession(true);
		IAesKey crypt = (IAesKey) session.getAttribute(QuarkConstants.HTTP_SEESION_ENCRYPT);

		if (crypt == null) {
			crypt = Security.initAESfromRSA(encrypt.getK());
			session.setAttribute(QuarkConstants.HTTP_SEESION_ENCRYPT, crypt);
			data = crypt.decrypt(encrypt.getD());
		} else {
			data = Security.decodeRequest(encrypt.getD(), encrypt.getK(), crypt);
		}

		req = getRequest(data);

		return req;
	}

	/**
	 * Encrypt response JSON into encrypted JSON format
	 *
	 * @param request
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private final String encrypt(final HttpServletRequest request,
			final String data) throws Exception {

		final IAesKey crypt = (IAesKey) request.getSession()
			.getAttribute(QuarkConstants.HTTP_SEESION_ENCRYPT);

		if (crypt == null) {
			return data;
		}

		final byte[] iv = Security.getRandom(crypt.getCipher().getBlockSize());
		final String enc = crypt.encrypt(data, iv);
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("iv", Util.bytesToHex(iv));
		node.put("d", enc);
		node.put("cmd", WebSocketInstruction.ENC.toString());
		final String json = JsonDecoder.getJSONEngine().writeValueAsString(node);
		return json;
	}

	/**
	 * Decode JSON request into engine data format
	 *
	 * @param json
	 * @return
	 * @throws IOException
	 */
	private ExtJSDirectRequest<JsonNode> getRequest(final String json) throws IOException {

		JsonDecoder<WebRequest> decoder = null;
		ExtJSDirectRequest<JsonNode> req = null;

		decoder = new JsonDecoder<>(WebRequest.class, json);
		req = decoder.getObject();

		return req;
	}

}
