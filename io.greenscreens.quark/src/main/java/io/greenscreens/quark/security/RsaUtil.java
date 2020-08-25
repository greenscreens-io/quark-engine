/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * RSA utility to work with public and private keys
 */
enum RsaUtil {
	;

	private final static int KEY_SIZE = 1024;
	private final static String PUBLIC = "PUBLIC KEY";
	private final static String PRIVATE = "PRIVATE KEY";

	private static final String regex = "(-{5}[A-Z ]*-{5})";
	private static final String regnl = "\\s{2,}";

	public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException, NoSuchProviderException {
		// return KeyFactory.getInstance("RSA", "SC");
		return KeyFactory.getInstance("RSA");
	}

	public static KeyPairGenerator getKeyPairGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
		// return KeyPairGenerator.getInstance("RSA", "SC");
		return KeyPairGenerator.getInstance("RSA");
	}

	/**
	 * Convert PEM multi line to single line
	 * 
	 * @param val
	 * @return
	 */
	public static final String flatten(final String val) {
		if (val != null) {
			return val.replaceAll(regex, "").replaceAll(regnl, "");
		} else {
			return val;
		}
	}

	/**
	 * Convert PEM file to decoded byte array Removes headers and new lines then
	 * decode from Base64
	 * 
	 * @param raw
	 * @return
	 */
	public static byte[] convertFromPEM(final byte[] raw) {
		String data = new String(raw);
		final String[] lines = data.split("\\r?\\n");
		if (lines.length > 1) {
			lines[0] = "";
			lines[lines.length - 1] = "";
			data = String.join("", lines);
		}
		return Base64.getDecoder().decode(data);
	}

	public static PrivateKey getPrivateKey(final String key) throws Exception {

		final byte[] raw = convertFromPEM(key.getBytes());
		KeyFactory fact = getKeyFactory();
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(raw);
		PrivateKey priv = fact.generatePrivate(keySpec);
		Arrays.fill(raw, (byte) 0);
		return priv;
	}

	public static PrivateKey getPrivateKey2(final String key) throws Exception {
		final byte[] raw = convertFromPEM(key.getBytes());
		final ASN1Sequence primitive = (ASN1Sequence) ASN1Sequence.fromByteArray(raw);
		final Enumeration<?> e = primitive.getObjects();
		final BigInteger v = ((ASN1Integer) e.nextElement()).getValue();

		final int version = v.intValue();
		if (version != 0 && version != 1) {
			throw new IllegalArgumentException("wrong version for RSA private key");
		}

		/**
		 * In fact only modulus and private exponent are in use.
		 */
		final BigInteger modulus = ((ASN1Integer) e.nextElement()).getValue();
		@SuppressWarnings("unused")
		final BigInteger publicExponent = ((ASN1Integer) e.nextElement()).getValue();
		final BigInteger privateExponent = ((ASN1Integer) e.nextElement()).getValue();
		/*
		 * BigInteger prime1 = ((ASN1Integer) e.nextElement()).getValue(); BigInteger
		 * prime2 = ((ASN1Integer) e.nextElement()).getValue(); BigInteger exponent1 =
		 * ((ASN1Integer) e.nextElement()).getValue(); BigInteger exponent2 =
		 * ((ASN1Integer) e.nextElement()).getValue(); BigInteger coefficient =
		 * ((ASN1Integer) e.nextElement()).getValue();
		 */

		final RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
		final KeyFactory kf = getKeyFactory();
		final PrivateKey pkey = kf.generatePrivate(keySpec);
		return pkey;
	}

	public static PublicKey getPublicKey(final String key) throws Exception {
		final byte[] raw = convertFromPEM(key.getBytes());
		final X509EncodedKeySpec spec = new X509EncodedKeySpec(raw);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePublic(spec);
	}

	public static KeyPair generateRSAKeyPair() throws Exception {
		// final SecureRandom random = new SecureRandom();
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4);
		final KeyPairGenerator generator = getKeyPairGenerator();
		generator.initialize(spec, random);
		return generator.generateKeyPair();
	}

	public static String getPem(final Key key, final String name) throws IOException {
		final StringWriter writer = new StringWriter();
		final PemWriter pemWriter = new PemWriter(writer);
		pemWriter.writeObject(new PemObject(name, key.getEncoded()));
		pemWriter.flush();
		pemWriter.close();
		return writer.toString();
	}

	public static String toPublicPem(final Key key) throws IOException {
		return getPem(key, PUBLIC);
	}

	public static String toPrivatePem(final Key key) throws IOException {
		return getPem(key, PRIVATE);
	}

	public static String toPublicPem(final KeyPair key) throws IOException {
		return toPublicPem(key.getPublic());
	}

	public static String toPrivatePem(final KeyPair key) throws IOException {
		return toPrivatePem(key.getPrivate());
	}

}
