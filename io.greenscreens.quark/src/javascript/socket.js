/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Expose `Emitter`.
 */

if (typeof module !== 'undefined') {
  module.exports = SocketChannel;
}

/**
 * Web and WebSocket API engine
 * Used to call remote services.
 * All Direct functions linked to io.greenscreens namespace
 */
SocketChannel = (() => {

	const Decoder = new TextDecoder();
	const Encoder = new TextEncoder();

	var tid = 0;
	var queue = {
		up: 0,
		down: 0
	};

	var webSocket = null;

	/**
	 * Update coutners and queue to link resposnes to requests
	 * @param {Object} req
	 *      Request data
	 */
	function updateRequest(req, callback) {
		tid++;
		req.tid = tid.toString();
		queue[req.tid] = callback;
		queue.up++;
	}

	/**
	 * Rerset queue to remove old staleld elements
	 */
	function cleanQueue() {
		if (queue.up > 50 && queue.down >= queue.up) {
			queue = {
				up: 0,
				down: 0
			};
		}
	}

	/**
	 * Check if data can be encrypted
	 *
	 * @param {Object} req
	 */
	function canEncrypt(req) {
		let hasArgs = Array.isArray(req.data) && req.data.length > 0 && req.e !== false;
		return Security.isActive() && hasArgs;
	}

	/**
	 * Prepare remtoe call, encrypt if avaialble
	 *
	 * @param {Object} req
	 *         Data to send (optionaly encrypt)
	 */
	async function onCall(req, callback) {

		let msg = null;
		let enc = null;
		let data = null;
		let verb = 'data';

		let isEncrypt = canEncrypt(req);

		updateRequest(req, callback);

		// encrypt if supported
		if (isEncrypt) {
			enc = await Security.encrypt(JSON.stringify(req.data));
			req.data = [enc];
			verb = 'enc';
		}

		data = {
			cmd: verb,
			type: 'ws',
			data: [req]
		};
		msg = JSON.stringify(data);

		if (!Streams.isAvailable()) {
			return webSocket.send(msg);
		}

		msg = await Streams.compress(msg);
		webSocket.send(msg);
	}

	/**
	 * Parse and prepare received message for processing
	 *
	 * @param {String} mesasge
	 *
	 */
	async function prepareMessage(message) {

		let obj = null;

		try {

			if (message instanceof ArrayBuffer) {
				let text = await Streams.decompress(message);
				obj = JSON.parse(text);
			}

			if (typeof message === 'string') {
				obj = JSON.parse(message);
			}

			if (obj) {
				onMessage(obj);
			} else {
				Generator.emit('error', event);
			}

		} catch (e) {
			Generator.emit('error', e);
		}

	}

	/**
	 * Process received message
	 *
	 * @param {*} msg
	 *
	 */
	async function onMessage(obj) {

		let data = null;

		if (obj.cmd === 'api') {
			return Generator.emit('api', obj.data);
		}

		if (obj.cmd === 'err') {
			return Generator.emit('error', obj.result);
		}

		if (obj.cmd === 'data') {
			obj = obj.data;
			return doData(obj);
		}

		if (obj.cmd === 'enc') {

			data = await Security.decrypt(obj);

			if (data) {
			  return doData(data);
			}

		}

		Engine.emit('message', obj);
	}

	/**
	 * Process multiple records in a single response
	 *
	 * @param {Object || Array} obj
	 *
	 */
	function doData(obj) {

		if (Array.isArray(obj)) {

			obj.every(o => {
				onData(o);
				return true;
			});

		} else {

			onData(obj);

		}
	}

	/**
	 * Process single response record
	 *
	 * @param {Object} obj
	 */
	function onData(obj) {

		queue.down++;

		if (typeof queue[obj.tid] === 'function') {
			try {
				queue[obj.tid](null, obj);
			} finally {
				queue[obj.tid] = null;
			}
		} else {
			Engine.emit('message', obj);
		}

		cleanQueue();

	};

	/**
	 * Initialize API call listener
	 */
	function listener(req, callback) {

		onCall(req, callback)
			.catch((e) => {
				callback(e, null);
			});
	}

	/**
	 * If wss used in url, create WebSocket channel to
	 * exchange API messages
	 */
	async function startSocket(url, wasm, resolve, reject) {

		webSocket = new WebSocket(url, ['ws4is']);
		webSocket.binaryType = "arraybuffer";

		webSocket.onopen = function(event) {
			Generator.on('call', listener);
			resolve(true);
		};

		webSocket.onclose = function(event) {
			Generator.off('call', listener);
			webSocket = null;
			Engine.emit('offline');
		}

		webSocket.onerror = function(event) {
			Generator.off('call', listener);
			reject(event);
			Generator.emit('error', event);
			webSocket = null;
		};

		webSocket.onmessage = function(event) {
			prepareMessage(event.data);
		};

	}

	/**
	 * Initialize Socket channel
	 * @param {String} url
	 *      WebSocket Service URL
	 */
	function init(url, wasm) {

		kill();

		return new Promise((resolve, reject) => {
			startSocket(url, wasm, resolve, reject);
			return null;
		});

	}

	/**
	 * Close WebSocket channel if available
	 */
	function kill() {
		if (webSocket !== null) {
			webSocket.close();
			webSocket = null;
		}
	}

	/**
	 * Exported object with external methods
	 */
	var exported = {

		init: function(url, wasm) {
			return init(url, wasm);
		},

		kill: function() {
			return kill();
		}

	};

	Object.freeze(exported);

	return exported;

})();
