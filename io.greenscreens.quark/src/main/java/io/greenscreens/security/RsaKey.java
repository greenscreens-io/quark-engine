/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for handling RSA keys Support for Web Crypto API
 */
enum RsaKey {
	;

	private static final Logger LOG = LoggerFactory.getLogger(RsaKey.class);

	// 2048 not supported in legacy mode
	private static int KEY_SIZE = 1024; // 2048;

	private static KeyPair keyPairENCDEC = null;
	private static KeyPair keyPairVERSGN = null;
	private static String pemENCDEC = null;
	private static String pemVERSGN = null;
	private static String pemPrivENCDEC = null;

	private static final String WEB_MODE = "SHA384withECDSA";

	/**
	 * Initialize RSA key
	 */
	public static void initialize() {
		try {
			init();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
	}

	/**
	 * Internal init
	 * 
	 * @throws Exception
	 */
	static void init() throws Exception {

		KeyPairGenerator gen = null;

		gen = KeyPairGenerator.getInstance("RSA", "BC");
		gen.initialize(KEY_SIZE);
		keyPairENCDEC = gen.generateKeyPair();

		initVerificator();

		pemENCDEC = RsaUtil.toPublicPem(keyPairENCDEC);
		pemPrivENCDEC = RsaUtil.toPrivatePem(keyPairENCDEC);
	}

	static void initVerificator() throws Exception {
		if (keyPairVERSGN == null) {
			final ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("P-384");
			final KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", "BC");
			gen.initialize(spec);
			keyPairVERSGN = gen.generateKeyPair();
			pemVERSGN = RsaUtil.toPublicPem(keyPairVERSGN);
		}
	}

	/**
	 * Set new keys (support for dynamic web encryption)
	 * 
	 * @param pubKey
	 * @param privKey
	 */
	public static void setKeys(final PublicKey pubKey, final PrivateKey privKey) {

		try {
			keyPairENCDEC = new KeyPair(pubKey, privKey);
			pemENCDEC = RsaUtil.toPublicPem(keyPairENCDEC);
			pemPrivENCDEC = RsaUtil.toPrivatePem(keyPairENCDEC);
			initVerificator();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
	}

	/**
	 * Expose private key
	 * 
	 * @return
	 */
	public static PrivateKey getPrivateKey() {
		return keyPairENCDEC.getPrivate();
	}

	/**
	 * Expose public key
	 * 
	 * @return
	 */
	public static PublicKey getPublicKey() {
		return keyPairENCDEC.getPublic();
	}

	/**
	 * Get Public RSA key in PEM format
	 * 
	 * @param flat
	 * @return
	 */
	public static String getPublicEncoder(final boolean flat) {
		if (flat) {
			return RsaUtil.flatten(pemENCDEC);
		}
		return pemENCDEC;
	}

	/**
	 * Get Private RSA key in PEM format
	 * 
	 * @param flat
	 * @return
	 */
	public static String getPrivateEncoder(final boolean flat) {
		if (flat) {
			return RsaUtil.flatten(pemPrivENCDEC);
		}
		return pemPrivENCDEC;
	}

	/**
	 * Get Public RSA key in PEM format for Signing
	 * 
	 * @param flat
	 * @return
	 */
	public static String getPublicVerifier(final boolean flat) {
		if (flat) {
			return RsaUtil.flatten(pemVERSGN);
		}
		return pemVERSGN;
	}

	/**
	 * Convert signature to format for Web Crypto API
	 * 
	 * @param signedData
	 * @return
	 * @throws Exception
	 */
	static byte[] signConvert(byte[] signedData) throws Exception {
		int len = Transcoder.getSignatureByteArrayLength(384);
		final byte[] signEC = Transcoder.transcodeSignatureToConcat(signedData, len);
		return signEC;
	}

	/**
	 * Sign public key encrypt and public verify certificate with client challenge
	 * 
	 * @param challenge
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static byte[] signChallenge(final byte[] challenge, final boolean webCryptoAPI) throws Exception {

		final Signature signature = Signature.getInstance(WEB_MODE);
		signature.initSign(keyPairVERSGN.getPrivate());
		signature.update(challenge);

		if (webCryptoAPI) {
			signature.update(RsaUtil.flatten(pemENCDEC).getBytes());
			signature.update(RsaUtil.flatten(pemVERSGN).getBytes());
		} else {
			signature.update(pemENCDEC.getBytes());
			signature.update(pemVERSGN.getBytes());
		}

		final byte[] signedData = signature.sign();

		if (webCryptoAPI) {
			return signConvert(signedData);
		} else {
			return signedData;
		}
	}

	/**
	 * Generic text sign
	 * 
	 * @param data
	 * @param isHex
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String signChallenge(final String data, final boolean isHex, final boolean webCryptoAPI)
			throws Exception {
		if (isHex) {
			return signHexChallenge(data, webCryptoAPI);
		} else {
			return signBase64Challenge(data, webCryptoAPI);
		}
	}

	/**
	 * Generic text sign with Hex String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String signHexChallenge(final String data, final boolean webCryptoAPI) throws Exception {
		final byte[] signature = signChallenge(data.getBytes(), webCryptoAPI);
		return Security.bytesToHex(signature);
	}

	/**
	 * Generic text sign with Base64 String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String signBase64Challenge(final String data, final boolean webCryptoAPI) throws Exception {
		final Encoder base64 = Base64.getEncoder();
		final byte[] signature = signChallenge(data.getBytes(), webCryptoAPI);
		return base64.encodeToString(signature);
	}

	/**
	 * Generic string sign
	 * 
	 * @param data
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(final String data, final boolean webCryptoAPI) throws Exception {

		final Signature signature = Signature.getInstance(WEB_MODE);
		signature.initSign(keyPairVERSGN.getPrivate());
		signature.update(data.getBytes());

		final byte[] signedData = signature.sign();

		if (webCryptoAPI) {
			return signConvert(signedData);
		} else {
			return signedData;
		}
	}

	/**
	 * Generic text sign
	 * 
	 * @param data
	 * @param isHex
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String sign(final String data, final boolean isHex, final boolean webCryptoAPI) throws Exception {
		if (isHex) {
			return signHex(data, webCryptoAPI);
		} else {
			return signBase64(data, webCryptoAPI);
		}
	}

	/**
	 * Generic text sign with Hex String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String signHex(final String data, final boolean webCryptoAPI) throws Exception {
		final byte[] signature = sign(data, webCryptoAPI);
		return Security.bytesToHex(signature);
	}

	/**
	 * Generic text sign with Base64 String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws Exception
	 */
	public static String signBase64(final String data, final boolean webCryptoAPI) throws Exception {
		final Encoder base64 = Base64.getEncoder();
		final byte[] signature = sign(data, webCryptoAPI);
		return base64.encodeToString(signature);
	}

	/**
	 * Verify RSA signature on given data
	 * 
	 * @param data
	 * @param signature
	 * @param isHex
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(final String data, final String signature, final boolean isHex) throws Exception {
		if (isHex) {
			return verifyHex(data, signature);
		} else {
			return verifyBase64(data, signature);
		}
	}

	/**
	 * Verify RSA signature on given data in Hex String format
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static boolean verifyHex(final String data, final String signature) throws Exception {
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = Security.hexToBytes(signature);
		return verify(dataBin, signatureBin);
	}

	/**
	 * Verify RSA signature on given data in Base64 encoded format
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static boolean verifyBase64(final String data, final String signature) throws Exception {
		final Decoder base64 = Base64.getDecoder();
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = base64.decode(signature);
		return verify(dataBin, signatureBin);
	}

	/**
	 * Generic verify for bytes
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, byte[] sig) throws Exception {
		Signature signature = Signature.getInstance(WEB_MODE);
		signature.initVerify(keyPairVERSGN.getPublic());
		signature.update(data);
		return signature.verify(sig);
	}

}
