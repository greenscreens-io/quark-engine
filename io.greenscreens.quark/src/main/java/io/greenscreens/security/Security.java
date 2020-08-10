/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.security;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.Util;

/**
 * Helper class for handling encryption One can use
 * http://travistidwell.com/jsencrypt/demo/ to generate keys.
 * 
 * Must be initialized with ICrypt module. Create class that implements ICrypt
 * and implement encryption / decryption code.
 * 
 */
public enum Security {
	;

	private static final Logger LOG = LoggerFactory.getLogger(Security.class);

	final private static Charset ASCII = Charset.forName("ASCII");
	final private static Charset UTF8 = Charset.forName("UTF-8");

	// timeout value from config file
	private static long time;

	public static void initialize() {

		try {
			java.security.Security.removeProvider("BC");
			java.security.Security.addProvider(new BouncyCastleProvider());
			generateRSAKeys();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
	}

	/**
	 * Get password timeout value in seconds
	 * 
	 * @return
	 */
	public static long getTime() {
		return time;
	}

	/**
	 * Get password timeout value in miliseconds
	 * 
	 * @return
	 */
	public static long getTimeMilis() {
		return time * 1000;
	}

	/**
	 * Set password timeout value in seconds
	 * 
	 * @param time
	 */
	public static void setTime(final long time) {
		Security.time = time;
	}

	/**
	 * Get initialization vector for AES
	 * 
	 * @param value
	 * @return
	 */
	public static IvParameterSpec getIV(final String value) {
		final String aesIV = value.substring(0, 16);
		return new IvParameterSpec(aesIV.getBytes(ASCII));
	}

	/**
	 * Get random byte from prng generator
	 * 
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public static byte[] getRandom(final int size) throws Exception {
		final SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		final byte[] iv = new byte[size];
		randomSecureRandom.nextBytes(iv);
		return iv;
	}

	/**
	 * Init aes encryption from url encrypted request
	 * 
	 * @param k
	 * @return
	 */
	public static IAesKey initAESfromRSA(final String k) {

		IAesKey aes = null;

		final byte[] aes_data = RsaCrypt.decrypt(k, RsaKey.getPrivateKey(), true);

		if (aes_data != null) {

			final byte[] aes_iv = Arrays.copyOfRange(aes_data, 0, 16);
			final byte[] aes_key = Arrays.copyOfRange(aes_data, 16, 32);

			aes = new AesCrypt(aes_key);
			aes.setIv(aes_iv);
		}

		return aes;
	}

	/**
	 * Init AES from password
	 * 
	 * @param secretKey
	 * @return
	 */
	public static IAesKey initAES(final String secretKey) {
		return new AesCrypt(secretKey);
	}

	/**
	 * Generate new RSA key
	 * 
	 * @throws Exception
	 */
	public static void generateRSAKeys() {
		RsaKey.initialize();
	}

	/**
	 * Load RSA keys from PEM format
	 * 
	 * @param publicKey
	 * @param privateKey
	 * @throws Exception
	 */
	public static void setRSAKeys(final String publicKey, final String privateKey) throws Exception {
		PublicKey pubKey = RsaUtil.getPublicKey(publicKey);
		PrivateKey privKey = RsaUtil.getPrivateKey(privateKey);
		RsaKey.setKeys(pubKey, privKey);
	}

	/**
	 * Get active RSA public key in PEM format
	 * 
	 * @return
	 */
	public static String getRSAPublic(final boolean webCryptoAPI) {
		return RsaKey.getPublicEncoder(webCryptoAPI);
	}

	public static String getRSAVerifier(final boolean webCryptoAPI) {
		return RsaKey.getPublicVerifier(webCryptoAPI);
	}

	/**
	 * Get active RSA private key in PEM format
	 * 
	 * @return
	 */
	public static String getRSAPrivate(final boolean webCryptoAPI) {
		return RsaKey.getPrivateEncoder(webCryptoAPI);
	}

	/**
	 * Sign data with RSA key
	 * 
	 * @param data
	 * @return
	 */
	public static String sign(final String data, final boolean isHex) {

		String msg = null;

		try {
			msg = RsaKey.sign(data, isHex, true);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return msg;
	}

	/**
	 * Used by api servlet to send signed key helping prevent MITM modifications
	 * 
	 * @param challenge
	 * @return
	 */
	public static String signApiKey(final String challenge) {
		final String keyEnc = Security.getRSAPublic(true);
		final String keyVer = Security.getRSAVerifier(true);
		final String data = String.format("%s%s%s", challenge, keyEnc, keyVer);
		return Security.sign(data, false);
	}

	/**
	 * Sign data with RSA key
	 * 
	 * @param data
	 * @return
	 */
	public static String signChallenge(final String data, final boolean isHex, final boolean webCryptoAPI) {

		String msg = null;

		try {
			msg = RsaKey.signChallenge(data, isHex, webCryptoAPI);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return msg;
	}

	/**
	 * Decode url encrypted request
	 * 
	 * @param d     - data encrypted with AES
	 * @param k     - AES IV encrypted with RSA, used to decrypt d
	 * @param crypt
	 * @return
	 * @throws Exception
	 */
	public static String decodeRequest(final String d, final String k, final IAesKey crypt) throws Exception {

		final byte[] aes_data = RsaCrypt.decrypt(k, RsaKey.getPrivateKey(), true);
		final byte[] iv = Arrays.copyOfRange(aes_data, 0, 16);

		final byte[] decoded = crypt.decryptData(d, iv);
		return new String(decoded, UTF8);
	}

	/**
	 * Converts raw bytes to string hex
	 * 
	 * @param data
	 * @return
	 */
	protected static String bytesToHex(final byte[] data) {
		return Util.bytesToHex(data);
	}

	/**
	 * Convert string hex to raw byte's
	 * 
	 * @param str
	 * @return
	 */
	protected static byte[] hexToBytes(final String str) {
		return Util.hexStringToByteArray(str);
	}

}
