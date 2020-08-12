/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo1;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.cdi.Required;
import io.greenscreens.demo.DemoURLConstants;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;

/**
 * Example controller class to work with AS400 object
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "AS400")
public class AS400Controller {

	private static final Logger LOG = LoggerFactory.getLogger(AS400Controller.class);

	// we support storing multiple as400 objects under different names
	// use for an example @SessionAttribute("as400") or just use default
	@Inject
	SystemI as400;

	// we have our own producer, as default will inject session only if it exists
	@Inject @Autoinit
	HttpSession session;

	@ExtJSMethod("login")
	public ExtJSResponse login(@Required final String system,
							@Required final String userName,
							@Required final String password) {

		final ExtJSResponse.Builder resp = ExtJSResponse.Builder.create();

		try {
			as400.setUsePasswordCache(false);
			as400.setSystemName(system);
			as400.authenticate(userName, password);
			resp.setStatus(true);
		} catch (Exception e) {
			resp.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return resp.build();
	}

	@ExtJSMethod("logout")
	public ExtJSResponse logout() {
		as400.disconnectAllServices();
		session.invalidate();
		return new ExtJSResponse(true, null);
	}

}
