/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo3;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.OutputQueue;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintObjectPageInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;

import io.greenscreens.demo.DemoURLConstants;
import io.greenscreens.demo1.Authenticated;
import io.greenscreens.demo1.SystemI;
import io.greenscreens.demo2.FileUtil;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;

/**
 * Example controller class to work with Spool files
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "SPOOL")
public class SpoolController {

	enum STATE {HOLD, RELEASE}

	private static final Logger LOG = LoggerFactory.getLogger(SpoolController.class);

	@Inject @Authenticated
	SystemI as400;

	/**
	 * Load spool pages as string and return to the browser
	 * Use default WSCST for conversion,
	 * to support different code-pages, custom WSCST is required
	 * @param data
	 * @return
	 */
	@ExtJSMethod("load")
	public ExtJSResponse load(final SpoolData data) {

		final ExtJSResponse.Builder builder = ExtJSResponse.Builder.create();

		final SpooledFile file = get(data);
		final PrintParameterList printParms = getCovnertOpt();

		PrintObjectPageInputStream is = null;
		try {
			is = file.getPageInputStream(printParms);

			final String pages = getPages(is);

			builder.setMessage(pages);
			builder.setStatus(true);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		} finally {
			FileUtil.close(is);
		}

		return builder.build();
	}

	/**
	 * Remove spool file
	 * @param data
	 * @return
	 */
	@ExtJSMethod("remove")
	public ExtJSResponse remove(final SpoolData data) {

		final ExtJSResponse.Builder builder = ExtJSResponse.Builder.create();

		final SpooledFile file = get(data);

		try {
			file.delete();
			builder.setStatus(true);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return builder.build();
	}

	/**
	 * Move spool file to OUTQ
	 * @param data
	 * @param outq
	 * @return
	 */
	@ExtJSMethod("move")
	public ExtJSResponse move(final SpoolData data, final String outq) {

		final ExtJSResponse.Builder builder = ExtJSResponse.Builder.create();

		final SpooledFile file = get(data);

		try {
			final OutputQueue queue = new OutputQueue(as400, outq);
			file.move(queue);
			builder.setStatus(true);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return builder.build();

	}

	/**
	 * Change spool file state
	 * @param data
	 * @param state
	 * @return
	 */
	@ExtJSMethod("state")
	public ExtJSResponse state(final SpoolData data, final STATE state) {

		final ExtJSResponse.Builder builder = ExtJSResponse.Builder.create();

		final SpooledFile file = get(data);

		try {
			switch (state) {
			case RELEASE:
				file.release();
				break;
			case HOLD:
				file.hold("*IMMED");
				break;
			default:
				break;
			}
			builder.setStatus(true);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return builder.build();
	}

	/**
	 * Convert web data into jt400 spooledfile object
	 * @param data
	 * @return
	 */
	private SpooledFile get(final SpoolData data) {
		return new SpooledFile(as400, data.getSpoolName(), data.getNumber(),
				data.getJobName(), data.getJobUser(), data.getJobNumber());
	}

	/**
	 * Create spool conversion configuration
	 * @return
	 */
	private PrintParameterList getCovnertOpt() {

		final PrintParameterList printParms = new PrintParameterList();

		// https://www.ibm.com/support/pages/retrieving-and-modifying-wscst-source-qwpdefault
		printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST");
		printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

		// printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*HP4");

		// printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPGIF.WSCST");
		// printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPTIFFG4.WSCST");

		return printParms;
	}

	/**
	 * Read spool file page b page and convert into string
	 * @param pois
	 * @return
	 * @throws IOException
	 */
	private String getPages(final PrintObjectPageInputStream pois) throws IOException {

		final StringBuilder sb = new StringBuilder();

		boolean hasNext = true;

		while (hasNext) {

			int size = pois.available();
			byte[] raw = new byte[size];
			pois.read(raw);

			sb.append(new String(raw, "Cp037"));
			sb.append((char) 0x0c); // page break

			hasNext = pois.nextPage();
		}

		return sb.toString();
	}

}
