/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo4;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.greenscreens.demo.DemoURLConstants;
import io.greenscreens.demo1.Authenticated;
import io.greenscreens.demo1.SystemI;
import io.greenscreens.ext.ExtJSObjectResponse;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;
import io.greenscreens.jt400.programs.qsys.qdcrdevd.DEVD0100;
import io.greenscreens.jt400.programs.qsys.qdcrdevd.IQDCRDEVD;
import io.greenscreens.jt400.programs.qsys.qdcrdevd.QDCRDEVD;

/**
 * Get workstation session data by display name
 * Call from JavaScript (browser or NodeJS)
 * let obj = await io.greenscreens.QDCRDEVD.describe('QPADEV0001'):
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "QDCRDEVD")
public class QDCRDEVDController {

	private static final Logger LOG = LoggerFactory.getLogger(QDCRDEVDController.class);

	private static final String MSG_DSP	= "Display name must be at least 1 character long";

	@Inject
	@Authenticated
	SystemI as400;

	/**
	 * Call QDCRDEVD program to retrieve display data
	 *
	 * @param data example - QPADEV001
	 * @return
	 */
	@ExtJSMethod(value = "describe", validate = true)
	public ExtJSObjectResponse<DEVD0100> describe(
			@NotNull @NotEmpty(message = MSG_DSP)
			final String displayName) {

		final ExtJSObjectResponse.Builder<DEVD0100> builder = ExtJSObjectResponse.Builder.create(DEVD0100.class);

		try {
			// QDCRDEVD program params
			final QDCRDEVD params = QDCRDEVD.build(DEVD0100.class, displayName);

			// QDCRDEVD program to be called
			final IQDCRDEVD program = IQDCRDEVD.create(as400);

			// call IBM i program and parse response into DEVD0100 format
			final DEVD0100 result = program.call(params, DEVD0100.class);

			// set web response data
			builder.setData(result);
			builder.setStatus(true);

		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return builder.build();

	}

}
