/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Web and WebSocket API engine
 * Used to call remote services.
 * All Direct functions linked to io.greenscreens namespace
 */
SocketChannel = (() => {

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
		let hasArgs = Array.isArray(req.data) && req.data.length > 0;
		return Security.isActive() && hasArgs;
	}

	/**
	 * Prepare remtoe call, encrypt if avaialble
	 *
	 * @param {Object} req
	 *         Data to send (optionaly encrypt)
	 */
	async function onCall(req, callback) {

		let enc = null;
		let data = null;

		let isEncrypt = canEncrypt(req);

		updateRequest(req, callback);

		// encrypt if supported
		if (isEncrypt) {

			enc = await Security.encrypt(JSON.stringify(req.data));
			req.data = [enc];
			data = {
				cmd: 'enc',
				type: 'ws',
				data: [req]
			};

		} else {
			data = {
				cmd: 'data',
				type: 'ws',
				data: [req]
			};
		}

		// send
		webSocket.send(JSON.stringify(data));

	}

	/**
	 * Parse and prepare received message for processing
	 *
	 * @param {String} mesasge
	 *
	 */
	function prepareMessage(message) {

		let obj = null;

		try {

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

		if (obj.cmd === 'err') {
			return Generator.emit('error', obj.result);
		}

		if (obj.cmd === 'data') {
			obj = obj.data;
			doData(obj);
		}

		if (obj.cmd === 'enc') {

			data = await Security.decrypt(obj);

			if (data) {
				doData(data);
			}

		}
	}

	/**
	 * Process multiple records in a single response
	 *
	 * @param {Object || Array} obj
	 *
	 */
	function doData(obj) {

		if (Array.isArray(obj)) {

			obj.every(function(o) {
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
	function startSocket(url, resolve, reject) {

		webSocket = new WebSocket(url, ['ws4is']);

		webSocket.onopen = function(event) {
			Generator.on('call', listener);
			resolve(true);
		};

		webSocket.onclose = function(event) {
			Generator.off('call', listener);
			webSocket = null;
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
	function init(url) {

		kill();

		return new Promise(function(resolve, reject) {
			return startSocket(url, resolve, reject);
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

		init: function(url) {
			return init(url);
		},

		kill: function() {
			return kill();
		}

	};

	Object.freeze(exported);

	return exported;

})();
