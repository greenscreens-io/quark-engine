/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

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

		// remove all existing listeners
		Generator.off('call');

		// close socket if used
		if (SocketChannel) {
			SocketChannel.kill();
		}

		cfg = cfg || {};

		// initialize API
		let data = await Generator.build(cfg.api);

		// initialize encryption if provided
		if (data.signature) {
			if (!Security.isActive()) {
				await Security.init(data);
			}
		}

		// if remote API defined
		if (cfg.service) {

			// register HTTP/S channel for API
			if (cfg.service.indexOf('http') === 0) {
				await WebChannel.init(cfg.service);
				return true;
			}

			// register WebSocket channel for API
			if (cfg.service.indexOf('ws') === 0) {
				await SocketChannel.init(cfg.service);
				return true;
			}
		}

		throw new Error(ERROR_MESSAGE);

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
