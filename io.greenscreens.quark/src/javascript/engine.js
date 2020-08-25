/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Expose `Emitter`.
 */

if (typeof module !== 'undefined') {
	module.exports = Engine;
}

/**
 * Web and WebSocket API engine
 * Used to initialize remote API and remote services.
 */
Engine = (() => {

	const ERROR_MESSAGE = 'Invalid definition for Engine Remote Service';

	/**
	 * Build Js Api from JSON definition retrieved from API service
	 */
	async function init(cfg) {

		cfg = cfg || {};

		if (!cfg.api) {
			throw new Error('API Url not defined!');
		}

		// remove all existing listeners
		Generator.off('call');

		// close socket if used
		if (SocketChannel) {
			SocketChannel.kill();
		}

		let isWSChannel = cfg.api === cfg.service && cfg.api.indexOf('ws') == 0;

		if (isWSChannel) {
			return await fromWebSocketChannel(cfg);
		}

		await fromWebChannel(cfg);
		let sts = await initService(cfg);
		if (sts) return true;

		throw new Error(ERROR_MESSAGE);

	}

	/**
	 * Initialize API from WebSocket channel
	 *
	 * @param {Object} cfg
	 * 		  Init configuration object with api and service url's
	 */
	async function initService(cfg) {

		// if remote API defined
		if (!cfg.service) return false;

		// register HTTP/S channel for API
		if (cfg.service.indexOf('http') === 0) {
			await WebChannel.init(cfg.service);
			return true;
		}

		// register WebSocket channel for API
		if (cfg.service.indexOf('ws') === 0) {
			await SocketChannel.init(cfg.service, cfg.wasm);
			return true;
		}

		return false;

	}

	/**
	 * Initialize API from WebSocket channel
	 *
	 * @param {Object} cfg
	 * 		  Init configuration object with api and service url's
	 */
	function fromWebSocketChannel(cfg) {

		return new Promise((resolve, reject) => {

			var challenge = Date.now();

			Generator.once('api', async (data) => {

				data.challenge = challenge;
				try {
					await registerAPI(data);
					resolve(true);
				} catch (e) {
					reject(e);
				}

			});

			SocketChannel.init(cfg.service + '?q=' + challenge, cfg.wasm);

			return null;
		});

	}

	/**
	 * Initialize API from HTTP/s channel
	 *
	 * @param {Object} cfg
	 * 		  Init configuration object with api and service url's
	 */
	async function fromWebChannel(cfg) {
		let data = await getAPI(cfg.api);
		await registerAPI(data);
	}

	/**
	 * Register callers from API definition
	 *
	 * @param {Object} data
	 * 		  API definitions receive from server
	 */
	async function registerAPI(data) {

		// initialize encryption if provided
		if (data.signature) {
			if (!Security.isActive()) {
				await Security.init(data);
			}
		}

		Generator.build(data.api);
	}

	/**
	 * Get API definition through HTTP/s channel
	 *
	 * @param {String} url
	 * 		  URL Address for API service definitions
	 */
	async function getAPI(url) {

		let app = location.pathname.split('/')[1];
		let service = url ? url : (location.origin + `/${app}/api`);
		let id = Date.now();

		let resp = await fetch(service, {
			method: 'get',
			headers: {
				'x-time': id
			}
		});
		let data = await resp.json();

		// update local challenge for signature verificator
		data.challenge = id.toString();

		return data;

	}

	/**
	 * Exported object with external methods
	 */
	var exported = {

		init: function(cfg) {
			return init(cfg);
		}

	};

	Emitter(exported);
	Object.freeze(exported);

	return exported;

})();
