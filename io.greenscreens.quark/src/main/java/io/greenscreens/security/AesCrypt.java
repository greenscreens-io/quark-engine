/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.security;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.Util;

/**
 * AEC encryption & Decryption utility
 */
final class AesCrypt implements IAesKey {

	private static final Logger LOG = LoggerFactory.getLogger(AesCrypt.class);

	final private static Charset ASCII = Charset.forName("ASCII");
	final private static Charset UTF8 = Charset.forName("UTF-8");
	
	private static Cipher cipher;

	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;

	static {
		try {
			//cipher = Cipher.getInstance("AES/OFB/NoPadding");
			cipher = Cipher.getInstance("AES/CTR/NoPadding");			
		} catch (GeneralSecurityException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public AesCrypt(final String secretKey) {
		super();
		setSecretKey(secretKey);
	}

	public AesCrypt(final byte[] secretKey) {
		super();
		setSecretKey(secretKey);
	}

	/**
	 * Set key used to encrypt data, length must be 16 characters
	 * 
	 * @param secretKey
	 */
	@Override
	public void setSecretKey(final String secretKey) {
		if (secretKey == null || secretKey.length() != 16) {
			throw new RuntimeException("Invalid AES key length");
		}
		keyspec = new SecretKeySpec(secretKey.getBytes(ASCII), "AES");
	}

	/**
	 * Set key used to encrypt data, length must be 16 bytes
	 * 
	 * @param secretKey
	 */
	@Override
	public void setSecretKey(final byte[] secretKey) {
		if (secretKey == null || secretKey.length != 16) {
			throw new RuntimeException("Invalid AES key length");
		}
		keyspec = new SecretKeySpec(secretKey, "AES");		
	}
	
	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	@Override
	public void setIv(final String iv) {
		ivspec = new IvParameterSpec(iv.getBytes(ASCII));
	}

	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	@Override
	public void setIv(final byte [] iv) {
		ivspec = new IvParameterSpec(iv);
	}
	
	/**
	 * Encrypt string and return raw byte's
	 * 
	 * @param text
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[] encryptData(final String text) throws Exception {
		return encryptData(text, ivspec);
	}

	@Override
	public byte[] encryptData(final String text, final byte[] iv) throws Exception {
		return encryptData(text, new IvParameterSpec(iv));
	}

	@Override
	public byte[] encryptData(final String text, final IvParameterSpec iv) throws Exception {

		if (text == null || text.length() == 0) {
			throw new Exception("Empty string");
		}

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, iv);
			encrypted = cipher.doFinal(padString(text).getBytes(UTF8));
		} catch (Exception e) {
			LOG.error(e.getMessage());
            LOG.debug(e.getMessage(), e);
			throw e;
		}

		return encrypted;
	}

	/**
	 * Decrypt hex encoded data to byte array
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[] decryptData(final String code) throws Exception {
		return decryptData(code, ivspec);
	}

	@Override
	public byte[] decryptData(final String code, final String iv) throws Exception {
		return decryptData(code, iv.getBytes());
	}

	@Override
	public byte[] decryptData(final String code, final byte[] iv) throws Exception {
		return decryptData(code, new IvParameterSpec(iv));
	}

	@Override
	public byte[] decryptData(final String code, final IvParameterSpec iv) throws Exception {

		if (code == null || code.length() == 0) {
			throw new Exception("Empty string");
		}

		byte[] decrypted = {};

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
			decrypted = cipher.doFinal(Security.hexToBytes(code));
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}

	@Override
	public byte[] decryptData(final byte[] data, final byte[] iv) throws Exception {
		return decryptData(data, new IvParameterSpec(iv));

	}

	@Override
	public byte[] decryptData(final byte[] data, final IvParameterSpec iv) throws Exception {
		
		byte[] decrypted = {};

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);

			decrypted = cipher.doFinal(data);
		} catch (Exception e) {
			LOG.error(e.getMessage());
            LOG.debug(e.getMessage(), e);
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}


	/**
	 * Encrypts string to hex string
	 */
	@Override
	public String encrypt(final String text) throws Exception {
		return Security.bytesToHex(encryptData(text));
	}

	@Override
	public String encrypt(final String text, final byte[] iv) throws Exception {
		return Security.bytesToHex(encryptData(text, new IvParameterSpec(iv)));
	}

	@Override
	public String encrypt(final String text, IvParameterSpec iv) throws Exception {
		return Security.bytesToHex(encryptData(text, iv));
	}

	/**
	 * Decrypts hex string to string value
	 */
	@Override
	public String decrypt(final String text) throws Exception {
		return new String(decryptData(text), UTF8);
	}

	/**
	 * Blank padding for AES algorithm
	 * 
	 * @param source
	 * @return
	 */
	private String padString(final String source) {
		return Util.padString(source, 16);
	}

	@Override
	public byte[] decrypt(final byte[] data) throws Exception {
		return decryptData(data, ivspec);
	}

	@Override
	public byte[] encrypt(final byte[] data) throws Exception {
		
		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			encrypted = cipher.doFinal(data);
		} catch (Exception e) {
			LOG.error(e.getMessage());
            LOG.debug(e.getMessage(), e);
			throw e;
		}

		return encrypted;

	}

	@Override
	public Cipher getCipher() {
		return cipher;
	}

}
