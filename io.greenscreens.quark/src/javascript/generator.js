/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Expose `Emitter`.
 */

if (typeof module !== 'undefined') {
	module.exports = Generator;
}

/**
 * Web and WebSocket API engine
 * Used to call remote services.
 * All Direct functions linked to defiend namespace
 */
Generator = (() => {

	/**
	 * Build JS object with callable functions that maps to Java side methods
	 * Data is retrieved from API service
	 *
	 * @param {String} url || api object
	 * 		  URL Address for API service definitions
	 */
	async function build(o) {
		let data = o.api || o;
		buildAPI(data);
		return data;
	}

	/**
	 * From API tree generate namespace tree and
	 * links generated functions to WebScoket api calls
	 *
	 * @param {Object} cfg
	 * 		Alternative definition to API
	 */
	function buildAPI(cfg) {

		if (Array.isArray(cfg)) {
			cfg.every(v => {
				buildInstance(v);
				return true;
			});
		} else {
			buildInstance(cfg);
		}

	}

	/**
	 * Build from single definition
	 *
	 * @param {Object} api
	 * 		  Java Class/Method definition
	 */
	function buildInstance(api) {

		let tree = null;
		let action = null;

		tree = buildNamespace(api.namespace);

		if (!tree[api.action]) {
			tree[api.action] = {};
		}
		action = tree[api.action];

		api.methods.every(v => {
			buildMethod(api.namespace, api.action, action, v);
			return true;
		});
	}

	/**
	 * Generate namespace object structure from string version
	 *
	 * @param  {String} namespace
	 * 			Tree structure delimited with dots
	 *
	 * @return {Object}
	 * 			Object tree structure
	 */
	function buildNamespace(namespace) {

		let tmp = null;

		namespace.split('.').every(v => {

			if (!tmp) {
				if (!window[v]) window[v] = {};
				tmp = window[v];
			} else {
				if (!tmp[v]) tmp[v] = {};
				Object.freeze(tmp);
				tmp = tmp[v];
			}

			return true;
		});

		return tmp;
	}

	/**
	 * Build instance methods
	 *
	 * @param {String} namespace
	 * @param {String} action
	 * @param {String} instance
	 * @param {Array} api
	 */
	function buildMethod(namespace, action, instance, api) {

		let enc = api.encrypt === false ? false : true;
		let cfg = {
			n: namespace,
			c: action,
			m: api.name,
			l: api.len,
			e: enc
		};

		instance[api.name] = apiFn(cfg);
		Object.freeze(instance[api.name]);
	}

	/**
	 * Generic function used to attach for generated API
	 *
	 * @param {Array} params List of arguments from caller
	 */
	function apiFn(params) {

		let prop = params;

		function fn() {

			let args, req, promise = null;

			args = Array.prototype.slice.call(arguments);

			req = {
				"namespace": prop.n,
				"action": prop.c,
				"method": prop.m,
				"e": prop.e,
				"data": args
			};

			promise = new Promise((resolve, reject) => {
				exported.emit('call', req, (err, obj) => {
					onResponse(err, obj, prop, resolve, reject);
				});
			});

			return promise;
		}

		return fn;
	}

	/**
	 * Process remote response
	 */
	function onResponse(err, obj, prop, response, reject) {

		if (err) {
			reject(err);
			return;
		}

		let sts = (prop.c === obj.action) &&
			(prop.m === obj.method) &&
			obj.result &&
			obj.result.success;

		if (sts) {
			response(obj.result);
		} else {
			reject(obj.result || obj);
		}

	};

	/**
	 * Exported object with external methods
	 */
	var exported = {

		build: function(cfg) {
			return build(cfg);
		}

	};

	Emitter(exported);
	Object.freeze(exported);

	return exported;

})();
