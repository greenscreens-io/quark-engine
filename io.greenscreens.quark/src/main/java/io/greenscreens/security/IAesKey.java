/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public interface IAesKey {

	/**
	 * Set key used to encrypt data
	 * 
	 * @param secretKey
	 */
	void setSecretKey(String secretKey);

	void setSecretKey(byte[] secretKey);

	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	void setIv(String iv);

	void setIv(byte[] iv);

	/**
	 * Encrypt string and return raw byte's
	 * 
	 * @param text
	 * @return
	 * @throws Exception
	 */
	byte[] encryptData(String text) throws Exception;

	byte[] encryptData(String text, byte[] iv) throws Exception;

	byte[] encryptData(String text, IvParameterSpec iv) throws Exception;

	/**
	 * Decrypt hex encoded data to byte array
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	byte[] decryptData(String code) throws Exception;

	byte[] decryptData(String code, String iv) throws Exception;

	byte[] decryptData(String code, byte[] iv) throws Exception;

	byte[] decryptData(String code, IvParameterSpec iv) throws Exception;

	byte[] decryptData(byte[] data, byte[] iv) throws Exception;

	byte[] decryptData(byte[] data, IvParameterSpec iv) throws Exception;

	/**
	 * Encrypts string to hex string
	 */
	String encrypt(String text) throws Exception;

	String encrypt(String text, byte[] iv) throws Exception;

	String encrypt(String text, IvParameterSpec iv) throws Exception;

	/**
	 * Decrypts hex string to string value
	 */
	String decrypt(String text) throws Exception;

	byte[] decrypt(byte[] data) throws Exception;

	byte[] encrypt(byte[] data) throws Exception;

	Cipher getCipher();

}