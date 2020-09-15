/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Expose `Emitter`.
 */

if (typeof module !== 'undefined') {
	module.exports = Security;
}

/**
 * Security engine using Web Crypto API to encrypt / decrypt
 * messages between browser and server.
 *
 * Received RSA public key is signed and verified at the
 * browser side to prevent tampering
 */
Security = (() => {

	var VERSION = 0;
	var encKEY = null;
	var aesKEY = null;
	var exportedAES = null;

	const Encoder = new TextEncoder();

	/**
	 *  Use local challenge, to verify received data signature
	 *
	 *  @param {Object} cfg
	 *      Data received from server contins public key and signature
	 */
	function getChallenge(cfg) {
		return [cfg.challenge || '', cfg.keyEnc || '', cfg.keyVer || ''].join('');
	}

	/**
	 * Create random bytes
	 *
	 * @param {int} size
	 *     length of data (required)
	 */
	function getRandom(size) {
		let array = new Uint8Array(size);
		window.crypto.getRandomValues(array);
		return array;
	}

	/**
	 * Create AES key for data encryption
	 * @returns CryptoKey
	 */
	async function generateAesKey() {
		let type = {
			name: "AES-CTR",
			length: 128
		};
		let mode = ["encrypt", "decrypt"];
		return window.crypto.subtle.generateKey(type, true, mode);
	}

	/**
	 * Extract CryptoKey into RAW bytes
	 * @param {CryptoKey} key
	 * @returns Uin8Array
	 */
	async function exportAesKey(key) {
		let buffer = await window.crypto.subtle.exportKey("raw", key);
		return new Uint8Array(buffer);
	}

	/**
	 * Import RSA key received from server
	 * Key is publicKey used to send encrypted AES key
	 *
	 * @param {String} key
	 *          PEM encoded key without headers,
	 *          flattened in a single line
	 *
	 * @param {Object} type
	 *          Crypto API key definition format
	 *
	 * @param {String} mode
	 *          Key usage 'encrypt' or 'decrypt'
	 */
	async function importRsaKey(key, type, mode) {

		let binaryDerString = window.atob(key);
		let binaryDer = str2ab(binaryDerString);

		return window.crypto.subtle.importKey(
			"spki",
			binaryDer,
			type,
			true,
			[mode]
		);
	}

	/**
	 * Verify signature
	 *
	 * @param {CryptoKey}
	 *      Public key used for verification
	 *
	 * @param {ArrayBuffer} signature
	 *        Signature of received data
	 *
	 * @param {ArrayBuffer} challenge
	 *        Challenge to verify with signature (ts + pemENCDEC + pemVERSGN)
	 */
	async function verify(key, signature, challenge) {

		let binSignature = str2ab(atob(signature));
		let binChallenge = Encoder.encode(challenge);
		let type = {
			name: "ECDSA",
			hash: {
				name: "SHA-384"
			}
		};

		return window.crypto.subtle.verify(
			type,
			key,
			binSignature,
			binChallenge
		);
	}

	/**
	 * Encrypt message with RSA key
	 *
	 * @param {String || ArrayBuffer} data
	 *        String or AraryBuffer to encrypt
	 */
	async function encryptRSA(data) {

		let encoded = data;

		if (typeof data === 'string') {
			encoded = Encoder.encode(data);
		}

		return window.crypto.subtle.encrypt(
			"RSA-OAEP",
			encKEY,
			encoded
		);
	}

	/**
	 * Encrypt message with AES
	 */
	async function encryptAesMessage(key, iv, data) {

		let encoded = Encoder.encode(data);
		let type = {
			name: "AES-CTR",
			counter: iv,
			length: 128
		};

		return window.crypto.subtle.encrypt(type, key, encoded);
	}

	/**
	 * Decrypt AES encrypted message
	 */
	async function decryptAesMessage(key, iv, data) {

		let databin = hex2ab(data);
		let ivbin = hex2ab(iv);

		let counter = new Uint8Array(ivbin);
		let dataArray = new Uint8Array(databin);
		let type = {
			name: "AES-CTR",
			counter: counter,
			length: 128
		};

		return window.crypto.subtle.decrypt(type, key, dataArray);
	}

	function isValid() {
		return encKEY !== null && aesKEY !== null;
	}

	function isAvailable() {
		return window.crypto.subtle != null;
	}

	/********************************************************************/
	/*                   P U B L I C  F U N C T I O N S                 */
	/********************************************************************/

	/**
	 * Initialize encryption and verification keys
	 * Verifies data signatures to prevent tampering
	 */
	async function init(cfg) {

		if (!window.crypto.subtle) {
			console.log('Security mode not available, TLS protocol required.');
			return;
		}

		console.log('Security Initializing...');

		VERSION++;
		encKEY = await importRsaKey(cfg.keyEnc, {
			name: 'RSA-OAEP',
			hash: 'SHA-256'
		}, 'encrypt');
		aesKEY = await generateAesKey();
		exportedAES = await exportAesKey(aesKEY);

		let verKey = await importRsaKey(cfg.keyVer, {
			name: 'ECDSA',
			namedCurve: "P-384"
		}, 'verify');
		let status = await verify(verKey, cfg.signature, getChallenge(cfg || {}));

		if (!status) {
			encKEY = null;
			aesKEY = null;
			exportedAES = null;
			throw new Error('Signature invalid');
		}

		console.log('Security Initialized!');

	}

	/**
	 *  Ecnrypt received data in format {d:.., k:...}
	 * @param
	 * 		data  - string to encrypt
	 */
	async function encrypt(data, bin) {

		let iv = getRandom(16);
		let key = new Uint8Array(iv.length + exportedAES.length);

		key.set(iv);
		key.set(exportedAES, iv.length);

		let encryptedKey = await encryptRSA(key);
		let encryptedData = await encryptAesMessage(aesKEY, iv, data);

		if (bin === true) {
			return {
				d: encryptedData,
				k: encryptedKey
			};
		}
		return {
			d: buf2hex(encryptedData),
			k: buf2hex(encryptedKey)
		};

	}

	/**
	 * Decrypt received data in format {d:.., k:...}
	 *
	 * @param
	 * 		cfg  - data elements to decrypt
	 * 		cfg.d - aes encrypted server resposne
	 * 		cfg.k - aes IV used for masking
	 *
	 */
	async function decrypt(cfg) {

		let iv = cfg.iv;
		let data = cfg.d;

		let message = await decryptAesMessage(aesKEY, iv, data);

		var str = stringFromUTF8Array(new Uint8Array(message));
		var obj = JSON.parse(str);

		if (obj && obj.type == 'ws' && obj.cmd === 'data') {
			obj = obj.data;
		}

		return obj;
	}

	/**
	 * Exported object with external methods
	 */
	const exported = {

		isAvailable: function() {
			return isAvailable();
		},

		isActive: function() {
			return isValid();
		},

		init: function(cfg) {
			return init(cfg);
		},

		encrypt: function(cfg, bin) {
			return encrypt(cfg, bin);
		},

		decrypt: function(cfg) {
			return decrypt(cfg);
		}
	};
	Object.freeze(exported);

	return exported;
})();
