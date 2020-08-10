/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.security;

/**
 * Transcodes the JCA ASN.1/DER-encoded signature to support WebCrypto API
 * format
 */
public enum Transcoder {
	;

	/**
	 * Returns the expected signature byte array length (R + S parts) for the
	 * specified ECDSA algorithm.
	 *
	 * @param alg The ECDSA algorithm. Must be supported and not {@code null}.
	 * @return The expected byte array length for the signature.
	 * @throws JwtException If the algorithm is not supported.
	 */
	public static int getSignatureByteArrayLength(final int alg) throws Exception {

		switch (alg) {
		case 256:
			return 64;
		case 384:
			return 96;
		case 512:
			return 132;
		default:
			throw new Exception("Unsupported Algorithm: ");
		}
	}

	/**
	 * Transcodes the JCA ASN.1/DER-encoded signature into the concatenated R + S
	 * format expected by ECDSA JWS.
	 *
	 * @param derSignature The ASN1./DER-encoded. Must not be {@code null}.
	 * @param outputLength The expected length of the ECDSA JWS signature.
	 * @return The ECDSA JWS encoded signature.
	 * @throws JwtException If the ASN.1/DER signature format is invalid.
	 */
	public static byte[] transcodeSignatureToConcat(final byte[] derSignature, int outputLength) throws Exception {

		if (derSignature.length < 8 || derSignature[0] != 48) {
			throw new Exception("Invalid ECDSA signature format");
		}

		int offset;
		if (derSignature[1] > 0) {
			offset = 2;
		} else if (derSignature[1] == (byte) 0x81) {
			offset = 3;
		} else {
			throw new Exception("Invalid ECDSA signature format");
		}

		byte rLength = derSignature[offset + 1];

		int i = rLength;
		while ((i > 0) && (derSignature[(offset + 2 + rLength) - i] == 0)) {
			i--;
		}

		byte sLength = derSignature[offset + 2 + rLength + 1];

		int j = sLength;
		while ((j > 0) && (derSignature[(offset + 2 + rLength + 2 + sLength) - j] == 0)) {
			j--;
		}

		int rawLen = Math.max(i, j);
		rawLen = Math.max(rawLen, outputLength / 2);

		if ((derSignature[offset - 1] & 0xff) != derSignature.length - offset
				|| (derSignature[offset - 1] & 0xff) != 2 + rLength + 2 + sLength || derSignature[offset] != 2
				|| derSignature[offset + 2 + rLength] != 2) {
			throw new Exception("Invalid ECDSA signature format");
		}

		final byte[] concatSignature = new byte[2 * rawLen];

		System.arraycopy(derSignature, (offset + 2 + rLength) - i, concatSignature, rawLen - i, i);
		System.arraycopy(derSignature, (offset + 2 + rLength + 2 + sLength) - j, concatSignature, 2 * rawLen - j, j);

		return concatSignature;
	}

	/**
	 * Transcodes the ECDSA JWS signature into ASN.1/DER format for use by the JCA
	 * verifier.
	 *
	 * @param jwsSignature The JWS signature, consisting of the concatenated R and S
	 *                     values. Must not be {@code null}.
	 * @return The ASN.1/DER encoded signature.
	 * @throws JwtException If the ECDSA JWS signature format is invalid.
	 */
	public static byte[] transcodeSignatureToDER(byte[] jwsSignature) throws Exception {

		int rawLen = jwsSignature.length / 2;

		int i = rawLen;

		while ((i > 0) && (jwsSignature[rawLen - i] == 0)) {
			i--;
		}

		int j = i;

		if (jwsSignature[rawLen - i] < 0) {
			j += 1;
		}

		int k = rawLen;

		while ((k > 0) && (jwsSignature[2 * rawLen - k] == 0)) {
			k--;
		}

		int l = k;

		if (jwsSignature[2 * rawLen - k] < 0) {
			l += 1;
		}

		int len = 2 + j + 2 + l;

		if (len > 255) {
			throw new Exception("Invalid ECDSA signature format");
		}

		int offset;

		final byte derSignature[];

		if (len < 128) {
			derSignature = new byte[2 + 2 + j + 2 + l];
			offset = 1;
		} else {
			derSignature = new byte[3 + 2 + j + 2 + l];
			derSignature[1] = (byte) 0x81;
			offset = 2;
		}

		derSignature[0] = 48;
		derSignature[offset++] = (byte) len;
		derSignature[offset++] = 2;
		derSignature[offset++] = (byte) j;

		System.arraycopy(jwsSignature, rawLen - i, derSignature, (offset + j) - i, i);

		offset += j;

		derSignature[offset++] = 2;
		derSignature[offset++] = (byte) l;

		System.arraycopy(jwsSignature, 2 * rawLen - k, derSignature, (offset + l) - k, k);

		return derSignature;
	}
}