/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.security;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for handling RSA keys Support for Web Crypto API
 */
public enum RsaCrypt {
	;

	private static final Logger LOG = LoggerFactory.getLogger(RsaCrypt.class);

	private static final String WEB_MODE = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private static final String WEB_MODE_JCA = "RSA/NONE/OAEPWithSHA-256AndMGF1Padding";
	private static final OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
			new MGF1ParameterSpec("SHA-256"), PSpecified.DEFAULT);

	/**
	 * Encrypt string with given RSA key
	 * 
	 * @param Buffer
	 * @param key
	 * @return
	 */
	public static String encrypt(final String data, final PublicKey key, final boolean isHex) {
		if (isHex) {
			return encryptHex(data, key);
		} else {
			return encryptBase64(data, key);
		}
	}

	/**
	 * Encrypt string with given RSA key into Base64 String format
	 * 
	 * @param data
	 * @param key
	 * @param webCryptoApi
	 * @return
	 */
	public static String encryptBase64(final String data, final PublicKey key) {
		final byte[] enc = encrypt(data.getBytes(), key);
		final Encoder base64 = Base64.getEncoder();
		return base64.encodeToString(enc);
	}

	/**
	 * Encrypt string with given RSA key into Hex String format
	 * 
	 * @param data
	 * @param key
	 * @param webCryptoApi
	 * @return
	 */
	public static String encryptHex(final String data, final PublicKey key) {
		final byte[] enc = encrypt(data.getBytes(), key);
		return Security.bytesToHex(enc);
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decrypt(final String data, final PrivateKey key, final boolean isHex) {
		if (isHex) {
			return decryptHex(data, key);
		} else {
			return decryptBase64(data, key);
		}
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decryptBase64(final String data, final PrivateKey key) {

		byte[] bin = null;

		bin = Base64.getDecoder().decode(data);
		bin = decrypt(bin, key);

		return bin;
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decryptHex(final String data, final PrivateKey key) {

		byte[] bin = null;

		bin = Security.hexToBytes(data);
		bin = decrypt(bin, key);

		return bin;
	}

	/**
	 * Decrypt data with private key and given mode
	 * 
	 * @param buffer
	 * @param key
	 * @param mode
	 * @return
	 */
	public static byte[] decrypt(final byte[] buffer, final PrivateKey key) {

		byte[] data = null;

		try {

			final Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
			data = cipher.doFinal(buffer);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			data = new byte[0];
		}

		return data;
	}

	/**
	 * Encrypt data with public key
	 * 
	 * @param Buffer
	 * @param key
	 * @return
	 */
	public static byte[] encrypt(final byte[] data, final PublicKey key) {

		byte[] result = null;

		try {

			final Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);
			result = cipher.doFinal(data);

		} catch (Exception e) {
			result = new byte[0];
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Initialize cipher from key as encoder or decoder
	 * 
	 * @param key
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	private static Cipher getCipher(final Key key, final int mode) throws Exception {

		Cipher cipher = null;

		try {
			cipher = Cipher.getInstance(WEB_MODE, BouncyCastleProvider.PROVIDER_NAME);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			cipher = Cipher.getInstance(WEB_MODE_JCA);
		}
		cipher.init(mode, key, oaepParams);

		return cipher;
	}

}
