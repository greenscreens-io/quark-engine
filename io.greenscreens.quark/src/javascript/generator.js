/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

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
	 * @param {String} url  
	 * 		  UR LAddress for API service definitions
	 */
	async function build(url) {
	  	
	  let app = location.pathname.split('/')[1];
	  let service = url ? url : (location.origin + `/${app}/api`);		
	  let id = Date.now();
	 
	  let resp = await fetch(service, {method: 'get', headers : {'x-time' :id}});
      let data = await resp.json();
      
      // update local challenge for signature verificator
      data.challenge = id.toString();
      
	  buildAPI(data.api);
	  
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
			cfg.every(function(v) {
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
		
		var tree = null;
		var action = null;
		
		tree = buildNamespace(api.namespace);
		
		if (!tree[api.action]) {
			tree[api.action] = {};		  
		}
		action = tree[api.action];
			
		api.methods.every(function(v) {
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
		
		var tmp = null;
		
		namespace.split('.').every(function(v) {
			
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

		let cfg = {
			n: namespace,
			c: action,
			m: api.name,
			l: api.len
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

		var prop = params;

		function fn() {

            let args, req, promise = null;
            
            args = Array.prototype.slice.call(arguments);

			req = {
				"namespace": prop.n,
				"action": prop.c,
				"method": prop.m,
				"data": args
			};
			
			promise = new Promise(function(resolve, reject) {
                exported.emit('call', req, function(err, obj) {
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
		
		var sts = (prop.c === obj.action) 
				&& (prop.m === obj.method) 
				&& obj.result 
				&& obj.result.success;

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